package com.aihub.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作日志响应DTO
 */
@Data
public class OperationLogResponse {
    
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String module;
    
    private String operation;
    
    private String method;
    
    private String url;
    
    private String params;
    
    private String result;
    
    private Integer status;
    
    private String ip;
    
    private Integer duration;
    
    private LocalDateTime operationTime;
}
