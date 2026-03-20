package com.aihub.admin.service;

import com.aihub.admin.dto.response.HealthCheckResponse;

/**
 * 系统健康检查服务接口
 */
public interface HealthCheckService {
    
    /**
     * 执行健康检查
     * 
     * @return 健康检查结果
     */
    HealthCheckResponse checkHealth();
}
