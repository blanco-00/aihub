package com.aihub.service.impl;

import com.aihub.dto.InitSuperAdminDTO;
import com.aihub.entity.User;
import com.aihub.enums.UserRole;
import com.aihub.exception.BusinessException;
import com.aihub.mapper.UserMapper;
import com.aihub.service.InitializationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class InitializationServiceImpl implements InitializationService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public boolean isInitialized() {
        Long count = userMapper.countByRoleAndNotDeleted(UserRole.SUPER_ADMIN.getCode());
        return count != null && count > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSuperAdmin(InitSuperAdminDTO dto) {
        // 检查是否已存在超级管理员
        if (isInitialized()) {
            throw new BusinessException("系统已初始化，无法创建超级管理员");
        }
        
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(dto.getUsername());
        if (existingUser != null && existingUser.getIsDeleted() == 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        User existingEmail = userMapper.findByEmail(dto.getEmail());
        if (existingEmail != null && existingEmail.getIsDeleted() == 0) {
            throw new BusinessException("邮箱已存在");
        }
        
        // 创建超级管理员
        User admin = new User();
        admin.setUsername(dto.getUsername());
        admin.setEmail(dto.getEmail());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setRole(UserRole.SUPER_ADMIN.getCode());
        admin.setStatus(1);
        admin.setIsDeleted(0);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(admin);
        log.info("超级管理员创建成功：{}", dto.getUsername());
    }
}
