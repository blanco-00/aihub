package com.aihub.service.impl;

import com.aihub.entity.LoginLog;
import com.aihub.mapper.LoginLogMapper;
import com.aihub.service.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 登录日志服务实现
 */
@Slf4j
@Service
public class LoginLogServiceImpl implements LoginLogService {
    
    @Autowired
    private LoginLogMapper loginLogMapper;
    
    @Override
    @Async
    public void recordLogin(Long userId, String username, Integer status, String message, HttpServletRequest request) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setStatus(status);
            loginLog.setMessage(message);
            loginLog.setLoginTime(LocalDateTime.now());
            
            // 获取IP地址
            if (request != null) {
                String ip = getClientIpAddress(request);
                loginLog.setIp(ip);
                
                // 获取User-Agent
                String userAgent = request.getHeader("User-Agent");
                loginLog.setUserAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent);
            }
            
            loginLogMapper.insert(loginLog);
            log.debug("记录登录日志成功: userId={}, username={}, status={}", userId, username, status);
        } catch (Exception e) {
            log.error("记录登录日志失败: userId={}, username={}, status={}", 
                userId, username, status, e);
            // 不抛出异常，避免影响登录流程
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
