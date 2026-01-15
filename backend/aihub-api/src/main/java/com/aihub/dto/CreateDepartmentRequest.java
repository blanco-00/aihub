package com.aihub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建部门请求DTO
 */
@Data
public class CreateDepartmentRequest {
    
    @NotBlank(message = "部门名称不能为空")
    private String name;
    
    private Long parentId;
    
    private Integer sortOrder;
    
    private Integer status;
    
    private String remark;
}
