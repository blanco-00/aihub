package com.aihub.admin.dto.response;

import lombok.Data;

/**
 * 数据库状态响应
 */
@Data
public class DatabaseStatusResponse {
    /**
     * 数据库连接状态
     */
    private boolean connected;
    
    /**
     * 数据库是否存在
     */
    private boolean databaseExists;
    
    /**
     * 错误信息
     * 注意：表结构初始化由 Flyway 自动处理，无需手动检查
     */
    private String errorMessage;
}
