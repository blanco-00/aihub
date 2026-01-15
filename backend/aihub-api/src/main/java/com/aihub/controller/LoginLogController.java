package com.aihub.controller;

import com.aihub.dto.LoginLogListRequest;
import com.aihub.dto.PageResult;
import com.aihub.dto.LoginLogResponse;
import com.aihub.dto.Result;
import com.aihub.service.LoginLogService;
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
