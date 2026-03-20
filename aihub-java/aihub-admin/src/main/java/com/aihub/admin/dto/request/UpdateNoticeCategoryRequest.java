package com.aihub.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新通知分类请求DTO
 */
@Data
public class UpdateNoticeCategoryRequest {
    
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String name;
    
    @NotBlank(message = "分类代码不能为空")
    @Size(max = 50, message = "分类代码长度不能超过50个字符")
    private String code;
    
    @Size(max = 200, message = "分类描述长度不能超过200个字符")
    private String description;
    
    private Integer sortOrder;
    
    private Integer status;
}
