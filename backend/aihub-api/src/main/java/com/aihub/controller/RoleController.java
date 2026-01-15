package com.aihub.controller;

import com.aihub.dto.*;
import com.aihub.enums.UserRole;
import com.aihub.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    /**
     * 获取角色下拉选项（兼容旧接口，从枚举获取）
     */
    @GetMapping("/options")
    public Result<List<RoleOptionResponse>> getRoleOptions() {
        List<RoleOptionResponse> options = Arrays.stream(UserRole.values())
                .map(role -> {
                    RoleOptionResponse option = new RoleOptionResponse();
                    option.setId(role.getCode());
                    option.setName(role.getDescription());
                    return option;
                })
                .collect(Collectors.toList());
        return Result.success(options);
    }
    
    /**
     * 获取所有角色列表（从数据库获取）
     */
    @GetMapping
    public Result<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return Result.success(roles);
    }
    
    /**
     * 根据ID获取角色详情
     */
    @GetMapping("/{id}")
    public Result<RoleResponse> getRoleById(@PathVariable Long id) {
        RoleResponse role = roleService.getRoleById(id);
        return Result.success(role);
    }
    
    /**
     * 创建角色
     */
    @PostMapping
    public Result<Void> createRole(@RequestBody CreateRoleRequest request) {
        roleService.createRole(request);
        return Result.success();
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody UpdateRoleRequest request) {
        roleService.updateRole(id, request);
        return Result.success();
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }
}
