-- ============================================
-- AIHub 数据库迁移脚本 V1.0.6
-- 创建时间: 2026-01-15
-- 说明: 为用户表添加 created_at 索引，优化用户列表查询性能
-- ============================================

-- 添加 created_at 索引（用于 ORDER BY 排序优化）
-- MySQL 不支持 IF NOT EXISTS，使用存储过程检查索引是否存在
SET @index_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'user' 
    AND INDEX_NAME = 'idx_user_created_at'
);

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_user_created_at ON user(created_at)',
    'SELECT ''Index idx_user_created_at already exists, skipping'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加联合索引（用于常见查询场景优化）
-- 联合索引：is_deleted + created_at（用于列表查询的排序）
SET @index_exists2 = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'user' 
    AND INDEX_NAME = 'idx_user_deleted_created'
);

SET @sql2 = IF(@index_exists2 = 0,
    'CREATE INDEX idx_user_deleted_created ON user(is_deleted, created_at)',
    'SELECT ''Index idx_user_deleted_created already exists, skipping'' AS message'
);

PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;
