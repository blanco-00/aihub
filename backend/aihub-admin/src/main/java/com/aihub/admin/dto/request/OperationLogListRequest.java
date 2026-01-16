package com.aihub.admin.dto.request;

import lombok.Data;

/**
 * 操作日志列表查询请求DTO
 */
@Data
public class OperationLogListRequest {
    
    private Integer current = 1;
    
    private Integer size = 10;
    
    private String username;
    
    private String module;
    
    private String operation;
    
    private Integer status;
    
    private String startTime;
    
    private String endTime;
}
