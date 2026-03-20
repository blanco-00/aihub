package com.aihub.admin.controller;

import com.aihub.admin.dto.response.HealthCheckResponse;
import com.aihub.admin.dto.response.SystemMonitorResponse;
import com.aihub.admin.service.HealthCheckService;
import com.aihub.admin.service.SystemMonitorService;
import com.aihub.common.web.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统监控控制器
 * 
 * 性能优化说明：
 * 1. 监控数据已缓存3秒，避免频繁计算
 * 2. 建议前端请求间隔不少于3秒
 * 3. 如需更频繁的监控，可考虑使用WebSocket推送
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor")
public class SystemMonitorController {
    
    @Autowired
    private SystemMonitorService systemMonitorService;
    
    @Autowired
    private HealthCheckService healthCheckService;
    
    /**
     * 获取系统监控信息
     * 
     * 注意：监控数据已缓存3秒，频繁请求不会提高数据更新频率
     * 建议前端请求间隔不少于3秒，避免不必要的性能开销
     */
    @GetMapping("/system")
    public Result<SystemMonitorResponse> getSystemMonitorInfo() {
        SystemMonitorResponse info = systemMonitorService.getSystemMonitorInfo();
        return Result.success(info);
    }
    
    /**
     * 系统健康检查接口
     * 
     * 用于负载均衡器、Kubernetes等场景的健康检查
     * 检查应用、数据库、Redis的连接状态
     * 
     * @return 健康检查结果，HTTP状态码：200（健康）或 503（不健康）
     */
    @GetMapping("/health")
    public Result<HealthCheckResponse> healthCheck() {
        HealthCheckResponse health = healthCheckService.checkHealth();
        
        // 如果整体状态为DOWN，返回503状态码
        if ("DOWN".equals(health.getStatus())) {
            Result<HealthCheckResponse> result = Result.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "系统不健康");
            result.setData(health);
            return result;
        }
        
        return Result.success(health);
    }
}
