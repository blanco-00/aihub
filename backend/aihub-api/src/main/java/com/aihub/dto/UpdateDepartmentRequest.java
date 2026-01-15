package com.aihub.dto;

import lombok.Data;

/**
 * 更新部门请求DTO
 */
@Data
public class UpdateDepartmentRequest {
    
    private String name;
    
    private Long parentId;
    
    private Integer sortOrder;
    
    private Integer status;
    
    private String remark;
}
