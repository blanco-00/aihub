package com.aihub.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字典数据响应DTO
 */
@Data
public class DictDataResponse {
    
    private Long id;
    
    private String dictType;
    
    private String dictLabel;
    
    private String dictValue;
    
    private Integer sortOrder;
    
    private Integer status;
    
    private String remark;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
