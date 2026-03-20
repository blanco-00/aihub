package com.aihub.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字典类型响应DTO
 */
@Data
public class DictTypeResponse {
    
    private Long id;
    
    private String dictName;
    
    private String dictType;
    
    private Integer status;
    
    private String remark;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
