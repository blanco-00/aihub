package com.aihub.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新Token请求DTO
 */
@Data
public class RefreshTokenRequest {
    
    @NotBlank(message = "刷新Token不能为空")
    private String refreshToken;
}
