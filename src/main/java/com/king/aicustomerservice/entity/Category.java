package com.king.aicustomerservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类实体类
 */
@Data
@TableName("category")
public class Category {

    /** 分类ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称 */
    private String name;

    /** 分类描述 */
    private String description;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态: 0-禁用, 1-正常 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
