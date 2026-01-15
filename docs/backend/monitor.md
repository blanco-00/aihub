# 系统监控模块设计

> 本文档包含系统监控相关的日志表设计和功能设计。

## 📚 相关文档

- [功能清单](../features.md) - 查看功能规划与开发状态
- [后端开发指南](./guide.md) - 返回后端文档总览
- [日志配置文档](./logging.md) - 日志配置和日志等级使用
- [数据库设计文档](./database.md) - 查看数据库设计规范
- [项目主文档](../../README.md) - 返回项目文档入口

## 数据库表设计

### 登录日志表 (login_log)

```sql
CREATE TABLE login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    ip VARCHAR(50) COMMENT 'IP地址',
    address VARCHAR(200) COMMENT '登录地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    status TINYINT(1) DEFAULT 1 COMMENT '登录状态 0-失败 1-成功',
    message VARCHAR(255) COMMENT '登录消息',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';
```

### 操作日志表 (operation_log)

```sql
CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    module VARCHAR(50) COMMENT '操作模块',
    operation VARCHAR(100) COMMENT '操作类型',
    method VARCHAR(10) COMMENT '请求方法',
    url VARCHAR(500) COMMENT '请求URL',
    params TEXT COMMENT '请求参数',
    result TEXT COMMENT '操作结果',
    status TINYINT(1) DEFAULT 1 COMMENT '操作状态 0-失败 1-成功',
    ip VARCHAR(50) COMMENT 'IP地址',
    duration INT COMMENT '耗时（毫秒）',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_module (module),
    INDEX idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
```

### 系统日志表 (system_log)

```sql
CREATE TABLE system_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    level VARCHAR(10) COMMENT '日志级别 DEBUG/INFO/WARN/ERROR',
    module VARCHAR(50) COMMENT '模块名称',
    message TEXT COMMENT '日志消息',
    stack_trace TEXT COMMENT '堆栈信息',
    ip VARCHAR(50) COMMENT 'IP地址',
    user_id BIGINT COMMENT '用户ID',
    request_id VARCHAR(100) COMMENT '请求ID',
    log_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_level (level),
    INDEX idx_module (module),
    INDEX idx_log_time (log_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';
```

## 在线用户设计

### 存储方式

使用 Redis 存储在线用户信息：

- **Key**: `online:user:{userId}`
- **Value**: JSON（包含登录时间、IP、Token等信息）
- **TTL**: Token过期时间

### 功能设计

- **查询接口**：从 Redis 读取所有在线用户
- **强制下线**：删除 Redis 中的 Token，前端刷新时自动跳转登录
- **在线人数统计**：统计当前在线用户数量

## 功能设计

### 登录日志

- **自动记录**：登录时自动记录（成功/失败）
- **查询功能**：支持按用户、IP、时间范围查询
- **导出功能**：支持导出登录日志

### 操作日志

- **AOP拦截**：通过 AOP 拦截器自动记录关键操作
- **查询功能**：支持按模块、操作类型、用户查询
- **审计功能**：提供操作审计功能

### 系统日志

- **日志入库**：应用日志自动入库
- **查询功能**：支持按级别、模块、时间查询
- **日志分析**：提供日志分析功能

### 在线用户

- **实时显示**：实时显示在线用户列表
- **用户信息**：显示用户登录信息、IP地址、登录时间
- **强制下线**：支持强制用户下线

## 实现优先级

- **P1**：登录日志（登录时记录）
- **P1**：操作日志（AOP拦截器）
- **P1**：在线用户（Redis存储）
- **P1**：系统日志（日志入库）
