-- ============================================
-- AIHub 数据库迁移脚本 V1.0.9
-- 创建时间: 2026-01-15
-- 说明: 为用户表添加个人资料相关字段（头像、简介）
-- ============================================

ALTER TABLE user 
ADD COLUMN avatar VARCHAR(500) NULL COMMENT '头像URL' AFTER phone,
ADD COLUMN description VARCHAR(500) NULL COMMENT '个人简介' AFTER avatar;
