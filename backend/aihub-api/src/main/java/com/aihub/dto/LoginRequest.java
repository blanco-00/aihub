package com.aihub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "用户名或邮箱不能为空")
    private String usernameOrEmail;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 记住我（7天有效期）
     */
    private Boolean rememberMe = false;
}
