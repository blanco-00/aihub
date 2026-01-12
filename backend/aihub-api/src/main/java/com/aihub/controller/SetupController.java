package com.aihub.controller;

import com.aihub.dto.ConnectionTestResult;
import com.aihub.dto.DatabaseConfigDTO;
import com.aihub.dto.Result;
import com.aihub.service.SetupService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/setup")
public class SetupController {
    
    @Autowired
    private SetupService setupService;
    
    /**
     * 检查是否已配置数据库
     */
    @GetMapping("/status")
    public Result<Boolean> getSetupStatus() {
        boolean configured = setupService.isConfigured();
        return Result.success(configured);
    }
    
    /**
     * 测试数据库连接
     */
    @PostMapping("/test-connection")
    public Result<ConnectionTestResult> testConnection(@Valid @RequestBody DatabaseConfigDTO config) {
        try {
            log.info("测试数据库连接: host={}, port={}, database={}, username={}", 
                    config.getHost(), config.getPort(), config.getDatabase(), config.getUsername());
            ConnectionTestResult result = setupService.testConnection(config);
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试数据库连接异常", e);
            ConnectionTestResult errorResult = new ConnectionTestResult();
            errorResult.setSuccess(false);
            errorResult.setErrorMessage("测试连接时发生异常: " + e.getMessage());
            errorResult.setDatabaseExists(false);
            return Result.success(errorResult);
        }
    }
    
    /**
     * 保存数据库配置
     */
    @PostMapping("/save-config")
    public Result<Void> saveConfig(@Valid @RequestBody DatabaseConfigDTO config) {
        setupService.saveConfig(config);
        return Result.success();
    }
}
