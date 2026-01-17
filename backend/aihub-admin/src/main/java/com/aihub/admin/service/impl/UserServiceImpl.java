package com.aihub.admin.service.impl;

import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.request.UserListRequest;
import com.aihub.admin.dto.request.CreateUserRequest;
import com.aihub.admin.dto.request.UpdateUserRequest;
import com.aihub.admin.dto.response.UserListResponse;
import com.aihub.admin.entity.User;
import com.aihub.admin.enums.UserRole;
import com.aihub.common.web.exception.BusinessException;
import com.aihub.admin.mapper.UserMapper;
import com.aihub.admin.mapper.UserRoleMapper;
import com.aihub.admin.mapper.RoleMapper;
import com.aihub.admin.entity.Role;
import com.aihub.admin.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private com.aihub.admin.mapper.DepartmentMapper departmentMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public PageResult<UserListResponse> getUserList(UserListRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 计算偏移量
        Long offset = (long) (request.getCurrent() - 1) * request.getSize();
        
        // 查询用户列表
        long queryStart = System.currentTimeMillis();
        List<UserListResponse> users = userMapper.selectUserList(
            request.getKeyword(),
            request.getPhone(),
            request.getRole(),
            request.getStatus(),
            request.getDepartmentId(),
            offset,
            request.getSize()
        );
        long queryTime = System.currentTimeMillis() - queryStart;
        if (queryTime > 500) {
            log.warn("[性能警告] 查询用户列表耗时: {}ms, 结果数量: {}", queryTime, users.size());
        }
        
        // 设置角色描述和所有角色信息
        users.forEach(user -> {
            try {
                UserRole role = UserRole.valueOf(user.getRole());
                user.setRoleDescription(role.getDescription());
            } catch (Exception e) {
                user.setRoleDescription("未知角色");
            }
            
            // 查询用户的所有角色ID和名称
            List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
            user.setRoleIds(roleIds);
            
            // 查询角色名称列表
            if (roleIds != null && !roleIds.isEmpty()) {
                List<String> roleNames = roleIds.stream()
                    .map(roleId -> {
                        Role role = roleMapper.selectById(roleId);
                        return role != null ? role.getName() : null;
                    })
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
                user.setRoleNames(roleNames);
            } else {
                user.setRoleNames(new ArrayList<>());
            }
        });
        
        // 统计总数
        long countStart = System.currentTimeMillis();
        Long total = userMapper.countUserList(
            request.getKeyword(),
            request.getPhone(),
            request.getRole(),
            request.getStatus(),
            request.getDepartmentId()
        );
        long countTime = System.currentTimeMillis() - countStart;
        if (countTime > 500) {
            log.warn("[性能警告] 统计用户总数耗时: {}ms, 总数: {}", countTime, total);
        }
        
        // 计算总页数
        Long pages = (total + request.getSize() - 1) / request.getSize();
        
        // 构建分页结果
        PageResult<UserListResponse> result = new PageResult<>();
        result.setRecords(users);
        result.setTotal(total);
        result.setCurrent(request.getCurrent());
        result.setSize(request.getSize());
        result.setPages(pages);
        
        long totalTime = System.currentTimeMillis() - startTime;
        if (totalTime > 1000) {
            log.warn("[性能警告] 用户列表查询总耗时: {}ms, 查询耗时: {}ms, 统计耗时: {}ms", 
                    totalTime, queryTime, countTime);
        }
        
        return result;
    }
    
    @Override
    public UserListResponse getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        UserListResponse response = new UserListResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAvatar(user.getAvatar());
        response.setDescription(user.getDescription());
        response.setRole(user.getRole());
        response.setDepartmentId(user.getDepartmentId() != null ? user.getDepartmentId() : 0L);
        response.setStatus(user.getStatus());
        response.setRemark(user.getRemark());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        
        // 设置角色描述
        try {
            UserRole role = UserRole.valueOf(user.getRole());
            response.setRoleDescription(role.getDescription());
        } catch (Exception e) {
            response.setRoleDescription("未知角色");
        }
        
        // 设置部门名称（如果有部门ID）
        if (user.getDepartmentId() != null && user.getDepartmentId() > 0) {
            try {
                com.aihub.admin.entity.Department dept = departmentMapper.selectById(user.getDepartmentId());
                response.setDepartmentName(dept != null ? dept.getName() : "未知部门");
            } catch (Exception e) {
                response.setDepartmentName("未知部门");
            }
        } else {
            response.setDepartmentName("未分配");
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(CreateUserRequest request) {
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(request.getUsername());
        if (existingUser != null && existingUser.getIsDeleted() == 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        User existingEmail = userMapper.findByEmail(request.getEmail());
        if (existingEmail != null && existingEmail.getIsDeleted() == 0) {
            throw new BusinessException("邮箱已被注册");
        }
        
        // 验证角色
        try {
            UserRole.valueOf(request.getRole());
        } catch (Exception e) {
            throw new BusinessException("无效的角色");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setDepartmentId(request.getDepartmentId() != null ? request.getDepartmentId() : 0L);
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        user.setRemark(request.getRemark());
        user.setIsDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        log.info("用户创建成功: username={}, role={}", user.getUsername(), user.getRole());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UpdateUserRequest request) {
        // 查找用户
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查是否为最后一个超级管理员
        if (UserRole.SUPER_ADMIN.getCode().equals(user.getRole())) {
            Long superAdminCount = userMapper.countByRoleAndNotDeleted(UserRole.SUPER_ADMIN.getCode());
            if (superAdminCount <= 1) {
                // 如果是最后一个超级管理员，不允许修改角色或删除
                if (!UserRole.SUPER_ADMIN.getCode().equals(request.getRole())) {
                    throw new BusinessException("不能修改最后一个超级管理员的角色");
                }
            }
        }
        
        // 检查用户名是否已被其他用户使用
        User existingUser = userMapper.findByUsername(request.getUsername());
        if (existingUser != null && existingUser.getIsDeleted() == 0 && !existingUser.getId().equals(id)) {
            throw new BusinessException("用户名已被使用");
        }
        
        // 检查邮箱是否已被其他用户使用
        User existingEmail = userMapper.findByEmail(request.getEmail());
        if (existingEmail != null && existingEmail.getIsDeleted() == 0 && !existingEmail.getId().equals(id)) {
            throw new BusinessException("邮箱已被使用");
        }
        
        // 验证角色
        try {
            UserRole.valueOf(request.getRole());
        } catch (Exception e) {
            throw new BusinessException("无效的角色");
        }
        
        // 更新用户信息
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        user.setRole(request.getRole());
        // 更新部门ID：如果请求中提供了 departmentId（包括0），则更新
        // 0 表示未分配部门，null 表示不更新（保持原值）
        if (request.getDepartmentId() != null) {
            user.setDepartmentId(request.getDepartmentId());
        } else {
            // 如果前端传递的是 null 或未传递，保持原值不变
            // 但通常前端会传递 0 表示未分配，所以这里主要是处理 null 的情况
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            user.setRemark(request.getRemark());
        }
        
        // 如果提供了新密码，则更新密码
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("用户更新成功: id={}, username={}", id, user.getUsername());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        // 查找用户
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查是否为最后一个超级管理员
        if (UserRole.SUPER_ADMIN.getCode().equals(user.getRole())) {
            Long superAdminCount = userMapper.countByRoleAndNotDeleted(UserRole.SUPER_ADMIN.getCode());
            if (superAdminCount <= 1) {
                throw new BusinessException("不能删除最后一个超级管理员");
            }
        }
        
        // 逻辑删除
        user.setIsDeleted(1);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("用户删除成功: id={}, username={}", id, user.getUsername());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleUserStatus(Long id, Integer status) {
        // 查找用户
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查是否为最后一个超级管理员
        if (UserRole.SUPER_ADMIN.getCode().equals(user.getRole()) && status == 0) {
            Long superAdminCount = userMapper.countByRoleAndNotDeleted(UserRole.SUPER_ADMIN.getCode());
            if (superAdminCount <= 1) {
                throw new BusinessException("不能禁用最后一个超级管理员");
            }
        }
        
        // 更新状态
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("用户状态更新成功: id={}, username={}, status={}", id, user.getUsername(), status);
    }
    
    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            log.warn("查询用户角色失败，用户不存在: userId={}", userId);
            throw new BusinessException("用户不存在");
        }
        
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            log.warn("分配用户角色失败，用户不存在: userId={}", userId);
            throw new BusinessException("用户不存在");
        }
        
        // 验证角色ID是否有效
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                Role role = roleMapper.selectById(roleId);
                if (role == null || role.getIsDeleted() == 1) {
                    log.warn("分配用户角色失败，角色不存在: userId={}, roleId={}", userId, roleId);
                    throw new BusinessException("角色不存在: " + roleId);
                }
            }
        }
        
        // 检查超级管理员保护：如果用户是最后一个超级管理员，必须保留超级管理员角色
        List<Long> currentRoleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        boolean hasSuperAdminRole = false;
        if (currentRoleIds != null && !currentRoleIds.isEmpty()) {
            for (Long roleId : currentRoleIds) {
                Role role = roleMapper.selectById(roleId);
                if (role != null && "SUPER_ADMIN".equals(role.getCode())) {
                    hasSuperAdminRole = true;
                    break;
                }
            }
        }
        
        // 如果用户当前有超级管理员角色，检查是否要移除
        if (hasSuperAdminRole) {
            boolean willHaveSuperAdminRole = false;
            if (roleIds != null && !roleIds.isEmpty()) {
                for (Long roleId : roleIds) {
                    Role role = roleMapper.selectById(roleId);
                    if (role != null && "SUPER_ADMIN".equals(role.getCode())) {
                        willHaveSuperAdminRole = true;
                        break;
                    }
                }
            }
            
            // 如果要移除超级管理员角色，检查是否为最后一个超级管理员
            if (!willHaveSuperAdminRole) {
                Long superAdminCount = userMapper.countByRoleAndNotDeleted(UserRole.SUPER_ADMIN.getCode());
                if (superAdminCount <= 1) {
                    log.warn("分配用户角色失败，不能移除最后一个超级管理员的角色: userId={}", userId);
                    throw new BusinessException("不能移除最后一个超级管理员的角色");
                }
            }
        }
        
        // 先删除原有的关联
        userRoleMapper.deleteByUserId(userId);
        
        // 如果有角色ID，批量插入新的关联
        if (roleIds != null && !roleIds.isEmpty()) {
            userRoleMapper.batchInsert(userId, roleIds);
            
            // 保持向后兼容：更新 user.role 字段为主角色（第一个角色或最高权限角色）
            // 优先级：SUPER_ADMIN > ADMIN > USER
            String primaryRole = null;
            for (Long roleId : roleIds) {
                Role role = roleMapper.selectById(roleId);
                if (role != null) {
                    String roleCode = role.getCode();
                    if ("SUPER_ADMIN".equals(roleCode)) {
                        primaryRole = roleCode;
                        break; // SUPER_ADMIN 优先级最高，直接使用
                    } else if ("ADMIN".equals(roleCode) && !"SUPER_ADMIN".equals(primaryRole)) {
                        primaryRole = roleCode;
                    } else if ("USER".equals(roleCode) && primaryRole == null) {
                        primaryRole = roleCode;
                    }
                }
            }
            
            // 如果找到了主角色，更新 user.role 字段
            if (primaryRole != null) {
                user.setRole(primaryRole);
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(user);
            }
        } else {
            // 如果没有分配任何角色，保持 user.role 字段不变（向后兼容）
            // 或者可以设置为默认角色 USER
            // 这里选择保持原值不变，避免影响现有逻辑
        }
        
        log.info("分配用户角色成功: userId={}, roleCount={}", userId, roleIds != null ? roleIds.size() : 0);
    }
}
