package com.aihub.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户列表响应DTO
 */
@Data
public class UserListResponse {
    
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String description;
    private String role;
    private String roleDescription;
    private List<Long> roleIds; // 用户的所有角色ID列表
    private List<String> roleNames; // 用户的所有角色名称列表
    private Long departmentId;
    private String departmentName;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
