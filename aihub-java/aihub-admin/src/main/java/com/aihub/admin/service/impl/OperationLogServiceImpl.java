package com.aihub.admin.service.impl;

import com.aihub.admin.dto.request.OperationLogListRequest;
import com.aihub.admin.dto.response.OperationLogResponse;
import com.aihub.common.web.dto.PageResult;
import com.aihub.admin.entity.OperationLog;
import com.aihub.admin.mapper.OperationLogMapper;
import com.aihub.admin.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务实现
 */
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {
    
    @Autowired
    private OperationLogMapper operationLogMapper;
    
    @Override
    @Async
    public void recordOperation(Long userId, String username, String module, String operation, 
                               String method, String url, String params, String result, 
                               Integer status, Integer duration, HttpServletRequest request) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(userId);
            operationLog.setUsername(username);
            operationLog.setModule(module);
            operationLog.setOperation(operation);
            operationLog.setMethod(method);
            operationLog.setUrl(url != null && url.length() > 500 ? url.substring(0, 500) : url);
            operationLog.setParams(params);
            operationLog.setResult(result);
            operationLog.setStatus(status);
            operationLog.setDuration(duration);
            operationLog.setOperationTime(LocalDateTime.now());
            
            // 获取IP地址
            if (request != null) {
                String ip = getClientIpAddress(request);
                operationLog.setIp(ip);
            }
            
            operationLogMapper.insert(operationLog);
            log.debug("记录操作日志成功: userId={}, username={}, module={}, operation={}", 
                userId, username, module, operation);
        } catch (Exception e) {
            log.error("记录操作日志失败: userId={}, username={}, module={}, operation={}", 
                userId, username, module, operation, e);
            // 不抛出异常，避免影响业务流程
        }
    }
    
    @Override
    public PageResult<OperationLogResponse> getOperationLogList(OperationLogListRequest request) {
        // 计算偏移量
        Long offset = (long) (request.getCurrent() - 1) * request.getSize();
        
        // 查询操作日志列表
        List<OperationLogResponse> logs = operationLogMapper.selectOperationLogList(request, offset, request.getSize());
        
        // 统计总数
        Long total = operationLogMapper.countOperationLogList(request);
        
        // 计算总页数
        Long pages = (total + request.getSize() - 1) / request.getSize();
        
        // 构建分页结果
        PageResult<OperationLogResponse> result = new PageResult<>();
        result.setRecords(logs);
        result.setTotal(total);
        result.setCurrent(request.getCurrent());
        result.setSize(request.getSize());
        result.setPages(pages);
        
        return result;
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
