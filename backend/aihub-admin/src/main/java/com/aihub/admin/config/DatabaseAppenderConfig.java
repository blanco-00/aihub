package com.aihub.admin.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * DatabaseAppender 配置类
 * 在 Spring 上下文初始化完成后，将 ApplicationContext 注入到 DatabaseAppender
 */
@Component
public class DatabaseAppenderConfig implements ApplicationListener<ContextRefreshedEvent> {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            
            // 查找 DatabaseAppender
            ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            Appender<?> databaseAppender = rootLogger.getAppender("DATABASE");
            
            if (databaseAppender instanceof DatabaseAppender) {
                ((DatabaseAppender) databaseAppender).setApplicationContext(applicationContext);
            }
        } catch (Exception e) {
            // 配置失败不影响应用启动
            System.err.println("配置 DatabaseAppender 失败: " + e.getMessage());
        }
    }
}
