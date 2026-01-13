package com.aihub.service.impl;

import com.aihub.dto.*;
import com.aihub.entity.User;
import com.aihub.enums.UserRole;
import com.aihub.exception.BusinessException;
import com.aihub.mapper.UserMapper;
import com.aihub.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public PageResult<UserListResponse> getUserList(UserListRequest request) {
        // 计算偏移量
        Long offset = (long) (request.getCurrent() - 1) * request.getSize();
        
        // 查询用户列表
        List<UserListResponse> users = userMapper.selectUserList(
            request.getKeyword(),
            request.getRole(),
            request.getStatus(),
            offset,
            request.getSize()
        );
        
        // 设置角色描述
        users.forEach(user -> {
            try {
                UserRole role = UserRole.valueOf(user.getRole());
                user.setRoleDescription(role.getDescription());
            } catch (Exception e) {
                user.setRoleDescription("未知角色");
            }
        });
        
        // 统计总数
        Long total = userMapper.countUserList(
            request.getKeyword(),
            request.getRole(),
            request.getStatus()
        );
        
        // 计算总页数
        Long pages = (total + request.getSize() - 1) / request.getSize();
        
        // 构建分页结果
        PageResult<UserListResponse> result = new PageResult<>();
        result.setRecords(users);
        result.setTotal(total);
        result.setCurrent(request.getCurrent());
        result.setSize(request.getSize());
        result.setPages(pages);
        
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
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        
        // 设置角色描述
        try {
            UserRole role = UserRole.valueOf(user.getRole());
            response.setRoleDescription(role.getDescription());
        } catch (Exception e) {
            response.setRoleDescription("未知角色");
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
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
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
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
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
}
