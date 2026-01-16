package com.aihub.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建用户请求DTO
 */
@Data
public class CreateUserRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @Size(max = 50, message = "用户昵称长度不能超过50个字符")
    private String nickname;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 手机号（可选）
     */
    private String phone;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 50, message = "密码长度必须在8-50个字符之间")
    private String password;
    
    @NotBlank(message = "角色不能为空")
    private String role;
    
    /**
     * 部门ID（可选，0表示未分配）
     */
    private Long departmentId;
    
    /**
     * 状态（1-启用，0-禁用），默认启用
     */
    private Integer status = 1;
    
    /**
     * 备注
     */
    private String remark;
}
