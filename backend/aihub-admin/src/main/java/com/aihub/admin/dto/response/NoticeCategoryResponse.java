package com.aihub.admin.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知分类响应DTO
 */
@Data
public class NoticeCategoryResponse {
    
    private Long id;
    
    private String name;
    
    private String code;
    
    private String description;
    
    private Integer sortOrder;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
