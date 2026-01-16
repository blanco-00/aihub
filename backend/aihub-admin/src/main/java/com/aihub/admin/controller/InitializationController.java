package com.aihub.admin.controller;

import com.aihub.admin.dto.request.InitSuperAdminRequest;
import com.aihub.admin.dto.response.DatabaseStatusResponse;
import com.aihub.common.web.dto.Result;
import com.aihub.admin.service.InitializationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统初始化控制器
 */
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
    public Result<DatabaseStatusResponse> getDatabaseStatus() {
        DatabaseStatusResponse status = initializationService.checkDatabaseStatus();
        return Result.success(status);
    }
    
    /**
     * 创建超级管理员
     */
    @PostMapping("/super-admin")
    public Result<Void> createSuperAdmin(@Valid @RequestBody InitSuperAdminRequest request) {
        initializationService.createSuperAdmin(request);
        return Result.success();
    }
}
