package com.aihub.admin.dto.request;

import lombok.Data;

/**
 * 更新个人信息请求
 */
@Data
public class UpdateProfileRequest {
    
    private String nickname;
    
    private String email;
    
    private String phone;
    
    private String description;
    
    private String avatar;
}
