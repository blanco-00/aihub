-- ============================================
-- AIHub 数据库迁移脚本 V1.0.13
-- 创建时间: 2026-01-15
-- 说明: 创建用户角色关联表，支持多角色分配
-- ============================================

-- ============================================
-- 1. 创建用户角色关联表 (user_role)
-- ============================================
CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================================
-- 2. 迁移现有用户的角色到关联表
-- ============================================
-- 将 user 表中的 role 字段值转换为 role_id 并插入到 user_role 表
INSERT INTO user_role (user_id, role_id, created_at)
SELECT 
    u.id AS user_id,
    r.id AS role_id,
    u.created_at AS created_at
FROM user u
INNER JOIN role r ON r.code = u.role
WHERE u.is_deleted = 0
  AND r.is_deleted = 0
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id), role_id=VALUES(role_id);
