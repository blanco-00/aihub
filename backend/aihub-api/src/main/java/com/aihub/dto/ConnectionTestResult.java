package com.aihub.dto;

import lombok.Data;

@Data
public class ConnectionTestResult {
    /**
     * 连接是否成功
     */
    private boolean success;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 数据库是否存在
     */
    private boolean databaseExists;
    
    /**
     * 连接耗时（毫秒）
     */
    private Long duration;
}
