package com.king.aicustomerservice.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@TableName("sys_user")
public class SysUser {

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录名 */
    private String username;

    /** MD5 加密后的密码 */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 角色：ADMIN / USER */
    private String role;

    /** 状态：0禁用 1正常 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
