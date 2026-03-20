package com.aihub.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话响应DTO
 */
@Data
public class ChatSessionResponse {

    private Long id;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 使用的模型ID
     */
    private Long modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * Prompt模板ID
     */
    private Long promptTemplateId;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 总Token数
     */
    private Integer totalTokens;

    /**
     * 最后一条消息时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageAt;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
