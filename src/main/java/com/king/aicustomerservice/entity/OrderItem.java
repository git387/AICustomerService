package com.king.aicustomerservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细实体
 */
@Data
@TableName("order_item")
public class OrderItem {

    /** 明细ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 商品ID */
    private Long productId;

    /** 商品快照名称 */
    private String productName;

    /** 商品快照图片 */
    private String productImage;

    /** 成交单价 */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;
}
