package com.aihub.admin.service;

import com.aihub.admin.dto.request.CreateRoleRequest;
import com.aihub.admin.dto.response.RoleResponse;
import com.aihub.admin.dto.request.UpdateRoleRequest;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {
    
    /**
     * 获取所有角色列表
     */
    List<RoleResponse> getAllRoles();
    
    /**
     * 根据ID获取角色详情
     */
    RoleResponse getRoleById(Long id);
    
    /**
     * 创建角色
     */
    void createRole(CreateRoleRequest request);
    
    /**
     * 更新角色
     */
    void updateRole(Long id, UpdateRoleRequest request);
    
    /**
     * 删除角色（逻辑删除）
     */
    void deleteRole(Long id);
    
    /**
     * 获取角色的菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);
    
    /**
     * 保存角色菜单关联
     */
    void saveRoleMenus(Long roleId, List<Long> menuIds);
}
