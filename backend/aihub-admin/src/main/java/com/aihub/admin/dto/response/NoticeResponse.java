package com.aihub.admin.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知响应DTO
 */
@Data
public class NoticeResponse {
    
    private Long id;
    
    private String title;
    
    private String content;
    
    private Long categoryId;
    
    private String categoryName;
    
    private Integer type;
    
    private Integer publishType;
    
    private Long publisherId;
    
    private String publisherName;
    
    private Integer status;
    
    private LocalDateTime publishTime;
    
    private LocalDateTime expireTime;
    
    private Integer sortOrder;
    
    private Integer viewCount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
