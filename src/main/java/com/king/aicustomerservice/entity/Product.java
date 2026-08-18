package com.king.aicustomerservice.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@TableName("product")
public class Product {

    /** 商品ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称 */
    private String name;

    /** 分类ID */
    private Long categoryId;

    /** 售价 */
    private BigDecimal price;

    /** 库存 */
    private Integer stock;

    /** 主图访问路径 */
    private String image;

    /** 商品简介 */
    private String description;

    /** 状态：0下架 1上架 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 分类名称（非表字段，列表展示用） */
    @TableField(exist = false)
    private String categoryName;
}
