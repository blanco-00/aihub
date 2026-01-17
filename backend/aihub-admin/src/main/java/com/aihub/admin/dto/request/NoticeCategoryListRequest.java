package com.aihub.admin.dto.request;

import lombok.Data;

/**
 * 通知分类列表查询请求DTO
 */
@Data
public class NoticeCategoryListRequest {
    
    private Integer current = 1;
    
    private Integer size = 10;
    
    private String name;
    
    private String code;
    
    private Integer status;
}
