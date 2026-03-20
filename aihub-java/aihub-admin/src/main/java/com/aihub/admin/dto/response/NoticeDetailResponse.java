package com.aihub.admin.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知详情响应DTO（包含完整内容）
 */
@Data
public class NoticeDetailResponse {
    
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
    
    // 发布范围（用于编辑时回显）
    private List<Long> departmentIds;
    
    private List<Long> roleIds;
    
    private List<Long> userIds;
}
