package com.aihub.admin.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新菜单请求DTO
 */
@Data
public class UpdateMenuRequest {
    
    private Long parentId;
    
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String name;
    
    @Size(max = 200, message = "路由路径长度不能超过200个字符")
    private String path;
    
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    private String component;
    
    @Size(max = 200, message = "重定向路径长度不能超过200个字符")
    private String redirect;
    
    @Size(max = 50, message = "图标长度不能超过50个字符")
    private String icon;
    
    @Size(max = 100, message = "菜单标题长度不能超过100个字符")
    private String title;
    
    private Integer sortOrder;
    
    private Integer showLink;
    
    private Integer keepAlive;
    
    private Integer status;
}
