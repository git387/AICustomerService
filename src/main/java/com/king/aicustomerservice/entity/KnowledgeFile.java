package com.king.aicustomerservice.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文件实体
 */
@Data
@TableName("knowledge_file")
public class KnowledgeFile {

    /** 文件ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原始文件名 */
    private String originalName;

    /** 磁盘存储路径 */
    private String storedPath;

    /** 文件类型：txt / doc / pdf / md */
    private String fileType;

    /** 分块数量 */
    private Integer chunkCount;

    /** Redis 向量文档 ID 列表，逗号分隔 */
    private String vectorIds;

    /** 处理状态：PENDING / SUCCESS / FAIL */
    private String status;

    /** 失败原因 */
    private String errorMsg;

    /** 上传时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;
}
