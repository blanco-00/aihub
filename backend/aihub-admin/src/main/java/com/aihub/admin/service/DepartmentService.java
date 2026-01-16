package com.aihub.admin.service;

import com.aihub.admin.dto.request.CreateDepartmentRequest;
import com.aihub.admin.dto.response.DepartmentResponse;
import com.aihub.admin.dto.request.UpdateDepartmentRequest;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DepartmentService {
    
    /**
     * 获取部门树（所有部门）
     */
    List<DepartmentResponse> getDepartmentTree();
    
    /**
     * 根据ID获取部门详情
     */
    DepartmentResponse getDepartmentById(Long id);
    
    /**
     * 创建部门
     */
    void createDepartment(CreateDepartmentRequest request);
    
    /**
     * 更新部门
     */
    void updateDepartment(Long id, UpdateDepartmentRequest request);
    
    /**
     * 删除部门（逻辑删除）
     */
    void deleteDepartment(Long id);
}
