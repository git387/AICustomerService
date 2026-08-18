package com.king.aicustomerservice.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.king.aicustomerservice.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体
 */
@Data
@TableName("orders")
public class Order {

    /** 订单ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 订单总额 */
    private BigDecimal totalAmount;

    /** 订单状态 */
    private OrderStatus status;

    /** 收货人 */
    private String receiverName;

    /** 收货电话 */
    private String receiverPhone;

    /** 收货地址 */
    private String address;

    /** 支付宝交易号 */
    private String alipayTradeNo;

    /** 下单时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 下单用户名（非表字段） */
    @TableField(exist = false)
    private String username;

    /** 订单明细（非表字段） */
    @TableField(exist = false)
    private List<OrderItem> items;
}
