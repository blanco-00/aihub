package com.aihub.admin.service;

import com.aihub.admin.dto.response.SystemMonitorResponse;

/**
 * 系统监控服务接口
 */
public interface SystemMonitorService {
    
    /**
     * 获取系统监控信息
     */
    SystemMonitorResponse getSystemMonitorInfo();
}
