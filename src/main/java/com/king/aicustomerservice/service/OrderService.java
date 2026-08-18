package com.king.aicustomerservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.king.aicustomerservice.entity.CartItem;
import com.king.aicustomerservice.entity.Order;
import com.king.aicustomerservice.entity.OrderItem;
import com.king.aicustomerservice.entity.Product;
import com.king.aicustomerservice.entity.SysUser;
import com.king.aicustomerservice.entity.UserAddress;
import com.king.aicustomerservice.enums.OrderStatus;
import com.king.aicustomerservice.mapper.OrderItemMapper;
import com.king.aicustomerservice.mapper.OrderMapper;
import com.king.aicustomerservice.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务类
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final ProductService productService;
    private final CartService cartService;
    private final SysUserService sysUserService;
    private final AddressService addressService;

    /**
     * 从购物车创建订单并扣减库存
     *
     * @param addressId 可选，传入则使用已保存的收货地址
     */
    @Transactional
    public Order createFromCart(SysUser user, Long addressId, String receiverName, String receiverPhone, String address) {
        if (addressId != null) {
            UserAddress saved = addressService.findOwned(addressId, user.getId());
            receiverName = saved.getReceiverName();
            receiverPhone = saved.getReceiverPhone();
            address = saved.fullAddress();
        }
        if (!StringUtils.hasText(receiverName) || !StringUtils.hasText(receiverPhone) || !StringUtils.hasText(address)) {
            throw new RuntimeException("请选择或填写完整的收货地址");
        }
        List<CartItem> cartItems = cartService.listByUser(user.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车是空的");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = productMapper.selectById(cartItem.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new RuntimeException("商品已下架，无法下单");
            }
            productService.decreaseStock(product.getId(), cartItem.getQuantity());
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(user.getId());
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.UNPAID);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setAddress(address);
        orderMapper.insert(order);

        for (CartItem cartItem : cartItems) {
            Product product = productMapper.selectById(cartItem.getProductId());
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getImage());
            item.setPrice(product.getPrice());
            item.setQuantity(cartItem.getQuantity());
            orderItemMapper.insert(item);
        }
        cartService.clear(user.getId());
        return findById(order.getId());
    }

    /**
     * 查询用户订单列表
     */
    public List<Order> listByUser(Long userId) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getId));
        orders.forEach(this::fillItems);
        return orders;
    }

    /**
     * 管理端分页查询订单
     */
    public Page<Order> pageForAdmin(int page, int size, String status, String keyword) {
        OrderStatus orderStatus = null;
        if (StringUtils.hasText(status)) {
            try {
                orderStatus = OrderStatus.from(status);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(orderStatus != null, Order::getStatus, orderStatus)
                .like(StringUtils.hasText(keyword), Order::getOrderNo, keyword)
                .orderByDesc(Order::getId);
        Page<Order> result = orderMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(order -> {
            fillItems(order);
            SysUser user = sysUserService.findById(order.getUserId());
            if (user != null) {
                order.setUsername(user.getUsername());
            }
        });
        return result;
    }

    /**
     * 根据ID查询订单（含明细）
     */
    public Order findById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        fillItems(order);
        return order;
    }

    /**
     * 管理端按ID查询订单，附带用户名和明细
     */
    public Order findByIdForAdmin(Long id) {
        Order order = findById(id);
        SysUser user = sysUserService.findById(order.getUserId());
        if (user != null) {
            order.setUsername(user.getUsername());
        }
        return order;
    }

    /**
     * 根据订单号查询
     */
    public Order findByOrderNo(String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        fillItems(order);
        return order;
    }

    /**
     * 根据订单号查询，且必须属于指定用户（他人订单按不存在处理）
     */
    public Order findByOrderNoForUser(String orderNo, Long userId) {
        if (!StringUtils.hasText(orderNo)) {
            throw new RuntimeException("请提供订单号");
        }
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo.trim()));
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("未找到该订单，请确认订单号是否正确，且属于您本人下单");
        }
        fillItems(order);
        return order;
    }

    /**
     * 校验订单归属后返回
     */
    public Order findOwned(Long orderId, Long userId) {
        Order order = findById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看该订单");
        }
        return order;
    }

    /**
     * 支付成功后更新订单状态
     */
    @Transactional
    public void markPaid(String orderNo, String alipayTradeNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            return;
        }
        if (order.getStatus() == null || !order.getStatus().canPay()) {
            return;
        }
        order.setStatus(OrderStatus.PAID);
        order.setAlipayTradeNo(alipayTradeNo);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    /**
     * 管理员修改订单状态
     */
    public void updateStatus(Long id, OrderStatus status) {
        if (status == null) {
            throw new RuntimeException("请指定订单状态");
        }
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setStatus(status);
        orderMapper.updateById(order);
    }

    /**
     * 用户取消未支付订单
     */
    public void cancel(Long orderId, Long userId) {
        Order order = findOwned(orderId, userId);
        if (order.getStatus() == null || !order.getStatus().canCancel()) {
            throw new RuntimeException("仅未支付订单可以取消");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderMapper.updateById(order);
    }

    /**
     * 统计订单总数
     */
    public long count() {
        return orderMapper.selectCount(null);
    }

    /**
     * 生成简单订单号
     */
    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    /**
     * 填充订单明细
     */
    private void fillItems(Order order) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        order.setItems(items);
    }
}
