package com.aihub.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录日志服务接口
 */
public interface LoginLogService {
    
    /**
     * 记录登录日志
     * @param userId 用户ID
     * @param username 用户名
     * @param status 登录状态（1-成功，0-失败）
     * @param message 登录消息
     * @param request HTTP请求对象（用于获取IP、User-Agent等信息）
     */
    void recordLogin(Long userId, String username, Integer status, String message, HttpServletRequest request);
}
