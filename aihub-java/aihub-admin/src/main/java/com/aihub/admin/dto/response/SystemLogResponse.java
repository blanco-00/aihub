package com.aihub.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统日志响应DTO
 */
@Data
public class SystemLogResponse {
    
    private Long id;
    
    private String level; // 日志级别 DEBUG/INFO/WARN/ERROR
    
    private String module; // 模块名称
    
    private String message; // 日志消息
    
    private String stackTrace; // 堆栈信息
    
    private String ip; // IP地址
    
    private Long userId; // 用户ID
    
    private String requestId; // 请求ID
    
    private LocalDateTime logTime; // 日志时间
}
