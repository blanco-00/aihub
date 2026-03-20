package com.aihub.admin.controller;

import com.aihub.admin.dto.request.SystemLogListRequest;
import com.aihub.admin.dto.response.SystemLogResponse;
import com.aihub.admin.service.SystemLogService;
import com.aihub.common.web.dto.PageResult;
import com.aihub.common.web.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统日志管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system-logs")
public class SystemLogController {
    
    @Autowired
    private SystemLogService systemLogService;
    
    /**
     * 获取系统日志列表（分页、搜索、筛选）
     * 注意：此接口返回全量日志（所有系统日志），用于监控模块
     * 系统日志不区分用户，记录的是系统级别的错误和事件
     */
    @GetMapping
    public Result<PageResult<SystemLogResponse>> getSystemLogList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        SystemLogListRequest request = new SystemLogListRequest();
        request.setCurrent(current);
        request.setSize(size);
        request.setLevel(level);
        request.setModule(module);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        
        PageResult<SystemLogResponse> result = systemLogService.getSystemLogList(request);
        return Result.success(result);
    }
    
    /**
     * 根据ID获取系统日志详情
     */
    @GetMapping("/{id}")
    public Result<SystemLogResponse> getSystemLogDetail(@PathVariable Long id) {
        SystemLogResponse result = systemLogService.getSystemLogDetail(id);
        return Result.success(result);
    }
}
