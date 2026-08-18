package com.king.aicustomerservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车项实体
 */
@Data
@TableName("cart_item")
public class CartItem {

    /** 购物车项ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 数量 */
    private Integer quantity;

    /** 加入时间 */
    private LocalDateTime createTime;

    /** 商品名称（非表字段） */
    @TableField(exist = false)
    private String productName;

    /** 商品图片（非表字段） */
    @TableField(exist = false)
    private String productImage;

    /** 商品单价（非表字段） */
    @TableField(exist = false)
    private BigDecimal price;

    /** 商品库存（非表字段） */
    @TableField(exist = false)
    private Integer stock;
}
