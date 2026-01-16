package com.aihub.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 安全日志响应DTO（合并登录日志和操作日志）
 */
@Data
public class SecurityLogResponse {
    
    private Long id;
    
    /**
     * 日志类型：LOGIN-登录日志，OPERATION-操作日志
     */
    private String type;
    
    /**
     * 详情/摘要
     */
    private String summary;
    
    /**
     * IP地址
     */
    private String ip;
    
    /**
     * 地点
     */
    private String address;
    
    /**
     * 操作系统
     */
    private String system;
    
    /**
     * 浏览器类型
     */
    private String browser;
    
    /**
     * 操作时间
     */
    private LocalDateTime operatingTime;
}
