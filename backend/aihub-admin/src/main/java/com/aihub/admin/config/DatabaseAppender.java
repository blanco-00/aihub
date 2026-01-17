package com.aihub.admin.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import com.aihub.admin.service.SystemLogService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.UUID;

/**
 * 自定义 Logback Appender
 * 将 ERROR 级别的日志写入数据库
 */
public class DatabaseAppender extends AppenderBase<ILoggingEvent> implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext;
    private static SystemLogService systemLogService;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        DatabaseAppender.applicationContext = applicationContext;
    }
    
    /**
     * 获取 SystemLogService Bean
     */
    private SystemLogService getSystemLogService() {
        if (systemLogService == null && applicationContext != null) {
            try {
                systemLogService = applicationContext.getBean(SystemLogService.class);
            } catch (Exception e) {
                // Bean 可能还未初始化，忽略
            }
        }
        return systemLogService;
    }
    
    @Override
    protected void append(ILoggingEvent event) {
        // 只记录 ERROR 级别的日志
        if (!event.getLevel().levelStr.equals("ERROR")) {
            return;
        }
        
        // 获取 SystemLogService
        SystemLogService service = getSystemLogService();
        if (service == null) {
            // Service 还未初始化，跳过
            return;
        }
        
        try {
            // 获取日志信息
            String level = event.getLevel().levelStr;
            String loggerName = event.getLoggerName();
            String message = event.getFormattedMessage();
            
            // 从 loggerName 提取模块名（取包名的最后一部分）
            String module = extractModule(loggerName);
            
            // 获取堆栈信息
            String stackTrace = null;
            ThrowableProxy throwableProxy = (ThrowableProxy) event.getThrowableProxy();
            if (throwableProxy != null) {
                stackTrace = getStackTrace(throwableProxy.getThrowable());
            }
            
            // 获取 IP 地址和用户ID（从 MDC 中获取）
            String ip = event.getMDCPropertyMap().get("ip");
            String userIdStr = event.getMDCPropertyMap().get("userId");
            Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;
            String requestId = event.getMDCPropertyMap().get("requestId");
            if (requestId == null) {
                requestId = UUID.randomUUID().toString();
            }
            
            // 异步保存到数据库
            service.saveSystemLog(level, module, message, stackTrace, ip, userId, requestId);
        } catch (Exception e) {
            // 记录日志失败不影响应用运行，只记录到控制台
            System.err.println("保存系统日志到数据库失败: " + e.getMessage());
        }
    }
    
    /**
     * 从 loggerName 提取模块名
     */
    private String extractModule(String loggerName) {
        if (loggerName == null || loggerName.isEmpty()) {
            return "Unknown";
        }
        
        // 取包名的最后一部分作为模块名
        String[] parts = loggerName.split("\\.");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return loggerName;
    }
    
    /**
     * 获取异常堆栈信息
     */
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
