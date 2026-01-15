-- ============================================
-- AIHub 数据库迁移脚本 V1.0.8
-- 创建时间: 2026-01-15
-- 说明: 为用户表添加部门ID字段
-- ============================================

-- 添加部门ID字段到用户表
ALTER TABLE user ADD COLUMN department_id BIGINT DEFAULT 0 COMMENT '部门ID，0表示未分配' AFTER role;

-- 添加索引，优化按部门查询性能
CREATE INDEX idx_user_department_id ON user(department_id);
