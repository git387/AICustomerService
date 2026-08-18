package com.king.aicustomerservice.controller;

import com.king.aicustomerservice.common.Result;
import com.king.aicustomerservice.entity.CartItem;
import com.king.aicustomerservice.entity.Order;
import com.king.aicustomerservice.entity.Product;
import com.king.aicustomerservice.entity.SysUser;
import com.king.aicustomerservice.entity.UserAddress;
import com.king.aicustomerservice.service.AddressService;
import com.king.aicustomerservice.service.CartService;
import com.king.aicustomerservice.service.CategoryService;
import com.king.aicustomerservice.service.ChatService;
import com.king.aicustomerservice.service.OrderService;
import com.king.aicustomerservice.service.ProductService;
import com.king.aicustomerservice.service.SecurityUserHelper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 普通用户端 JSON 接口
 * 覆盖商品浏览、购物车、下单和智能客服
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserApiController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ChatService chatService;
    private final AddressService addressService;
    private final SecurityUserHelper securityUserHelper;

    /**
     * 商品分页列表
     */
    @GetMapping("/products")
    public Result<?> products(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "12") int size,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) String keyword) {
        return Result.ok(productService.pageForUser(page, size, categoryId, keyword));
    }

    /**
     * 商品详情
     */
    @GetMapping("/products/{id}")
    public Result<Product> product(@PathVariable Long id) {
        return Result.ok(productService.findById(id));
    }

    /**
     * 启用中的分类列表
     */
    @GetMapping("/categories")
    public Result<?> categories() {
        return Result.ok(categoryService.listAll());
    }

    /**
     * 查询购物车
     */
    @GetMapping("/cart")
    public Result<List<CartItem>> cart() {
        SysUser user = securityUserHelper.requireUser();
        return Result.ok(cartService.listByUser(user.getId()));
    }

    /**
     * 加入购物车
     */
    @PostMapping("/cart")
    public Result<Void> addCart(@RequestBody CartRequest request) {
        SysUser user = securityUserHelper.requireUser();
        int quantity = request.getQuantity() == null ? 1 : request.getQuantity();
        cartService.add(user.getId(), request.getProductId(), quantity);
        return Result.ok("已加入购物车", null);
    }

    /**
     * 修改购物车数量
     */
    @PutMapping("/cart/{id}")
    public Result<Void> updateCart(@PathVariable Long id, @RequestBody CartRequest request) {
        SysUser user = securityUserHelper.requireUser();
        cartService.updateQuantity(user.getId(), id, request.getQuantity());
        return Result.ok();
    }

    /**
     * 删除购物车项
     */
    @DeleteMapping("/cart/{id}")
    public Result<Void> deleteCart(@PathVariable Long id) {
        SysUser user = securityUserHelper.requireUser();
        cartService.delete(user.getId(), id);
        return Result.ok();
    }

    /**
     * 提交订单
     */
    @PostMapping("/orders")
    public Result<Order> createOrder(@RequestBody OrderRequest request) {
        SysUser user = securityUserHelper.requireUser();
        Order order = orderService.createFromCart(user, request.getAddressId(),
                request.getReceiverName(), request.getReceiverPhone(), request.getAddress());
        return Result.ok("下单成功", order);
    }

    /**
     * 当前用户订单列表
     */
    @GetMapping("/orders")
    public Result<List<Order>> myOrders() {
        SysUser user = securityUserHelper.requireUser();
        return Result.ok(orderService.listByUser(user.getId()));
    }

    /**
     * 订单详情
     */
    @GetMapping("/orders/{id}")
    public Result<Order> orderDetail(@PathVariable Long id) {
        SysUser user = securityUserHelper.requireUser();
        return Result.ok(orderService.findOwned(id, user.getId()));
    }

    /**
     * 取消未支付订单
     */
    @PostMapping("/orders/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        SysUser user = securityUserHelper.requireUser();
        orderService.cancel(id, user.getId());
        return Result.ok("订单已取消", null);
    }

    /**
     * 智能客服提问
     */
    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@RequestBody ChatRequest request) {
        SysUser user = securityUserHelper.requireUser();
        String answer = chatService.chat(user.getId(), request.getQuestion());
        return Result.ok(Map.of("answer", answer));
    }

    /**
     * 聊天历史
     */
    @GetMapping("/chat/history")
    public Result<?> chatHistory() {
        SysUser user = securityUserHelper.requireUser();
        return Result.ok(chatService.history(user.getId()));
    }

    /**
     * 当前用户收货地址列表
     */
    @GetMapping("/addresses")
    public Result<List<UserAddress>> addresses() {
        SysUser user = securityUserHelper.requireUser();
        return Result.ok(addressService.listByUser(user.getId()));
    }

    /**
     * 新增收货地址
     */
    @PostMapping("/addresses")
    public Result<UserAddress> saveAddress(@RequestBody UserAddress address) {
        SysUser user = securityUserHelper.requireUser();
        return Result.ok("地址已保存", addressService.save(user.getId(), address));
    }

    /**
     * 更新收货地址
     */
    @PutMapping("/addresses/{id}")
    public Result<Void> updateAddress(@PathVariable Long id, @RequestBody UserAddress address) {
        SysUser user = securityUserHelper.requireUser();
        address.setId(id);
        addressService.update(user.getId(), address);
        return Result.ok("地址已更新", null);
    }

    /**
     * 删除收货地址
     */
    @DeleteMapping("/addresses/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        SysUser user = securityUserHelper.requireUser();
        addressService.delete(user.getId(), id);
        return Result.ok("地址已删除", null);
    }

    /**
     * 设为默认收货地址
     */
    @PutMapping("/addresses/{id}/default")
    public Result<Void> defaultAddress(@PathVariable Long id) {
        SysUser user = securityUserHelper.requireUser();
        addressService.setDefault(user.getId(), id);
        return Result.ok("已设为默认地址", null);
    }

    /**
     * 购物车请求体
     */
    @Data
    public static class CartRequest {
        private Long productId;
        private Integer quantity;
    }

    /**
     * 下单请求体
     */
    @Data
    public static class OrderRequest {
        /** 已保存的地址ID，优先使用 */
        private Long addressId;
        private String receiverName;
        private String receiverPhone;
        private String address;
    }

    /**
     * 客服提问请求体
     */
    @Data
    public static class ChatRequest {
        private String question;
    }
}
