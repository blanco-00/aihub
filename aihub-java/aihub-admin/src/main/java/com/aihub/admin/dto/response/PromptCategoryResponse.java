package com.aihub.admin.dto.response;

import lombok.Data;

@Data
public class PromptCategoryResponse {

    private Long id;

    private String name;

    private String code;

    private String description;

    private Integer sortOrder;
}
