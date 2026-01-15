-- ============================================
-- AIHub 数据库迁移脚本 V1.0.4
-- 创建时间: 2026-01-15
-- 说明: 添加日志表（登录日志、操作日志、系统日志）
-- ============================================

-- ============================================
-- 1. 登录日志表 (login_log)
-- ============================================
CREATE TABLE IF NOT EXISTS login_log (
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

-- ============================================
-- 2. 操作日志表 (operation_log)
-- ============================================
CREATE TABLE IF NOT EXISTS operation_log (
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

-- ============================================
-- 3. 系统日志表 (system_log)
-- ============================================
CREATE TABLE IF NOT EXISTS system_log (
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
