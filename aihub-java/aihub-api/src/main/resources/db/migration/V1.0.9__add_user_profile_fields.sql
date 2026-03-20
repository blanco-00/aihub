-- ============================================
-- AIHub 数据库迁移脚本 V1.0.9
-- 创建时间: 2026-01-15
-- 说明: 为用户表添加个人资料相关字段（头像、简介）
-- ============================================

-- 添加 phone 字段（使用存储过程检查是否存在）
SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = 'aihub'
    AND table_name = 'user'
    AND column_name = 'phone'
);

SET @alter_phone = IF(@column_exists = 0,
    'ALTER TABLE user ADD COLUMN phone VARCHAR(20) NULL COMMENT ''手机号'' AFTER email',
    'SELECT ''Phone column already exists'''
);

PREPARE stmt FROM @alter_phone;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 avatar 字段（使用存储过程检查是否存在）
SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = 'aihub'
    AND table_name = 'user'
    AND column_name = 'avatar'
);

SET @alter_avatar = IF(@column_exists = 0,
    'ALTER TABLE user ADD COLUMN avatar VARCHAR(500) NULL COMMENT ''头像URL'' AFTER phone',
    'SELECT ''Avatar column already exists'''
);

PREPARE stmt FROM @alter_avatar;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 description 字段（使用存储过程检查是否存在）
SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = 'aihub'
    AND table_name = 'user'
    AND column_name = 'description'
);

SET @alter_description = IF(@column_exists = 0,
    'ALTER TABLE user ADD COLUMN description VARCHAR(500) NULL COMMENT ''个人简介'' AFTER avatar',
    'SELECT ''Description column already exists'''
);

PREPARE stmt FROM @alter_description;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
