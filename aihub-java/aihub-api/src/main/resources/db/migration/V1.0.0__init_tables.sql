-- ============================================
-- AIHub 数据库迁移脚本 V1.0.0
-- 创建时间: 2026-01-15
-- 说明: 初始化数据库表结构（已合并 V1.0.1, V1.0.2, V1.0.3, V1.0.4, V1.0.6）
-- 注意: Flyway 会在已连接的数据库上执行，不需要 CREATE DATABASE 和 USE
-- ============================================

-- ============================================
-- 1. 用户表 (user)
-- ============================================
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    nickname VARCHAR(50) NULL COMMENT '用户昵称',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    phone VARCHAR(20) NULL COMMENT '手机号',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色 SUPER_ADMIN-超级管理员 ADMIN-管理员 USER-普通用户',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_email (email),
    INDEX idx_user_role (role),
    INDEX idx_user_status (status),
    INDEX idx_user_is_deleted (is_deleted),
    INDEX idx_user_created_at (created_at),
    INDEX idx_user_deleted_created (is_deleted, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 模型配置表 (model_config)
-- ============================================
CREATE TABLE IF NOT EXISTS model_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模型ID',
    name VARCHAR(100) NOT NULL COMMENT '模型名称',
    vendor VARCHAR(50) NOT NULL COMMENT '厂商 OpenAI/Claude/DeepSeek等',
    model_id VARCHAR(100) NOT NULL COMMENT '模型ID',
    api_key VARCHAR(255) NOT NULL COMMENT 'API Key（加密存储）',
    base_url VARCHAR(255) COMMENT 'Base URL',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    config JSON COMMENT '模型配置参数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    INDEX idx_model_vendor (vendor),
    INDEX idx_model_status (status),
    INDEX idx_model_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型配置表';

-- ============================================
-- 3. 菜单表 (menu)
-- ============================================
CREATE TABLE IF NOT EXISTS menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID，0表示顶级',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称（路由name）',
    path VARCHAR(200) NOT NULL COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径，父级为Layout',
    redirect VARCHAR(200) COMMENT '重定向路径',
    icon VARCHAR(50) COMMENT '图标',
    title VARCHAR(100) NOT NULL COMMENT '菜单标题（i18n key）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    show_link TINYINT(1) DEFAULT 1 COMMENT '是否显示 0-隐藏 1-显示',
    keep_alive TINYINT(1) DEFAULT 0 COMMENT '是否缓存',
    status TINYINT(1) DEFAULT 1 COMMENT '状态 0-启用 1-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0,
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order),
    INDEX idx_status (status),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ============================================
-- 4. 角色表 (role)
-- ============================================
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代码 SUPER_ADMIN/ADMIN/USER',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    status TINYINT(1) DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0,
    INDEX idx_role_code (code),
    INDEX idx_role_status (status),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ============================================
-- 5. 角色菜单关联表 (role_menu)
-- ============================================
CREATE TABLE IF NOT EXISTS role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ============================================
-- 6. 登录日志表 (login_log)
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
-- 7. 操作日志表 (operation_log)
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
-- 8. 系统日志表 (system_log)
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

-- ============================================
-- 9. 初始化角色数据
-- ============================================
INSERT INTO role (code, name, description, status) VALUES
('SUPER_ADMIN', '超级管理员', '拥有所有权限，至少保留一个', 1),
('ADMIN', '管理员', '拥有大部分管理权限', 1),
('USER', '普通用户', '基础使用权限', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- ============================================
-- 10. 初始化菜单数据（系统管理）
-- ============================================
-- 系统管理父菜单
INSERT INTO menu (parent_id, name, path, component, icon, title, sort_order, show_link, keep_alive, status) VALUES
(0, 'SystemManagement', '/system', 'Layout', 'ri:settings-3-line', 'menus.pureSysManagement', 10, 1, 0, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 系统管理子菜单
INSERT INTO menu (parent_id, name, path, component, icon, title, sort_order, show_link, keep_alive, status) VALUES
((SELECT id FROM (SELECT id FROM menu WHERE name = 'SystemManagement' LIMIT 1) AS tmp), 
 'SystemUser', '/system/user', 'system/user/index', 'ri:admin-line', 'menus.pureUser', 1, 1, 0, 1),
((SELECT id FROM (SELECT id FROM menu WHERE name = 'SystemManagement' LIMIT 1) AS tmp), 
 'SystemRole', '/system/role/index', 'system/role/index', 'ri:admin-fill', 'menus.pureRole', 2, 1, 0, 1),
((SELECT id FROM (SELECT id FROM menu WHERE name = 'SystemManagement' LIMIT 1) AS tmp), 
 'SystemMenu', '/system/menu/index', 'system/menu/index', 'ep:menu', 'menus.pureSystemMenu', 3, 1, 0, 1),
((SELECT id FROM (SELECT id FROM menu WHERE name = 'SystemManagement' LIMIT 1) AS tmp), 
 'SystemDept', '/system/dept/index', 'system/dept/index', 'ri:git-branch-line', 'menus.pureDept', 4, 1, 0, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================
-- 11. 初始化菜单数据（系统监控）
-- ============================================
-- 系统监控父菜单
INSERT INTO menu (parent_id, name, path, component, icon, title, sort_order, show_link, keep_alive, status) VALUES
(0, 'SystemMonitor', '/monitor', 'Layout', 'ep:monitor', 'menus.pureSysMonitor', 11, 1, 0, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 系统监控子菜单
INSERT INTO menu (parent_id, name, path, component, icon, title, sort_order, show_link, keep_alive, status) VALUES
((SELECT id FROM (SELECT id FROM menu WHERE name = 'SystemMonitor' LIMIT 1) AS tmp), 
 'OnlineUser', '/monitor/online-user', 'monitor/online/index', 'ri:user-voice-line', 'menus.pureOnlineUser', 1, 1, 0, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================
-- 12. 初始化角色菜单关联（所有角色都可以访问所有菜单）
-- ============================================
-- 为所有角色分配所有菜单
INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
CROSS JOIN menu m
WHERE r.is_deleted = 0 AND m.is_deleted = 0
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);

-- ============================================
-- 初始化完成
-- 注意: 超级管理员需要通过初始化页面创建，不在此脚本中插入
-- ============================================
