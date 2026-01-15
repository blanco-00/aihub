package com.aihub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建角色请求DTO
 */
@Data
public class CreateRoleRequest {
    
    @NotBlank(message = "角色代码不能为空")
    @Size(max = 50, message = "角色代码长度不能超过50个字符")
    private String code;
    
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String name;
    
    @Size(max = 255, message = "角色描述长度不能超过255个字符")
    private String description;
    
    private Integer status = 1;
}
