package com.aihub.admin.service;

import com.aihub.admin.dto.request.CreateMenuRequest;
import com.aihub.admin.dto.response.MenuResponse;
import com.aihub.admin.dto.request.UpdateMenuRequest;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {
    
    /**
     * 获取菜单树（所有菜单）
     */
    List<MenuResponse> getMenuTree();
    
    /**
     * 根据ID获取菜单详情
     */
    MenuResponse getMenuById(Long id);
    
    /**
     * 创建菜单
     */
    void createMenu(CreateMenuRequest request);
    
    /**
     * 更新菜单
     */
    void updateMenu(Long id, UpdateMenuRequest request);
    
    /**
     * 删除菜单（逻辑删除）
     */
    void deleteMenu(Long id);
    
    /**
     * 根据角色代码获取菜单树（用于动态路由）
     */
    List<MenuResponse> getMenuTreeByRoleCode(String roleCode);
}
