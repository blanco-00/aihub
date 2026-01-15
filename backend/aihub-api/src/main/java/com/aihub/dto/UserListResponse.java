package com.aihub.dto;

import lombok.Data;
import java.time.LocalDateTime;

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
    private String role;
    private String roleDescription;
    private Long departmentId;
    private String departmentName;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
