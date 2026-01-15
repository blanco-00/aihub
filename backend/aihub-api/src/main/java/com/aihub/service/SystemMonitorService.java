package com.aihub.service;

import com.aihub.dto.SystemMonitorResponse;

/**
 * 系统监控服务接口
 */
public interface SystemMonitorService {
    
    /**
     * 获取系统监控信息
     */
    SystemMonitorResponse getSystemMonitorInfo();
}
