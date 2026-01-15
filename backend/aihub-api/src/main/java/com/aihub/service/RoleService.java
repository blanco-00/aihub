package com.aihub.service;

import com.aihub.dto.CreateRoleRequest;
import com.aihub.dto.RoleResponse;
import com.aihub.dto.UpdateRoleRequest;

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
}
