package com.aihub.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息响应DTO
 */
@Data
public class ChatMessageResponse {

    private Long id;

    /**
     * 会话ID
     */
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
    private Long modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 响应时间(毫秒)
     */
    private Integer responseTimeMs;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
