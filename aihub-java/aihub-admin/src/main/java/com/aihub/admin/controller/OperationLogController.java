package com.aihub.admin.controller;

import com.aihub.common.web.dto.Result;
import com.aihub.common.web.dto.PageResult;

import com.aihub.admin.dto.request.OperationLogListRequest;
import com.aihub.admin.dto.response.OperationLogResponse;
import com.aihub.common.web.dto.Result;
import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {
    
    @Autowired
    private OperationLogService operationLogService;
    
    /**
     * 获取操作日志列表（分页、搜索、筛选）
     * 注意：此接口返回全量日志（所有用户的操作日志），用于监控模块
     * 如需查看个人日志，请使用 /api/auth/security-logs 接口
     */
    @GetMapping
    public Result<PageResult<OperationLogResponse>> getOperationLogList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        OperationLogListRequest request = new OperationLogListRequest();
        request.setCurrent(current);
        request.setSize(size);
        request.setUsername(username);
        request.setModule(module);
        request.setOperation(operation);
        request.setStatus(status);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        
        PageResult<OperationLogResponse> result = operationLogService.getOperationLogList(request);
        return Result.success(result);
    }
}
