package com.aihub.service.impl;

import com.aihub.dto.ConnectionTestResult;
import com.aihub.dto.DatabaseConfigDTO;
import com.aihub.service.SetupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
@Service
public class SetupServiceImpl implements SetupService {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public ConnectionTestResult testConnection(DatabaseConfigDTO config) {
        ConnectionTestResult result = new ConnectionTestResult();
        long startTime = System.currentTimeMillis();
        
        try {
            // 测试连接（不指定数据库，先连接到 MySQL 服务器）
            // 设置超时时间为 15 秒，避免在网络较慢或数据库负载高时超时
            String serverUrl = String.format("jdbc:mysql://%s:%d/?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&connectTimeout=15000&socketTimeout=15000",
                    config.getHost(), config.getPort());
            
            // 使用 Properties 设置连接参数，确保特殊字符能正确处理
            Properties props = new Properties();
            props.setProperty("user", config.getUsername());
            // 处理密码为空的情况
            String password = config.getPassword() != null ? config.getPassword() : "";
            props.setProperty("password", password);
            // 设置连接超时和 socket 超时（单位：毫秒），15 秒
            props.setProperty("connectTimeout", "15000");
            props.setProperty("socketTimeout", "15000");
            
            log.info("尝试连接数据库: host={}, port={}, username={}, passwordLength={}", 
                    config.getHost(), config.getPort(), config.getUsername(), 
                    password != null ? password.length() : 0);
            
            try (Connection conn = DriverManager.getConnection(serverUrl, props)) {
                log.info("数据库连接成功");
                // 连接成功，检查数据库是否存在
                result.setDatabaseExists(checkDatabaseExists(conn, config.getDatabase()));
                result.setSuccess(true);
                log.info("数据库检查完成: databaseExists={}", result.isDatabaseExists());
            }
        } catch (SQLException e) {
            log.error("数据库连接测试失败: {}", e.getMessage(), e);
            result.setSuccess(false);
            String errorMsg = e.getMessage();
            
            if (errorMsg != null && errorMsg.contains("Unknown database")) {
                result.setErrorMessage("数据库不存在，请先创建数据库 '" + config.getDatabase() + "'");
                result.setDatabaseExists(false);
            } else if (errorMsg != null && errorMsg.contains("Access denied")) {
                result.setErrorMessage("用户名或密码错误");
            } else if (errorMsg != null && errorMsg.contains("Communications link failure")) {
                result.setErrorMessage("无法连接到数据库服务器，请检查服务是否启动");
            } else if (errorMsg != null && (errorMsg.contains("timeout") || errorMsg.contains("Timeout"))) {
                result.setErrorMessage("连接超时，请检查数据库服务是否正常运行，或网络是否通畅");
            } else {
                result.setErrorMessage("连接失败: " + (errorMsg != null ? errorMsg : "未知错误"));
            }
        } catch (Exception e) {
            log.error("测试数据库连接时发生异常", e);
            result.setSuccess(false);
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("timeout") || errorMsg.contains("Timeout"))) {
                result.setErrorMessage("连接超时，请检查数据库服务是否正常运行");
            } else {
                result.setErrorMessage("测试连接时发生异常: " + (errorMsg != null ? errorMsg : e.getClass().getSimpleName()));
            }
            result.setDatabaseExists(false);
        } finally {
            result.setDuration(System.currentTimeMillis() - startTime);
            log.info("数据库连接测试完成: success={}, duration={}ms", result.isSuccess(), result.getDuration());
        }
        
        return result;
    }
    
    private boolean checkDatabaseExists(Connection conn, String databaseName) {
        try {
            String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, databaseName);
                try (var rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            log.debug("检查数据库是否存在失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public void saveConfig(DatabaseConfigDTO config) {
        // 处理密码为空的情况
        if (config.getPassword() == null) {
            config.setPassword("");
        }
        
        try {
            // 只保存到项目目录下的配置文件（.env 和 application-local.yml）
            saveEnvFile(config);
            
            // 动态更新 DataSource 配置（如果 DataSource 是 DriverManagerDataSource 类型）
            updateDataSource(config);
            
        } catch (Exception e) {
            log.error("保存配置失败", e);
            throw new RuntimeException("保存配置失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 动态更新 DataSource 配置
     * 注意：Spring Boot 使用 HikariCP 连接池，不支持动态更新，需要重启应用
     * 但配置已保存到文件，重启后会自动加载
     */
    private void updateDataSource(DatabaseConfigDTO config) {
        try {
            // Spring Boot 默认使用 HikariCP 连接池，不支持动态更新
            // 配置已保存到 application-local.yml，重启后会自动加载
            log.info("配置已保存，由于使用 HikariCP 连接池，需要重启应用才能生效");
            
            // 尝试关闭旧的连接池（如果可能）
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                com.zaxxer.hikari.HikariDataSource hikariDataSource = (com.zaxxer.hikari.HikariDataSource) dataSource;
                try {
                    hikariDataSource.close();
                    log.info("已关闭旧的 HikariCP 连接池");
                } catch (Exception e) {
                    log.warn("关闭 HikariCP 连接池失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("处理 DataSource 配置失败: {}", e.getMessage());
            // 不抛出异常，因为配置已保存，重启后仍可使用
        }
    }
    
    private void saveEnvFile(DatabaseConfigDTO config) {
        try {
            // 获取项目根目录（backend/aihub-api）
            // 通过类路径定位 resources 目录，然后向上找到 aihub-api 目录
            String projectRoot = getProjectRoot();
            
            // 保存到 application-local.yml（Spring Boot 会自动加载，优先级最高）
            String resourcesDir = projectRoot + File.separator + "src" + File.separator + "main" + File.separator + "resources";
            String localConfigFile = resourcesDir + File.separator + "application-local.yml";
            File resourcesDirFile = new File(resourcesDir);
            if (resourcesDirFile.exists()) {
                try (FileWriter writer = new FileWriter(localConfigFile)) {
                    writer.write("# AIHub Database Configuration (Generated by setup wizard)\n");
                    writer.write("# This file is in .gitignore and will not be committed\n");
                    writer.write("# Spring Boot will automatically load this file when 'local' profile is active\n");
                    writer.write("# Note: This file only overrides database configuration in application.yml\n");
                    writer.write("# Other configurations (MyBatis Plus, server, logging, etc.) will still be loaded from application.yml\n\n");
                    writer.write("spring:\n");
                    writer.write("  datasource:\n");
                    writer.write(String.format("    url: jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true\n",
                            config.getHost(), config.getPort(), config.getDatabase()));
                    writer.write(String.format("    username: %s\n", config.getUsername()));
                    writer.write(String.format("    password: %s\n", config.getPassword()));
                }
                log.info("配置文件已保存到: {} (Spring Boot 会自动加载)", localConfigFile);
                
                // 自动激活 local profile（如果 application.yml 中没有设置）
                activateLocalProfile(projectRoot, resourcesDir);
            } else {
                log.warn("resources 目录不存在，跳过保存 application-local.yml");
            }
            
            // 同时保存到 .env 文件（用于环境变量方式配置，Spring Boot 不会自动加载，需要手动加载）
            String envFile = projectRoot + File.separator + ".env";
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("# AIHub Database Configuration (Generated by setup wizard)\n");
                writer.write("# This file is in .gitignore and will not be committed\n");
                writer.write("# This file can be used with environment variables\n");
                writer.write("# Usage: export $(cat .env | xargs) && mvn spring-boot:run\n");
                writer.write("# Or use dotenv-cli: dotenv -f .env -- mvn spring-boot:run\n\n");
                writer.write(String.format("DB_HOST=%s\n", config.getHost()));
                writer.write(String.format("DB_PORT=%d\n", config.getPort()));
                writer.write(String.format("DB_NAME=%s\n", config.getDatabase()));
                writer.write(String.format("DB_USERNAME=%s\n", config.getUsername()));
                writer.write(String.format("DB_PASSWORD=%s\n", config.getPassword()));
            }
            log.info("环境变量文件已保存到: {}", envFile);
            
        } catch (IOException e) {
            log.warn("保存配置文件失败: {}", e.getMessage());
        }
    }
    
    /**
     * 获取项目根目录（backend/aihub-api）
     */
    private String getProjectRoot() {
        try {
            // 通过类路径定位 resources 目录
            java.net.URL resourceUrl = getClass().getClassLoader().getResource("application.yml");
            if (resourceUrl != null) {
                String resourcePath = resourceUrl.getPath();
                // 处理 URL 编码和 file: 前缀
                if (resourcePath.startsWith("file:")) {
                    resourcePath = resourcePath.substring(5);
                }
                // 处理 URL 编码
                try {
                    resourcePath = java.net.URLDecoder.decode(resourcePath, "UTF-8");
                } catch (java.io.UnsupportedEncodingException e) {
                    // 忽略
                }
                
                // 从 resources/application.yml 向上找到 aihub-api 目录
                // 路径格式：.../aihub-api/target/classes/application.yml 或 .../aihub-api/src/main/resources/application.yml
                if (resourcePath.contains("aihub-api")) {
                    int index = resourcePath.indexOf("aihub-api");
                    String projectRoot = resourcePath.substring(0, index + "aihub-api".length());
                    // 处理 Windows 路径（去掉开头的 /）
                    if (projectRoot.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("windows")) {
                        projectRoot = projectRoot.substring(1);
                    }
                    return projectRoot;
                }
            }
        } catch (Exception e) {
            log.warn("无法从类路径获取项目根目录: {}", e.getMessage());
        }
        
        // 回退方案：使用当前工作目录
        String userDir = System.getProperty("user.dir");
        if (userDir.endsWith("aihub-api")) {
            return userDir;
        } else if (userDir.contains("aihub-api")) {
            // 如果当前目录包含 aihub-api，尝试找到它
            int index = userDir.indexOf("aihub-api");
            return userDir.substring(0, index + "aihub-api".length());
        } else {
            // 如果都不行，尝试从当前目录向上查找
            File currentDir = new File(userDir);
            while (currentDir != null && !currentDir.getName().equals("aihub-api")) {
                currentDir = currentDir.getParentFile();
            }
            if (currentDir != null) {
                return currentDir.getAbsolutePath();
            }
        }
        
        // 最后的回退：假设当前目录就是项目根目录
        return userDir;
    }
    
    /**
     * 自动激活 local profile（如果 application.yml 中没有设置）
     */
    private void activateLocalProfile(String projectRoot, String resourcesDir) {
        try {
            String applicationYml = resourcesDir + "/application.yml";
            Path ymlPath = Paths.get(applicationYml);
            
            if (!Files.exists(ymlPath)) {
                log.warn("application.yml 不存在，跳过激活 local profile");
                return;
            }
            
            String content = Files.readString(ymlPath);
            
            // 检查是否已经设置了 profiles.active: local
            if (content.contains("active: local") || content.contains("active:local")) {
                log.debug("application.yml 中已激活 local profile，跳过自动激活");
                return;
            }
            
            // 检查是否已有 profiles 配置（但可能激活的是其他 profile）
            if (content.contains("profiles:")) {
                // 如果已有 profiles 配置，更新 active 值
                String newContent = content.replaceFirst(
                    "(profiles:\\s*\n\\s*active:\\s*)[^\\n]+",
                    "$1local"
                );
                if (!newContent.equals(content)) {
                    Files.writeString(ymlPath, newContent);
                    log.info("已更新 local profile");
                    return;
                }
                // 如果替换失败，说明格式可能不同，跳过
                log.debug("application.yml 中已有 profiles 配置，但格式无法自动更新，跳过");
                return;
            }
            
            // 在 spring: 节点下添加 profiles.active: local
            // 确保在 spring: 后面、application: 前面添加
            String newContent = content.replaceFirst(
                "(spring:\\s*\n)(\\s+application:)",
                "$1  profiles:\n    active: local\n$2"
            );
            
            // 如果上面的替换没有成功（可能是因为 spring: 后面没有 application:），尝试另一种方式
            if (newContent.equals(content)) {
                // 在 spring: 后面添加 profiles 配置
                newContent = content.replaceFirst(
                    "(spring:\\s*\n)",
                    "$1  profiles:\n    active: local\n"
                );
            }
            
            if (!newContent.equals(content)) {
                Files.writeString(ymlPath, newContent);
                log.info("已自动激活 local profile");
            } else {
                log.warn("无法自动激活 local profile，请手动在 application.yml 中添加：spring.profiles.active: local");
            }
        } catch (IOException e) {
            log.warn("激活 local profile 失败: {}", e.getMessage());
        }
    }
    
    @Override
    public boolean isConfigured() {
        // 优先检查 application-local.yml（Spring Boot 会自动加载，如果激活了 local profile）
        String projectRoot = getProjectRoot();
        String resourcesDir = projectRoot + File.separator + "src" + File.separator + "main" + File.separator + "resources";
        String localConfigFile = resourcesDir + File.separator + "application-local.yml";
        File localConfigFileObj = new File(localConfigFile);
        if (localConfigFileObj.exists()) {
            return true;
        }
        
        // 检查 .env 文件
        String envFile = projectRoot + File.separator + ".env";
        File envFileObj = new File(envFile);
        if (envFileObj.exists()) {
            return true;
        }
        
        // 检查环境变量是否设置
        String password = System.getenv("DB_PASSWORD");
        if (password != null && !password.isEmpty()) {
            return true;
        }
        
        return false;
    }
}
