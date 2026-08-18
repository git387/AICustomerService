package com.king.aicustomerservice.tool;

import com.king.aicustomerservice.entity.Order;
import com.king.aicustomerservice.entity.OrderItem;
import com.king.aicustomerservice.enums.OrderStatus;
import com.king.aicustomerservice.service.OrderService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.stream.Collectors;

/**
 * 智能客服订单查询工具
 * 绑定当前用户 ID，只能查询该用户自己的订单
 */
public class OrderQueryTool {

    private final Long userId;
    private final OrderService orderService;

    public OrderQueryTool(Long userId, OrderService orderService) {
        this.userId = userId;
        this.orderService = orderService;
    }

    /**
     * 供大模型调用：按订单号查询当前用户订单
     */
    @Tool(description = "根据订单号查询当前登录用户自己的订单详情。只能查询该用户本人下单的订单，找不到时不要编造。")
    public String queryOrderByOrderNo(
            @ToolParam(description = "用户提供的订单号，例如 202608181200001234") String orderNo) {
        try {
            Order order = orderService.findByOrderNoForUser(orderNo, userId);
            return formatOrder(order);
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    private String formatOrder(Order order) {
        String items = (order.getItems() == null || order.getItems().isEmpty())
                ? "无"
                : order.getItems().stream()
                .map(this::formatItem)
                .collect(Collectors.joining("；"));
        return """
                订单号：%s
                订单ID：%d
                状态：%s
                金额：¥%s
                收货人：%s %s
                收货地址：%s
                商品：%s
                下单时间：%s
                支付时间：%s
                详情页链接：[查看订单详情](/orders/%d)
                """.formatted(
                order.getOrderNo(),
                order.getId(),
                OrderStatus.labelOf(order.getStatus()),
                order.getTotalAmount(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getAddress(),
                items,
                order.getCreateTime(),
                order.getPayTime() == null ? "未支付" : order.getPayTime(),
                order.getId()
        );
    }

    private String formatItem(OrderItem item) {
        return item.getProductName() + " × " + item.getQuantity() + "（¥" + item.getPrice() + "）";
    }
}
