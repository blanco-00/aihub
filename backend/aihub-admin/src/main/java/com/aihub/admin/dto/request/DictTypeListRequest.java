package com.aihub.admin.dto.request;

import lombok.Data;

/**
 * 字典类型列表查询请求DTO
 */
@Data
public class DictTypeListRequest {
    
    private Integer current = 1;
    
    private Integer size = 10;
    
    private String dictName;
    
    private String dictType;
    
    private Integer status;
    
    private String startTime;
    
    private String endTime;
}
