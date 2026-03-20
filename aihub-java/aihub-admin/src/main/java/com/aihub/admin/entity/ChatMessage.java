package com.aihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    @TableField("session_id")
    private Long sessionId;

    /**
     * 角色 user/assistant/system
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * Token数量
     */
    private Integer tokens;

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
     * 响应时间(毫秒)
     */
    @TableField("response_time_ms")
    private Integer responseTimeMs;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
