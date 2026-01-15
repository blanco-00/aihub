package com.aihub.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 登录日志响应DTO
 */
@Data
public class LoginLogResponse {
    
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String ip;
    
    private String address;
    
    private String userAgent;
    
    private Integer status;
    
    private String message;
    
    private LocalDateTime loginTime;
}
