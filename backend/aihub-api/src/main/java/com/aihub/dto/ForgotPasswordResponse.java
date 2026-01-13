package com.aihub.dto;

import lombok.Data;

/**
 * 忘记密码响应DTO（包含验证码，仅开发模式）
 */
@Data
public class ForgotPasswordResponse {
    
    /**
     * 验证码（仅开发模式返回，生产环境不返回）
     */
    private String code;
    
    /**
     * 提示信息
     */
    private String message;
}
