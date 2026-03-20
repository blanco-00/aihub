-- ============================================
-- AIHub 数据库迁移脚本 V1.0.17
-- 创建时间: 2026-01-17
-- 说明: 修复通知公告菜单配置，添加通知公告管理子菜单
-- ============================================

-- ============================================
-- 1. 更新通知公告父菜单为 Layout（容器菜单）
-- ============================================
UPDATE menu 
SET component = 'Layout', path = '/system/notice'
WHERE name = 'SystemNotice' 
  AND (component != 'Layout' OR path != '/system/notice');

-- ============================================
-- 2. 添加通知公告管理子菜单
-- ============================================
INSERT INTO menu (parent_id, name, path, component, icon, title, sort_order, show_link, keep_alive, status) 
SELECT 
  (SELECT id FROM menu WHERE name = 'SystemNotice' LIMIT 1),
  'SystemNoticeManagement', 
  '/system/notice/management', 
  'system/notice/index', 
  'ri:notification-badge-line', 
  'menus.pureNoticeManagement', 
  2, 1, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE name = 'SystemNoticeManagement');

-- ============================================
-- 3. 为所有角色分配通知公告管理菜单权限
-- ============================================
INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
CROSS JOIN menu m
WHERE r.is_deleted = 0 
  AND r.status = 1
  AND m.is_deleted = 0 
  AND m.status = 1
  AND m.name = 'SystemNoticeManagement'
  AND NOT EXISTS (
    SELECT 1 FROM role_menu rm 
    WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );
