package com.aihub.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 在线用户响应DTO
 */
@Data
public class OnlineUserResponse {
    
    private Long userId;
    
    private String username;
    
    private String ip;
    
    private String address;
    
    private String system;
    
    private String browser;
    
    private LocalDateTime loginTime;
    
    private String token;
}
