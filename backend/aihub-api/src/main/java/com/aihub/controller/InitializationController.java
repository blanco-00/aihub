package com.aihub.controller;

import com.aihub.dto.InitSuperAdminDTO;
import com.aihub.dto.Result;
import com.aihub.service.InitializationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/init")
public class InitializationController {
    
    @Autowired
    private InitializationService initializationService;
    
    /**
     * 检查系统是否已初始化
     */
    @GetMapping("/status")
    public Result<Boolean> getInitStatus() {
        try {
            boolean initialized = initializationService.isInitialized();
            return Result.success(initialized);
        } catch (Exception e) {
            log.error("检查初始化状态失败", e);
            // 如果数据库连接失败，返回 false（未初始化）
            return Result.success(false);
        }
    }
    
    /**
     * 检查数据库状态
     */
    @GetMapping("/database/status")
    public Result<com.aihub.dto.DatabaseStatusDTO> getDatabaseStatus() {
        com.aihub.dto.DatabaseStatusDTO status = initializationService.checkDatabaseStatus();
        return Result.success(status);
    }
    
    /**
     * 初始化数据库表结构
     */
    @PostMapping("/database/init")
    public Result<Void> initializeDatabase() {
        initializationService.initializeDatabase();
        return Result.success();
    }
    
    /**
     * 创建超级管理员
     */
    @PostMapping("/super-admin")
    public Result<Void> createSuperAdmin(@Valid @RequestBody InitSuperAdminDTO dto) {
        initializationService.createSuperAdmin(dto);
        return Result.success();
    }
}
