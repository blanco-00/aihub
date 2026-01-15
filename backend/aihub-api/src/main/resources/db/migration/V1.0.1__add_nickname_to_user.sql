-- ============================================
-- AIHub 数据库迁移脚本 V1.0.1
-- 创建时间: 2026-01-13
-- 说明: 为用户表添加 nickname（用户昵称）字段
-- 注意: 此脚本用于升级已有数据库，新数据库请使用 V1.0.0（已包含 nickname 字段）
-- ============================================

-- 检查字段是否已存在，避免重复添加（适用于 Flyway 重复执行的情况）
SET @dbname = DATABASE();
SET @tablename = "user";
SET @columnname = "nickname";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  "SELECT 1", -- 字段已存在，不执行任何操作
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN ", @columnname, " VARCHAR(50) NULL COMMENT '用户昵称' AFTER username")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 将现有用户的 nickname 设置为 username（作为默认值）
UPDATE user SET nickname = username WHERE nickname IS NULL;

-- 添加索引（可选，如果需要按昵称搜索）
-- CREATE INDEX idx_user_nickname ON user(nickname);
