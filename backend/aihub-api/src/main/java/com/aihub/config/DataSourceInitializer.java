package com.aihub.config;

import com.aihub.service.InitializationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 数据源初始化器
 * 在应用启动时立即初始化数据库连接，而不是延迟加载
 * 同时预加载系统初始化状态，避免每次请求都查询数据库
 */
@Slf4j
@Component
@Order(1)  // 设置优先级，确保在其他组件之前执行
public class DataSourceInitializer implements CommandLineRunner {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    @Autowired(required = false)
    private InitializationService initializationService;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化数据库连接...");
        
        try {
            // 测试数据库连接
            if (jdbcTemplate != null) {
                jdbcTemplate.execute("SELECT 1");
                log.info("数据库连接初始化成功");
            } else {
                // 如果没有 JdbcTemplate，直接使用 DataSource 测试连接
                try (var connection = dataSource.getConnection()) {
                    connection.isValid(5);  // 5秒超时
                    log.info("数据库连接初始化成功");
                }
            }
            
            // 数据库连接成功后，预加载系统初始化状态
            if (initializationService != null) {
                try {
                    log.info("开始预加载系统初始化状态...");
                    boolean initialized = initializationService.isInitialized();
                    log.info("系统初始化状态预加载完成: initialized={}", initialized);
                } catch (Exception e) {
                    log.warn("预加载系统初始化状态失败（不影响应用启动）: {}", e.getMessage());
                    // 不抛出异常，让应用继续启动
                }
            }
        } catch (Exception e) {
            log.error("数据库连接初始化失败: {}", e.getMessage(), e);
            // 不抛出异常，让应用继续启动（连接池会在需要时重试）
            // 如果需要启动时强制检查，可以取消下面的注释
            // throw new RuntimeException("数据库连接初始化失败", e);
        }
    }
}
