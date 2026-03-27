package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型配置实体类
 */
@Data
@TableName("model_config")
public class ModelConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 厂商 OpenAI/Claude/DeepSeek等
     */
    private String vendor;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * API Key（加密存储）
     */
    private String apiKey;

    /**
     * Base URL
     */
    private String baseUrl;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 模型配置参数（JSON格式）
     */
    private String config;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 是否为默认模型
     */
    @TableField("is_default")
    private Integer isDefault;

    /**
     * 模型类型: chat=对话模型, embedding=向量模型, image=文生图模型, audio=语音模型, rerank=重排序模型
     */
    @TableField("model_type")
    private String modelType;
}
