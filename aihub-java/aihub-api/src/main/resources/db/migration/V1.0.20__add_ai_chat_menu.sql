-- ============================================
-- 添加AI聊天菜单
-- 创建时间: 2026-03-15
-- ============================================

-- AI聊天子菜单（添加到ModelManagement父菜单下）
INSERT INTO menu (parent_id, name, path, component, icon, title, sort_order, show_link, keep_alive, status) VALUES
((SELECT id FROM (SELECT id FROM menu WHERE name = 'ModelManagement' LIMIT 1) AS tmp),
 'ModelChat', '/ai/chat', 'ai/chat/index', 'ri:chat-voice-line', 'menus.pureModelChat', 2, 1, 0, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), title=VALUES(title);

-- 为所有角色分配AI聊天菜单权限
INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
CROSS JOIN menu m
WHERE r.is_deleted = 0 AND m.is_deleted = 0 AND m.name = 'ModelChat'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);
