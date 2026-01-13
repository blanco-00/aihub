package com.aihub.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    
    /**
     * 访问Token
     */
    private String token;
    
    /**
     * 刷新Token
     */
    private String refreshToken;
    
    /**
     * Token过期时间（秒）
     */
    private Long expiresIn;
    
    /**
     * 用户信息
     */
    private UserInfo user;
    
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String role;
        private String roleDescription;
    }
}
