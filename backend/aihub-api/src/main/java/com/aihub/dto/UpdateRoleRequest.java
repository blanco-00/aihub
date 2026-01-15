package com.aihub.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新角色请求DTO
 */
@Data
public class UpdateRoleRequest {
    
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String name;
    
    @Size(max = 255, message = "角色描述长度不能超过255个字符")
    private String description;
    
    private Integer status;
}
