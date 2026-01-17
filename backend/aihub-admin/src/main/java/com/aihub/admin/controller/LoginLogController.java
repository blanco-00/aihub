package com.aihub.admin.controller;

import com.aihub.common.web.dto.Result;
import com.aihub.common.web.dto.PageResult;

import com.aihub.admin.dto.request.LoginLogListRequest;
import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.dto.response.LoginLogResponse;
import com.aihub.common.web.dto.Result;
import com.aihub.admin.service.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/login-logs")
public class LoginLogController {
    
    @Autowired
    private LoginLogService loginLogService;
    
    /**
     * 获取登录日志列表（分页、搜索、筛选）
     * 注意：此接口返回全量日志（所有用户的登录日志），用于监控模块
     * 如需查看个人日志，请使用 /api/auth/security-logs 接口
     */
    @GetMapping
    public Result<PageResult<LoginLogResponse>> getLoginLogList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        LoginLogListRequest request = new LoginLogListRequest();
        request.setCurrent(current);
        request.setSize(size);
        request.setUsername(username);
        request.setIp(ip);
        request.setStatus(status);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        
        PageResult<LoginLogResponse> result = loginLogService.getLoginLogList(request);
        return Result.success(result);
    }
}
