package com.aihub.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePromptTemplateRequest {

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称长度不能超过100个字符")
    private String name;

    @Size(max = 500, message = "模板描述长度不能超过500个字符")
    private String description;

    private Long categoryId;

    @NotBlank(message = "模板内容不能为空")
    private String content;

    /**
     * 变量定义（JSON格式）
     */
    private String variables;
}
