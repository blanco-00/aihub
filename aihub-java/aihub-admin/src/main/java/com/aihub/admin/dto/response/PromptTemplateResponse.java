package com.aihub.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromptTemplateResponse {

    private Long id;

    private String name;

    private String description;

    private Long categoryId;

    private String categoryName;

    private String content;

    private String variables;

    private Integer isBuiltin;

    private Integer status;

    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
