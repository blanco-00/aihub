package com.aihub.controller;

import com.aihub.dto.Result;
import com.aihub.dto.SystemMonitorResponse;
import com.aihub.service.SystemMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/monitor/system")
public class SystemMonitorController {
    
    @Autowired
    private SystemMonitorService systemMonitorService;
    
    /**
     * 获取系统监控信息
     * 
     * 注意：监控数据已缓存3秒，频繁请求不会提高数据更新频率
     * 建议前端请求间隔不少于3秒，避免不必要的性能开销
     */
    @GetMapping
    public Result<SystemMonitorResponse> getSystemMonitorInfo() {
        SystemMonitorResponse info = systemMonitorService.getSystemMonitorInfo();
        return Result.success(info);
    }
}
