package com.aihub.service;

import com.aihub.dto.OperationLogListRequest;
import com.aihub.dto.OperationLogResponse;
import com.aihub.dto.PageResult;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {
    
    /**
     * 记录操作日志
     */
    void recordOperation(Long userId, String username, String module, String operation, 
                        String method, String url, String params, String result, 
                        Integer status, Integer duration, HttpServletRequest request);
    
    /**
     * 查询操作日志列表（分页）
     */
    PageResult<OperationLogResponse> getOperationLogList(OperationLogListRequest request);
}
