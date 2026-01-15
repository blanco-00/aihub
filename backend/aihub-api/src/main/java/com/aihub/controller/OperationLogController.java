package com.aihub.controller;

import com.aihub.dto.OperationLogListRequest;
import com.aihub.dto.OperationLogResponse;
import com.aihub.dto.PageResult;
import com.aihub.dto.Result;
import com.aihub.service.OperationLogService;
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
