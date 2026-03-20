package com.aihub.admin.controller;

import com.aihub.admin.dto.request.InitSuperAdminRequest;
import com.aihub.admin.dto.response.DatabaseStatusResponse;
import com.aihub.admin.service.InitializationService;
import com.aihub.common.web.dto.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/init")
public class InitController {

    @Autowired
    private InitializationService initializationService;

    @GetMapping("/status")
    public Result<Boolean> getInitStatus() {
        boolean initialized = initializationService.isInitialized();
        return Result.success(initialized);
    }

    @GetMapping("/database/status")
    public Result<DatabaseStatusResponse> getDatabaseStatus() {
        DatabaseStatusResponse status = initializationService.checkDatabaseStatus();
        return Result.success(status);
    }

    @PostMapping("/super-admin")
    public Result<Void> createSuperAdmin(@Valid @RequestBody InitSuperAdminRequest request) {
        initializationService.createSuperAdmin(request);
        return Result.success(null);
    }
}
