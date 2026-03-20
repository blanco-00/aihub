package com.aihub.admin.dto.request;

import lombok.Data;

/**
 * 通知列表查询请求DTO
 */
@Data
public class NoticeListRequest {
    
    private Integer current = 1;
    
    private Integer size = 10;
    
    private String title;
    
    private Long categoryId;
    
    private Integer type;
    
    private Integer publishType;
    
    private Integer status;
    
    private String startTime;
    
    private String endTime;
}
