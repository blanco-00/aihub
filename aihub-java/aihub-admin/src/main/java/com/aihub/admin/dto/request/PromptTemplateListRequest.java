package com.aihub.admin.dto.request;

import lombok.Data;

@Data
public class PromptTemplateListRequest {

    private Long categoryId;

    private String keyword;

    private Integer status;

    private Long current = 1L;

    private Long size = 10L;
}
