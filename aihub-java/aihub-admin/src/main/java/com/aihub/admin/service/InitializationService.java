package com.aihub.admin.service;

import com.aihub.admin.dto.request.InitSuperAdminRequest;
import com.aihub.admin.dto.response.DatabaseStatusResponse;

/**
 * 系统初始化服务
 */
public interface InitializationService {
    
    /**
     * 检查系统是否已初始化（是否存在超级管理员）
     * @return true-已初始化，false-未初始化
     */
    boolean isInitialized();
    
    /**
     * 检查数据库状态
     * @return 数据库状态信息
     */
    DatabaseStatusResponse checkDatabaseStatus();
    
    /**
     * 创建超级管理员（仅在没有超级管理员时可用）
     * @param request 超级管理员信息
     * @throws com.aihub.common.web.exception.BusinessException 如果已存在超级管理员，抛出异常
     */
    void createSuperAdmin(InitSuperAdminRequest request);
}
