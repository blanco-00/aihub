package com.aihub.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色响应DTO
 */
@Data
public class RoleResponse {
    
    private Long id;
    
    private String code;
    
    private String name;
    
    private String description;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
