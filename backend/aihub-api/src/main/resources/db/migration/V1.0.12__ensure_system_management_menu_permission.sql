-- ============================================
-- AIHub 数据库迁移脚本 V1.0.12
-- 创建时间: 2026-01-16
-- 说明: 确保所有角色都关联了系统管理菜单（父菜单），以便子菜单自动显示
-- ============================================

-- ============================================
-- 为所有角色分配系统管理菜单权限（如果还没有关联）
-- ============================================
-- 系统管理菜单（id=1）是父菜单，关联后其所有子菜单会自动显示
INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
CROSS JOIN menu m
WHERE r.is_deleted = 0 
  AND r.status = 1
  AND m.is_deleted = 0 
  AND m.status = 1
  AND m.name = 'SystemManagement'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);

-- ============================================
-- 为所有角色分配系统监控菜单权限（如果还没有关联）
-- ============================================
-- 系统监控菜单（id=6）是父菜单，关联后其所有子菜单会自动显示
INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
CROSS JOIN menu m
WHERE r.is_deleted = 0 
  AND r.status = 1
  AND m.is_deleted = 0 
  AND m.status = 1
  AND m.name = 'SystemMonitor'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);
