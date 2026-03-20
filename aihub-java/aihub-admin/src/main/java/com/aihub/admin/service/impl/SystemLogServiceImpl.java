package com.aihub.admin.service.impl;

import com.aihub.admin.dto.request.SystemLogListRequest;
import com.aihub.admin.dto.response.SystemLogResponse;
import com.aihub.admin.entity.SystemLog;
import com.aihub.admin.mapper.SystemLogMapper;
import com.aihub.admin.service.SystemLogService;
import com.aihub.common.web.dto.PageResult;
import com.aihub.common.web.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统日志服务实现
 */
@Slf4j
@Service
public class SystemLogServiceImpl implements SystemLogService {
    
    @Autowired
    private SystemLogMapper systemLogMapper;
    
    @Override
    @Async
    public void saveSystemLog(String level, String module, String message, String stackTrace, 
                             String ip, Long userId, String requestId) {
        try {
            SystemLog systemLog = new SystemLog();
            systemLog.setLevel(level);
            systemLog.setModule(module);
            systemLog.setMessage(message != null && message.length() > 5000 ? 
                message.substring(0, 5000) : message);
            systemLog.setStackTrace(stackTrace != null && stackTrace.length() > 10000 ? 
                stackTrace.substring(0, 10000) : stackTrace);
            systemLog.setIp(ip);
            systemLog.setUserId(userId);
            systemLog.setRequestId(requestId);
            systemLog.setLogTime(LocalDateTime.now());
            
            systemLogMapper.insert(systemLog);
            log.debug("保存系统日志成功: level={}, module={}", level, module);
        } catch (Exception e) {
            log.error("保存系统日志失败: level={}, module={}", level, module, e);
            // 不抛出异常，避免影响日志记录流程
        }
    }
    
    @Override
    public PageResult<SystemLogResponse> getSystemLogList(SystemLogListRequest request) {
        // 计算偏移量
        Long offset = (long) (request.getCurrent() - 1) * request.getSize();
        
        // 查询系统日志列表
        List<SystemLogResponse> logs = systemLogMapper.selectSystemLogList(request, offset, request.getSize());
        
        // 统计总数
        Long total = systemLogMapper.countSystemLogList(request);
        
        // 计算总页数
        Long pages = (total + request.getSize() - 1) / request.getSize();
        
        // 构建分页结果
        PageResult<SystemLogResponse> result = new PageResult<>();
        result.setRecords(logs);
        result.setTotal(total);
        result.setCurrent(request.getCurrent());
        result.setSize(request.getSize());
        result.setPages(pages);
        
        return result;
    }
    
    @Override
    public SystemLogResponse getSystemLogDetail(Long id) {
        SystemLog systemLog = systemLogMapper.selectById(id);
        if (systemLog == null) {
            throw new BusinessException("系统日志不存在");
        }
        
        SystemLogResponse response = new SystemLogResponse();
        BeanUtils.copyProperties(systemLog, response);
        return response;
    }
}
