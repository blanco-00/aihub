-- ============================================
-- AIHub 数据库迁移脚本 V1.0.5
-- 创建时间: 2026-01-15
-- 说明: 添加部门表
-- ============================================

-- ============================================
-- 1. 部门表 (department)
-- ============================================
CREATE TABLE IF NOT EXISTS department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID，0表示顶级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT(1) DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0,
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ============================================
-- 2. 初始化部门数据（可选）
-- ============================================
-- 可以根据实际需求添加初始部门数据
