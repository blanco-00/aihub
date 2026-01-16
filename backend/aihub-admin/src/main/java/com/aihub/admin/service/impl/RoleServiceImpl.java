package com.aihub.admin.service.impl;

import com.aihub.admin.dto.request.CreateRoleRequest;
import com.aihub.admin.dto.response.RoleResponse;
import com.aihub.admin.dto.request.UpdateRoleRequest;
import com.aihub.admin.entity.Role;
import com.aihub.common.web.exception.BusinessException;
import com.aihub.admin.mapper.RoleMapper;
import com.aihub.admin.mapper.RoleMenuMapper;
import com.aihub.admin.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private RoleMenuMapper roleMenuMapper;
    
    @Override
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleMapper.selectAllEnabled();
        return roles.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public RoleResponse getRoleById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null || role.getIsDeleted() == 1) {
            log.warn("查询角色失败，角色不存在: id={}", id);
            throw new BusinessException("角色不存在");
        }
        return convertToResponse(role);
    }
    
    @Override
    @Transactional
    public void createRole(CreateRoleRequest request) {
        // 检查角色代码是否已存在
        Role existingRole = roleMapper.selectByCode(request.getCode());
        if (existingRole != null) {
            log.warn("创建角色失败，角色代码已存在: code={}, existingRoleId={}", 
                request.getCode(), existingRole.getId());
            throw new BusinessException("角色代码已存在");
        }
        
        Role role = new Role();
        BeanUtils.copyProperties(request, role);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setIsDeleted(0);
        
        roleMapper.insert(role);
        log.info("创建角色成功: id={}, code={}, name={}", role.getId(), role.getCode(), role.getName());
    }
    
    @Override
    @Transactional
    public void updateRole(Long id, UpdateRoleRequest request) {
        Role role = roleMapper.selectById(id);
        if (role == null || role.getIsDeleted() == 1) {
            log.warn("更新角色失败，角色不存在: id={}", id);
            throw new BusinessException("角色不存在");
        }
        
        // 不允许修改系统内置角色（SUPER_ADMIN、ADMIN、USER）的代码和名称
        if ("SUPER_ADMIN".equals(role.getCode()) || "ADMIN".equals(role.getCode()) || "USER".equals(role.getCode())) {
            // 只允许修改描述和状态
            if (request.getDescription() != null) {
                role.setDescription(request.getDescription());
            }
            if (request.getStatus() != null) {
                role.setStatus(request.getStatus());
            }
        } else {
            // 自定义角色可以修改所有字段
            if (request.getName() != null) {
                role.setName(request.getName());
            }
            if (request.getDescription() != null) {
                role.setDescription(request.getDescription());
            }
            if (request.getStatus() != null) {
                role.setStatus(request.getStatus());
            }
        }
        
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.updateById(role);
        log.info("更新角色成功: id={}, code={}, name={}", role.getId(), role.getCode(), role.getName());
    }
    
    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null || role.getIsDeleted() == 1) {
            log.warn("删除角色失败，角色不存在: id={}", id);
            throw new BusinessException("角色不存在");
        }
        
        // 不允许删除系统内置角色
        if ("SUPER_ADMIN".equals(role.getCode()) || "ADMIN".equals(role.getCode()) || "USER".equals(role.getCode())) {
            log.warn("删除角色失败，系统内置角色不允许删除: roleId={}, code={}, name={}", 
                id, role.getCode(), role.getName());
            throw new BusinessException("系统内置角色不允许删除");
        }
        
        // 逻辑删除
        role.setIsDeleted(1);
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.updateById(role);
        log.info("删除角色成功: id={}, code={}, name={}", role.getId(), role.getCode(), role.getName());
    }
    
    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getIsDeleted() == 1) {
            log.warn("查询角色菜单失败，角色不存在: roleId={}", roleId);
            throw new BusinessException("角色不存在");
        }
        
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }
    
    @Override
    @Transactional
    public void saveRoleMenus(Long roleId, List<Long> menuIds) {
        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getIsDeleted() == 1) {
            log.warn("保存角色菜单失败，角色不存在: roleId={}", roleId);
            throw new BusinessException("角色不存在");
        }
        
        // 先删除原有的关联
        roleMenuMapper.deleteByRoleId(roleId);
        
        // 如果有菜单ID，批量插入新的关联
        if (menuIds != null && !menuIds.isEmpty()) {
            roleMenuMapper.batchInsert(roleId, menuIds);
        }
        
        log.info("保存角色菜单关联成功: roleId={}, menuCount={}", roleId, menuIds != null ? menuIds.size() : 0);
    }
    
    /**
     * 转换为响应DTO
     */
    private RoleResponse convertToResponse(Role role) {
        RoleResponse response = new RoleResponse();
        BeanUtils.copyProperties(role, response);
        return response;
    }
}
