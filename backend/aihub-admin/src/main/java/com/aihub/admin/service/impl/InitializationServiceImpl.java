package com.aihub.admin.service.impl;

import com.aihub.admin.dto.request.InitSuperAdminRequest;
import com.aihub.admin.dto.response.DatabaseStatusResponse;
import com.aihub.admin.entity.User;
import com.aihub.admin.enums.UserRole;
import com.aihub.admin.mapper.UserMapper;
import com.aihub.admin.service.InitializationService;
import com.aihub.common.web.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDateTime;

@Slf4j
@Service
public class InitializationServiceImpl implements InitializationService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public boolean isInitialized() {
        try {
            Long count = userMapper.countByRoleAndNotDeleted(UserRole.SUPER_ADMIN.getCode());
            return count != null && count > 0;
        } catch (Exception e) {
            log.debug("查询超级管理员失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public DatabaseStatusResponse checkDatabaseStatus() {
        DatabaseStatusResponse status = new DatabaseStatusResponse();

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            jdbcTemplate.execute("SELECT 1");
            status.setConnected(true);
            status.setDatabaseExists(true);

        } catch (Exception e) {
            log.debug("检查数据库状态失败: {}", e.getMessage());
            status.setConnected(false);
            status.setDatabaseExists(false);

            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Unknown database")) {
                status.setErrorMessage("数据库不存在，请先创建数据库 'aihub'");
            } else if (errorMsg != null && errorMsg.contains("Access denied")) {
                status.setErrorMessage("数据库连接被拒绝，请检查用户名和密码");
            } else if (errorMsg != null && errorMsg.contains("Communications link failure")) {
                status.setErrorMessage("无法连接到数据库服务器，请检查服务是否启动");
            } else {
                status.setErrorMessage("数据库连接失败: " + (errorMsg != null ? errorMsg : "未知错误"));
            }
        }

        return status;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSuperAdmin(InitSuperAdminRequest request) {
        if (isInitialized()) {
            throw new BusinessException("系统已初始化，无法创建超级管理员");
        }

        User existingUser = userMapper.findByUsername(request.getUsername());
        if (existingUser != null && existingUser.getIsDeleted() == 0) {
            throw new BusinessException("用户名已存在");
        }

        User existingEmail = userMapper.findByEmail(request.getEmail());
        if (existingEmail != null && existingEmail.getIsDeleted() == 0) {
            throw new BusinessException("邮箱已存在");
        }

        User admin = new User();
        admin.setUsername(request.getUsername());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole(UserRole.SUPER_ADMIN.getCode());
        admin.setStatus(1);
        admin.setIsDeleted(0);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(admin);
        log.info("超级管理员创建成功：{}", request.getUsername());
    }
}
