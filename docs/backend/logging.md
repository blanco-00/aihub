# 日志配置指南

## 日志等级说明

Spring Boot 使用 SLF4J + Logback，日志等级从低到高如下：

### 1. TRACE（跟踪）
- **用途**：最详细的日志，通常用于追踪程序执行流程
- **使用场景**：方法进入/退出、变量值变化等
- **示例**：`log.trace("进入方法 processData，参数: {}", data)`
- **建议**：生产环境通常关闭，只在深度调试时使用

### 2. DEBUG（调试）
- **用途**：调试信息，帮助开发者定位问题
- **使用场景**：
  - 关键变量的值
  - 业务流程的关键步骤
  - 状态检查结果
- **示例**：
  - `log.debug("用户登录: username={}, role={}", username, role)`
  - `log.debug("系统已初始化，存在超级管理员")`
- **建议**：开发环境开启，生产环境关闭或仅关键模块开启

### 3. INFO（信息）
- **用途**：重要的业务信息，记录系统正常运行的关键事件
- **使用场景**：
  - 用户操作（登录、注册、创建、删除等）
  - 系统启动/关闭
  - 重要的业务操作成功
  - 配置加载完成
- **示例**：
  - `log.info("用户登录成功: username={}", username)`
  - `log.info("数据库表初始化成功，共执行 {} 条 SQL 语句", count)`
  - `log.info("系统启动完成，端口: {}", port)`
- **建议**：开发和生产环境都开启，这是最重要的日志等级

### 4. WARN（警告）
- **用途**：警告信息，表示可能有问题但不影响系统运行
- **使用场景**：
  - 配置缺失但使用了默认值
  - 资源不足但系统仍可运行
  - 非关键操作失败
  - 过时的 API 使用
- **示例**：
  - `log.warn("数据库连接池接近上限，当前连接数: {}", count)`
  - `log.warn("配置文件不存在，使用默认配置")`
  - `log.warn("验证码错误: email={}, inputCode={}", email, code)`
- **建议**：开发和生产环境都开启，需要关注但不需要立即处理

### 5. ERROR（错误）
- **用途**：错误信息，表示发生了错误但系统仍可继续运行
- **使用场景**：
  - 业务逻辑错误（如用户不存在、密码错误等）
  - 非致命异常
  - 操作失败
- **示例**：
  - `log.error("登录失败: username={}, reason={}", username, reason)`
  - `log.error("数据库初始化失败", e)`
  - `log.error("发送邮件失败: email={}", email, e)`
- **建议**：开发和生产环境都开启，必须关注和处理

### 6. FATAL（致命）
- **用途**：致命错误，可能导致系统崩溃
- **使用场景**：数据库连接失败、内存溢出等
- **注意**：Logback 不支持 FATAL 等级，使用 ERROR 代替
- **建议**：立即处理，系统可能无法继续运行

## 日志等级选择原则

### 开发环境
```yaml
logging:
  level:
    com.aihub: debug  # 项目代码使用 debug，便于调试
    org.springframework: info  # 框架使用 info，减少噪音
    org.apache.ibatis: debug  # SQL 日志使用 debug，便于查看 SQL
```

### 生产环境
```yaml
logging:
  level:
    com.aihub: info  # 项目代码使用 info，只记录重要信息
    org.springframework: warn  # 框架使用 warn，只记录警告
    org.apache.ibatis: warn  # SQL 日志使用 warn，减少输出
```

## 实际使用示例

### ✅ 正确使用

```java
// INFO：重要的业务操作
log.info("用户注册成功: username={}, email={}", username, email);

// DEBUG：调试信息
log.debug("密码验证: username={}, inputPasswordLength={}", username, password.length());

// WARN：警告信息
log.warn("验证码错误: email={}, inputCode={}", email, code);

// ERROR：错误信息
log.error("登录失败", e);  // 带异常
log.error("登录失败: username={}, reason={}", username, reason);  // 不带异常
```

### ❌ 错误使用

```java
// ❌ 不应该用 INFO 记录调试信息
log.info("进入方法 processData");  // 应该用 DEBUG

// ❌ 不应该用 ERROR 记录警告
log.error("验证码错误");  // 应该用 WARN

// ❌ 不应该用 DEBUG 记录重要业务操作
log.debug("用户登录成功");  // 应该用 INFO
```

## 当前项目日志配置

### application.yml 配置

```yaml
# 日志配置
logging:
  level:
    # 项目代码：info（生产环境），debug（开发环境）
    com.aihub: info
    
    # 初始化服务：保持 debug（用于调试初始化状态）
    com.aihub.service.impl.InitializationServiceImpl: debug
    
    # Spring 框架：info（减少框架日志）
    org.springframework: info
    
    # MyBatis：warn（减少 SQL 日志输出）
    org.apache.ibatis: warn
    
    # JDBC：warn（减少数据库连接日志）
    org.springframework.jdbc: warn
```

### 开发环境建议

如果需要查看 SQL 日志，可以在 `application-local.yml` 中覆盖：

```yaml
# application-local.yml（开发环境）
logging:
  level:
    com.aihub: debug
    org.apache.ibatis: debug  # 开启 SQL 日志
```

## 日志输出格式

Spring Boot 默认日志格式：
```
2026-01-12T18:54:57.102+07:00  INFO 78940 --- [aihub-api] [nio-8080-exec-1] com.aihub.controller.AuthController : 用户登录成功
```

格式说明：
- `2026-01-12T18:54:57.102+07:00`：时间戳
- `INFO`：日志等级
- `78940`：进程 ID
- `[aihub-api]`：应用名称
- `[nio-8080-exec-1]`：线程名
- `com.aihub.controller.AuthController`：类名
- `用户登录成功`：日志消息

## 最佳实践

1. **使用参数化日志**：避免字符串拼接
   ```java
   // ✅ 正确
   log.info("用户登录: username={}, role={}", username, role);
   
   // ❌ 错误
   log.info("用户登录: username=" + username + ", role=" + role);
   ```

2. **异常日志包含堆栈**：使用 `log.error("消息", exception)`
   ```java
   // ✅ 正确
   log.error("登录失败", e);
   
   // ❌ 错误（丢失堆栈信息）
   log.error("登录失败: " + e.getMessage());
   ```

3. **敏感信息不要记录**：密码、Token 等
   ```java
   // ✅ 正确
   log.info("用户登录: username={}", username);
   
   // ❌ 错误
   log.info("用户登录: username={}, password={}", username, password);
   ```

4. **日志等级要合理**：根据重要性选择
   - 重要业务操作 → INFO
   - 调试信息 → DEBUG
   - 警告信息 → WARN
   - 错误信息 → ERROR

## 总结

| 等级 | 用途 | 开发环境 | 生产环境 | 示例 |
|------|------|----------|----------|------|
| TRACE | 最详细跟踪 | 关闭 | 关闭 | 方法进入/退出 |
| DEBUG | 调试信息 | 开启 | 关闭 | 变量值、状态检查 |
| INFO | 重要业务信息 | 开启 | 开启 | 用户操作、系统启动 |
| WARN | 警告信息 | 开启 | 开启 | 配置缺失、非关键错误 |
| ERROR | 错误信息 | 开启 | 开启 | 业务错误、异常 |
| FATAL | 致命错误 | 开启 | 开启 | 系统崩溃（用 ERROR） |
