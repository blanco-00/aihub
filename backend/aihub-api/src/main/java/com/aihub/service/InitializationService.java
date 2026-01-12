package com.aihub.service;

import com.aihub.dto.InitSuperAdminDTO;

public interface InitializationService {
    
    /**
     * 检查系统是否已初始化（是否存在超级管理员）
     * @return true-已初始化，false-未初始化
     */
    boolean isInitialized();
    
    /**
     * 创建超级管理员（仅在没有超级管理员时可用）
     * @param dto 超级管理员信息
     * @throws com.aihub.exception.BusinessException 如果已存在超级管理员，抛出异常
     */
    void createSuperAdmin(InitSuperAdminDTO dto);
}
