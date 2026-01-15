-- ============================================
-- AIHub 数据库迁移脚本 V1.0.3
-- 创建时间: 2026-01-15
-- 说明: 添加菜单管理表和角色菜单关联表
-- ============================================

-- ============================================
-- 1. 菜单表 (menu)
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
-- 2. 角色表 (role)
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
-- 3. 角色菜单关联表 (role_menu)
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
-- 4. 初始化角色数据
-- ============================================
INSERT INTO role (code, name, description, status) VALUES
('SUPER_ADMIN', '超级管理员', '拥有所有权限，至少保留一个', 1),
('ADMIN', '管理员', '拥有大部分管理权限', 1),
('USER', '普通用户', '基础使用权限', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- ============================================
-- 5. 初始化菜单数据（系统管理）
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
-- 6. 初始化菜单数据（系统监控）
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
-- 7. 初始化角色菜单关联（所有角色都可以访问所有菜单）
-- ============================================
-- 为所有角色分配所有菜单
INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
CROSS JOIN menu m
WHERE r.is_deleted = 0 AND m.is_deleted = 0
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);
