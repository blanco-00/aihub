package com.aihub.admin.service.impl;

import com.aihub.admin.dto.response.HealthCheckResponse;
import com.aihub.admin.service.HealthCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * 系统健康检查服务实现类
 */
@Slf4j
@Service
public class HealthCheckServiceImpl implements HealthCheckService {
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public HealthCheckResponse checkHealth() {
        HealthCheckResponse response = new HealthCheckResponse();
        
        // 检查应用状态（总是UP，因为能执行到这里说明应用在运行）
        HealthCheckResponse.ComponentStatus appStatus = new HealthCheckResponse.ComponentStatus();
        appStatus.setStatus("UP");
        response.setApplication(appStatus);
        
        // 检查数据库连接
        HealthCheckResponse.ComponentStatus dbStatus = checkDatabase();
        response.setDatabase(dbStatus);
        
        // 检查Redis连接
        HealthCheckResponse.ComponentStatus redisStatus = checkRedis();
        response.setRedis(redisStatus);
        
        // 确定整体状态：如果所有组件都是UP，则整体为UP，否则为DOWN
        boolean allUp = "UP".equals(appStatus.getStatus()) 
                     && "UP".equals(dbStatus.getStatus()) 
                     && "UP".equals(redisStatus.getStatus());
        response.setStatus(allUp ? "UP" : "DOWN");
        
        return response;
    }
    
    /**
     * 检查数据库连接
     */
    private HealthCheckResponse.ComponentStatus checkDatabase() {
        HealthCheckResponse.ComponentStatus status = new HealthCheckResponse.ComponentStatus();
        
        if (dataSource == null) {
            status.setStatus("DOWN");
            status.setError("DataSource未配置");
            return status;
        }
        
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            // 执行简单查询检查连接
            jdbcTemplate.execute("SELECT 1");
            status.setStatus("UP");
        } catch (Exception e) {
            log.debug("数据库健康检查失败: {}", e.getMessage());
            status.setStatus("DOWN");
            status.setError(e.getMessage());
        }
        
        return status;
    }
    
    /**
     * 检查Redis连接
     */
    private HealthCheckResponse.ComponentStatus checkRedis() {
        HealthCheckResponse.ComponentStatus status = new HealthCheckResponse.ComponentStatus();
        
        if (redisTemplate == null) {
            status.setStatus("DOWN");
            status.setError("Redis未配置");
            return status;
        }
        
        try {
            // 执行ping操作检查连接
            String result = redisTemplate.getConnectionFactory().getConnection().ping();
            if ("PONG".equals(result)) {
                status.setStatus("UP");
            } else {
                status.setStatus("DOWN");
                status.setError("Redis ping返回异常: " + result);
            }
        } catch (Exception e) {
            log.debug("Redis健康检查失败: {}", e.getMessage());
            status.setStatus("DOWN");
            status.setError(e.getMessage());
        }
        
        return status;
    }
}
