-- ============================================
-- AIHub 数据库迁移脚本 V1.0.10
-- 创建时间: 2026-01-15
-- 说明: 创建字典管理相关表（字典类型表、字典数据表）
-- ============================================

-- ============================================
-- 字典类型表 (dict_type)
-- ============================================
CREATE TABLE IF NOT EXISTS dict_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典主键',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型（系统编码）',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    remark VARCHAR(500) NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    UNIQUE KEY uk_dict_type (dict_type),
    INDEX idx_dict_name (dict_name),
    INDEX idx_dict_status (status),
    INDEX idx_dict_is_deleted (is_deleted),
    INDEX idx_dict_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- ============================================
-- 字典数据表 (dict_data)
-- ============================================
CREATE TABLE IF NOT EXISTS dict_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典编码',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型（关联dict_type.dict_type）',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典键值',
    sort_order INT DEFAULT 0 COMMENT '字典排序',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    remark VARCHAR(500) NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    INDEX idx_dict_data_type (dict_type),
    INDEX idx_dict_data_value (dict_value),
    INDEX idx_dict_data_status (status),
    INDEX idx_dict_data_sort (sort_order),
    INDEX idx_dict_data_is_deleted (is_deleted),
    INDEX idx_dict_data_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- ============================================
-- 初始化字典管理菜单
-- ============================================
-- 添加字典管理菜单到系统管理下
INSERT INTO menu (parent_id, name, path, component, icon, title, sort_order, show_link, keep_alive, status) VALUES
((SELECT id FROM (SELECT id FROM menu WHERE name = 'SystemManagement' LIMIT 1) AS tmp), 
 'SystemDict', '/system/dict/index', 'system/dict/index', 'ri:book-open-line', 'menus.pureDict', 5, 1, 0, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================
-- 为所有角色分配字典管理菜单权限
-- ============================================
INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
CROSS JOIN menu m
WHERE r.is_deleted = 0 
  AND m.is_deleted = 0 
  AND m.name = 'SystemDict'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);
