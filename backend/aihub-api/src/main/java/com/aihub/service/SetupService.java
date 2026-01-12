package com.aihub.service;

import com.aihub.dto.ConnectionTestResult;
import com.aihub.dto.DatabaseConfigDTO;

public interface SetupService {
    
    /**
     * 测试数据库连接
     * @param config 数据库配置
     * @return 连接测试结果
     */
    ConnectionTestResult testConnection(DatabaseConfigDTO config);
    
    /**
     * 保存数据库配置到本地文件
     * @param config 数据库配置
     */
    void saveConfig(DatabaseConfigDTO config);
    
    /**
     * 检查是否已配置数据库
     * @return true-已配置，false-未配置
     */
    boolean isConfigured();
}
