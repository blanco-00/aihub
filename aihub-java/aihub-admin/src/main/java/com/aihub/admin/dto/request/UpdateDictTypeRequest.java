package com.aihub.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新字典类型请求DTO
 */
@Data
public class UpdateDictTypeRequest {
    
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称长度不能超过100个字符")
    private String dictName;
    
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型长度不能超过100个字符")
    private String dictType;
    
    private Integer status;
    
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
