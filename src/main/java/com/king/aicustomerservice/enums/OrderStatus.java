package com.king.aicustomerservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

/**
 * 订单状态枚举
 * 数据库与接口均使用英文 code，页面展示用中文 label
 */
@RequiredArgsConstructor
public enum OrderStatus {

    /** 未支付 */
    UNPAID("UNPAID", "未支付"),

    /** 已支付 */
    PAID("PAID", "已支付"),

    /** 已发货 */
    SHIPPED("SHIPPED", "已发货"),

    /** 已完成 */
    COMPLETED("COMPLETED", "已完成"),

    /** 已取消 */
    CANCELLED("CANCELLED", "已取消");

    /** 存入数据库 / 前后端传输的状态码 */
    @EnumValue
    private final String code;

    /** 中文展示名 */
    private final String label;

    /**
     * 序列化为状态码，保证前端仍收到 UNPAID 等字符串
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 反序列化：支持 code 或枚举名
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OrderStatus from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();
        for (OrderStatus status : values()) {
            if (status.code.equalsIgnoreCase(text) || status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("不支持的订单状态: " + value);
    }

    /**
     * 是否可发起支付
     */
    public boolean canPay() {
        return this == UNPAID;
    }

    /**
     * 是否可取消
     */
    public boolean canCancel() {
        return this == UNPAID;
    }

    /**
     * 是否属于已付款成功类状态（已支付/已发货/已完成）
     */
    public boolean isPaidSuccess() {
        return this == PAID || this == SHIPPED || this == COMPLETED;
    }

    /**
     * 安全取中文名
     */
    public static String labelOf(OrderStatus status) {
        return status == null ? "" : status.getLabel();
    }
}
