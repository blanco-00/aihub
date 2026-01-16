package com.aihub.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单响应DTO
 */
@Data
public class MenuResponse {
    
    private Long id;
    
    private Long parentId;
    
    private String name;
    
    private String path;
    
    private String component;
    
    private String redirect;
    
    private String icon;
    
    private String title;
    
    private Integer sortOrder;
    
    private Integer showLink;
    
    private Integer keepAlive;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    /**
     * 子菜单列表
     */
    private List<MenuResponse> children;
}
