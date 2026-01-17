package com.aihub.admin.service;

import com.aihub.admin.dto.request.SystemLogListRequest;
import com.aihub.admin.dto.response.SystemLogResponse;
import com.aihub.common.web.dto.PageResult;

/**
 * 系统日志服务接口
 */
public interface SystemLogService {
    
    /**
     * 保存系统日志
     */
    void saveSystemLog(String level, String module, String message, String stackTrace, 
                      String ip, Long userId, String requestId);
    
    /**
     * 查询系统日志列表（分页）
     */
    PageResult<SystemLogResponse> getSystemLogList(SystemLogListRequest request);
    
    /**
     * 根据ID查询系统日志详情
     */
    SystemLogResponse getSystemLogDetail(Long id);
}
