-- ============================================
-- 添加模型管理菜单
-- 创建时间: 2026-02-14
-- ============================================

-- 模型管理父菜单
INSERT INTO menu (parent_id, name, path, component, icon, title, sort_order, show_link, keep_alive, status) VALUES
(0, 'ModelManagement', '/model', 'Layout', 'ri:robot-2-line', 'menus.pureModelManagement', 12, 1, 0, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), sort_order=VALUES(sort_order);

-- 模型管理子菜单
INSERT INTO menu (parent_id, name, path, component, icon, title, sort_order, show_link, keep_alive, status) VALUES
((SELECT id FROM (SELECT id FROM menu WHERE name = 'ModelManagement' LIMIT 1) AS tmp),
 'ModelConfig', '/model/config/index', 'model/config/index', 'ri:database-2-line', 'menus.pureModelConfig', 1, 1, 0, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), title=VALUES(title);

-- 为所有角色分配模型管理菜单
INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
CROSS JOIN menu m
WHERE r.is_deleted = 0 AND m.is_deleted = 0 AND m.name = 'ModelManagement'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);

INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
CROSS JOIN menu m
WHERE r.is_deleted = 0 AND m.is_deleted = 0 AND m.name = 'ModelConfig'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);
