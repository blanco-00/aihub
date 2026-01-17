package com.aihub.admin.service;

import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.UserListRequest;
import com.aihub.admin.dto.request.CreateUserRequest;
import com.aihub.admin.dto.request.UpdateUserRequest;
import com.aihub.admin.dto.response.UserListResponse;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 获取用户列表（分页、搜索、筛选）
     */
    PageResult<UserListResponse> getUserList(UserListRequest request);
    
    /**
     * 根据ID获取用户详情
     */
    UserListResponse getUserById(Long id);
    
    /**
     * 创建用户
     */
    void createUser(CreateUserRequest request);
    
    /**
     * 更新用户
     */
    void updateUser(Long id, UpdateUserRequest request);
    
    /**
     * 删除用户（逻辑删除）
     */
    void deleteUser(Long id);
    
    /**
     * 启用/禁用用户
     */
    void toggleUserStatus(Long id, Integer status);
    
    /**
     * 获取用户的角色ID列表
     */
    List<Long> getRoleIdsByUserId(Long userId);
    
    /**
     * 分配用户角色（支持多角色）
     */
    void assignUserRoles(Long userId, List<Long> roleIds);
}
