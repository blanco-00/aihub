package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话实体类
 */
@Data
@TableName("chat_session")
public class ChatSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 使用的模型ID
     */
    @TableField("model_id")
    private Long modelId;

    /**
     * 模型名称
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 使用的Prompt模板ID
     */
    @TableField("prompt_template_id")
    private Long promptTemplateId;

    /**
     * 消息数量
     */
    @TableField("message_count")
    private Integer messageCount;

    /**
     * 总Token数
     */
    @TableField("total_tokens")
    private Integer totalTokens;

    /**
     * 最后一条消息时间
     */
    @TableField("last_message_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageAt;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @TableField("is_deleted")
    private Integer isDeleted;
}
