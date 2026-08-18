package com.king.aicustomerservice.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能客服聊天记录实体
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    /** 消息ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 角色：user / assistant */
    private String role;

    /** 消息内容 */
    private String content;

    /** 发送时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
