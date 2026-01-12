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
        boolean initialized = initializationService.isInitialized();
        return Result.success(initialized);
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
