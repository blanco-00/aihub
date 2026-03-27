package com.aihub.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ModelConfigResponse {

    private Long id;

    private String name;

    private String vendor;

    private String modelId;

    private String apiKey;

    private String baseUrl;

    private Integer status;

    private String config;

    private String modelType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
