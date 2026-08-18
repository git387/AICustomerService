package com.king.aicustomerservice.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址实体
 */
@Data
@TableName("user_address")
public class UserAddress {

    /** 地址ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 收货人 */
    private String receiverName;

    /** 手机号 */
    private String receiverPhone;

    /** 省 */
    private String province;

    /** 市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 详细地址 */
    private String detail;

    /** 是否默认：0否 1是 */
    private Integer isDefault;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 用户名（非表字段，管理端展示用） */
    @TableField(exist = false)
    private String username;

    /**
     * 拼接完整地址，下单时写入订单快照
     */
    public String fullAddress() {
        StringBuilder builder = new StringBuilder();
        if (province != null) {
            builder.append(province);
        }
        if (city != null) {
            builder.append(city);
        }
        if (district != null) {
            builder.append(district);
        }
        if (detail != null) {
            builder.append(detail);
        }
        return builder.toString();
    }
}
