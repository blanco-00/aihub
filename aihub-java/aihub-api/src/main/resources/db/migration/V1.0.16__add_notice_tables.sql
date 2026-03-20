-- ============================================
-- AIHub 数据库迁移脚本 V1.0.16
-- 创建时间: 2026-01-17
-- 说明: 添加通知公告相关表
-- ============================================

-- ============================================
-- 1. 通知分类表 (notice_category)
-- ============================================
CREATE TABLE IF NOT EXISTS notice_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    code VARCHAR(50) NOT NULL COMMENT '分类代码（唯一）',
    description VARCHAR(200) NULL COMMENT '分类描述',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    UNIQUE KEY uk_notice_category_code (code),
    INDEX idx_notice_category_status (status),
    INDEX idx_notice_category_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知分类表';

-- ============================================
-- 2. 通知公告表 (notice)
-- ============================================
CREATE TABLE IF NOT EXISTS notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容（支持HTML）',
    category_id BIGINT NULL COMMENT '通知分类ID',
    type TINYINT NOT NULL DEFAULT 1 COMMENT '通知类型 1-普通通知 2-重要通知 3-紧急通知',
    publish_type TINYINT NOT NULL DEFAULT 1 COMMENT '发布范围类型 1-全部用户 2-指定部门 3-指定角色 4-指定用户',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    publisher_name VARCHAR(50) NOT NULL COMMENT '发布人姓名',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-草稿 1-已发布 2-已撤回',
    publish_time DATETIME NULL COMMENT '发布时间',
    expire_time DATETIME NULL COMMENT '过期时间（可选）',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序（数字越大越靠前）',
    view_count INT NOT NULL DEFAULT 0 COMMENT '查看次数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    INDEX idx_notice_category (category_id),
    INDEX idx_notice_type (type),
    INDEX idx_notice_status (status),
    INDEX idx_notice_publish_time (publish_time),
    INDEX idx_notice_is_deleted (is_deleted),
    INDEX idx_notice_status_publish_time (status, publish_time),
    INDEX idx_notice_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- ============================================
-- 3. 通知发布范围表 (notice_scope)
-- ============================================
CREATE TABLE IF NOT EXISTS notice_scope (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    notice_id BIGINT NOT NULL COMMENT '通知ID',
    scope_type TINYINT NOT NULL COMMENT '范围类型 1-部门 2-角色 3-用户',
    scope_id BIGINT NOT NULL COMMENT '范围ID（部门ID/角色ID/用户ID）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_notice_scope_notice (notice_id),
    INDEX idx_notice_scope_type_id (scope_type, scope_id),
    FOREIGN KEY (notice_id) REFERENCES notice(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知发布范围表';

-- ============================================
-- 4. 用户通知关联表 (notice_user)
-- ============================================
CREATE TABLE IF NOT EXISTS notice_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    notice_id BIGINT NOT NULL COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
    read_time DATETIME NULL COMMENT '阅读时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_notice_user (notice_id, user_id),
    INDEX idx_notice_user_user (user_id),
    INDEX idx_notice_user_read (is_read),
    INDEX idx_notice_user_user_read (user_id, is_read),
    FOREIGN KEY (notice_id) REFERENCES notice(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知关联表';

-- ============================================
-- 5. 初始化通知公告菜单（系统管理下）
-- ============================================
-- 通知公告父菜单
INSERT INTO menu (parent_id, name, path, component, icon, title, sort_order, show_link, keep_alive, status) VALUES
((SELECT id FROM (SELECT id FROM menu WHERE name = 'SystemManagement' LIMIT 1) AS tmp), 
 'SystemNotice', '/system/notice/index', 'system/notice/index', 'ri:notification-line', 'menus.pureNotice', 7, 1, 0, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 通知分类子菜单
INSERT INTO menu (parent_id, name, path, component, icon, title, sort_order, show_link, keep_alive, status) VALUES
((SELECT id FROM (SELECT id FROM menu WHERE name = 'SystemNotice' LIMIT 1) AS tmp), 
 'SystemNoticeCategory', '/system/notice/category', 'system/notice/category/index', 'ri:bookmark-line', 'menus.pureNoticeCategory', 1, 1, 0, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================
-- 6. 为所有角色分配通知公告菜单权限
-- ============================================
INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
CROSS JOIN menu m
WHERE r.is_deleted = 0 
  AND r.status = 1
  AND m.is_deleted = 0 
  AND m.status = 1
  AND m.name IN ('SystemNotice', 'SystemNoticeCategory')
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);
