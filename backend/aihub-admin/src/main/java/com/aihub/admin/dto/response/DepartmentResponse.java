package com.aihub.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门响应DTO
 */
@Data
public class DepartmentResponse {
    
    private Long id;
    
    private String name;
    
    private Long parentId;
    
    private Integer sortOrder;
    
    private Integer status;
    
    private String remark;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private List<DepartmentResponse> children;
}
