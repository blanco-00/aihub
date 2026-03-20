-- ============================================
-- AIHub 数据库迁移脚本 V1.0.14
-- 创建时间: 2026-01-15
-- 说明: 添加文件管理菜单
-- ============================================

-- ============================================
-- 添加文件管理菜单到系统管理下
-- ============================================
INSERT INTO menu (parent_id, name, path, component, icon, title, sort_order, show_link, keep_alive, status) VALUES
((SELECT id FROM (SELECT id FROM menu WHERE name = 'SystemManagement' LIMIT 1) AS tmp), 
 'FileManagement', '/system/file/index', 'system/file/index', 'ri:folder-line', 'menus.pureFileManagement', 6, 1, 0, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================
-- 为所有角色分配文件管理菜单权限
-- ============================================
INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
CROSS JOIN menu m
WHERE r.is_deleted = 0 
  AND m.is_deleted = 0 
  AND m.name = 'FileManagement'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);
