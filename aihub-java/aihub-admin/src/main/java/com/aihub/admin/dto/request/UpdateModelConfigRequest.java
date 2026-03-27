package com.aihub.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateModelConfigRequest {

    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "厂商不能为空")
    @Pattern(regexp = "^[A-Za-z]+$", message = "厂商只能包含字母")
    private String vendor;

    @NotBlank(message = "模型ID不能为空")
    @Size(max = 100, message = "模型ID长度不能超过100个字符")
    private String modelId;

    @NotBlank(message = "API Key不能为空")
    @Size(max = 255, message = "API Key长度不能超过255个字符")
    private String apiKey;

    @Size(max = 255, message = "Base URL长度不能超过255个字符")
    private String baseUrl;

    private Integer status;

    private String config;

    private String modelType;
}
