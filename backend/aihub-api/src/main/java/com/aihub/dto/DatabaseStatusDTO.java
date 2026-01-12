package com.aihub.dto;

import lombok.Data;

@Data
public class DatabaseStatusDTO {
    /**
     * 数据库连接状态
     */
    private boolean connected;
    
    /**
     * 数据库是否存在
     */
    private boolean databaseExists;
    
    /**
     * 表是否已初始化
     */
    private boolean tablesInitialized;
    
    /**
     * 错误信息
     */
    private String errorMessage;
}
