package com.aihub.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建会话请求DTO
 */
@Data
public class CreateSessionRequest {

    /**
     * 会话标题
     */
    @Size(max = 200, message = "会话标题长度不能超过200个字符")
    private String title;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * Prompt模板ID
     */
    private Long promptTemplateId;
}
