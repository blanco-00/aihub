package com.aihub.admin.dto.response;

import lombok.Data;

/**
 * 系统健康检查响应DTO
 */
@Data
public class HealthCheckResponse {
    
    /**
     * 整体健康状态
     */
    private String status;
    
    /**
     * 应用状态
     */
    private ComponentStatus application;
    
    /**
     * 数据库状态
     */
    private ComponentStatus database;
    
    /**
     * Redis状态
     */
    private ComponentStatus redis;
    
    /**
     * 组件状态
     */
    @Data
    public static class ComponentStatus {
        /**
         * 状态：UP（正常）、DOWN（异常）
         */
        private String status;
        
        /**
         * 错误信息（如果状态为DOWN）
         */
        private String error;
    }
}
