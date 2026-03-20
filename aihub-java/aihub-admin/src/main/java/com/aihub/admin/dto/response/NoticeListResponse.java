package com.aihub.admin.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知列表响应DTO（扩展字段，用于管理员列表）
 */
@Data
public class NoticeListResponse {
    
    private Long id;
    
    private String title;
    
    private Long categoryId;
    
    private String categoryName;
    
    private Integer type;
    
    private Integer publishType;
    
    private String publisherName;
    
    private Integer status;
    
    private LocalDateTime publishTime;
    
    private LocalDateTime expireTime;
    
    private Integer viewCount;
    
    private Integer readCount;
    
    private Integer unreadCount;
    
    private LocalDateTime createdAt;
}
