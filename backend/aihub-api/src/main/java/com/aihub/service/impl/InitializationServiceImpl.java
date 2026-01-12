package com.aihub.service.impl;

import com.aihub.dto.DatabaseStatusDTO;
import com.aihub.dto.InitSuperAdminDTO;
import com.aihub.entity.User;
import com.aihub.enums.UserRole;
import com.aihub.exception.BusinessException;
import com.aihub.mapper.UserMapper;
import com.aihub.service.InitializationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class InitializationServiceImpl implements InitializationService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public boolean isInitialized() {
        try {
            // 先检查数据库连接状态（不抛出异常）
            DatabaseStatusDTO dbStatus = checkDatabaseStatus();
            if (!dbStatus.isConnected() || !dbStatus.isDatabaseExists()) {
                log.debug("数据库未连接或不存在，系统未初始化");
                return false;
            }
            
            // 数据库连接正常，检查是否存在超级管理员
            try {
                Long count = userMapper.countByRoleAndNotDeleted(UserRole.SUPER_ADMIN.getCode());
                return count != null && count > 0;
            } catch (Exception e) {
                // 如果查询失败（可能是表不存在），返回 false
                log.debug("查询超级管理员失败: {}", e.getMessage());
                return false;
            }
        } catch (Exception e) {
            // 检查数据库状态失败，返回 false
            log.debug("检查初始化状态失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public DatabaseStatusDTO checkDatabaseStatus() {
        DatabaseStatusDTO status = new DatabaseStatusDTO();
        
        try {
            // 尝试获取数据库连接
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // 检查数据库连接
            jdbcTemplate.execute("SELECT 1");
            status.setConnected(true);
            status.setDatabaseExists(true);
            
            // 检查表是否存在
            try {
                String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'user'";
                Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
                status.setTablesInitialized(count != null && count > 0);
            } catch (Exception e) {
                log.debug("检查表是否存在失败: {}", e.getMessage());
                status.setTablesInitialized(false);
            }
            
        } catch (Exception e) {
            log.debug("检查数据库状态失败: {}", e.getMessage());
            status.setConnected(false);
            status.setDatabaseExists(false);
            status.setTablesInitialized(false);
            
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
    public void initializeDatabase() {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // 读取 SQL 脚本（使用 Flyway 迁移脚本）
            ClassPathResource resource = new ClassPathResource("db/migration/V1.0.0__init_tables.sql");
            InputStream inputStream = resource.getInputStream();
            String sql = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            
            // 分割 SQL 语句（按分号分割，但需要处理多行语句）
            // 先移除单行注释
            String cleanedSql = sql.replaceAll("--.*", "");
            // 移除多行注释
            cleanedSql = cleanedSql.replaceAll("/\\*[\\s\\S]*?\\*/", "");
            
            // 按分号分割，但保留 CREATE TABLE 等完整语句
            String[] statements = cleanedSql.split(";");
            
            int executedCount = 0;
            // 执行每个 SQL 语句
            for (String statement : statements) {
                String trimmed = statement.trim();
                // 跳过空语句
                if (trimmed.isEmpty()) {
                    continue;
                }
                
                // 确保语句以分号结尾（JdbcTemplate 需要）
                if (!trimmed.endsWith(";")) {
                    trimmed = trimmed + ";";
                }
                
                try {
                    log.info("执行 SQL: {}", trimmed.substring(0, Math.min(200, trimmed.length())));
                    jdbcTemplate.execute(trimmed);
                    executedCount++;
                    log.info("执行 SQL 成功，已执行 {} 条语句", executedCount);
                } catch (Exception e) {
                    String errorMsg = e.getMessage();
                    // 忽略已存在的表错误（MySQL 使用 CREATE TABLE IF NOT EXISTS）
                    if (errorMsg != null && (errorMsg.contains("already exists") || 
                                             errorMsg.contains("Duplicate") ||
                                             errorMsg.contains("Table") && errorMsg.contains("already exists"))) {
                        log.info("表已存在，跳过: {}", trimmed.substring(0, Math.min(100, trimmed.length())));
                        continue;
                    }
                    log.error("执行 SQL 失败: {}", errorMsg);
                    log.error("失败的 SQL: {}", trimmed);
                    throw new BusinessException("执行 SQL 失败: " + errorMsg);
                }
            }
            
            log.info("数据库表初始化成功，共执行 {} 条 SQL 语句", executedCount);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("数据库初始化失败", e);
            throw new BusinessException("数据库初始化失败: " + e.getMessage());
        }
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
