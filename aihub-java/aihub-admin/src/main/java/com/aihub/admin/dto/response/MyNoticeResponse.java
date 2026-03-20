package com.aihub.admin.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我的通知响应DTO（包含已读状态）
 */
@Data
public class MyNoticeResponse {
    
    private Long id;
    
    private String title;
    
    private String content;
    
    private Long categoryId;
    
    private String categoryName;
    
    private Integer type;
    
    private String publisherName;
    
    private LocalDateTime publishTime;
    
    private LocalDateTime expireTime;
    
    private Integer isRead;
    
    private LocalDateTime readTime;
    
    private LocalDateTime createdAt;
}
