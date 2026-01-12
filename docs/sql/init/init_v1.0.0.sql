-- ============================================
-- AIHub 数据库全量初始化脚本 v1.0.0
-- 创建时间: 2026-01-XX
-- 说明: 完整的数据库结构，用于新环境快速初始化
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS aihub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE aihub;

-- ============================================
-- 1. 用户表 (user)
-- ============================================
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色 SUPER_ADMIN-超级管理员 ADMIN-管理员 USER-普通用户',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_email (email),
    INDEX idx_user_role (role),
    INDEX idx_user_status (status),
    INDEX idx_user_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 模型配置表 (model_config)
-- ============================================
CREATE TABLE IF NOT EXISTS model_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模型ID',
    name VARCHAR(100) NOT NULL COMMENT '模型名称',
    vendor VARCHAR(50) NOT NULL COMMENT '厂商 OpenAI/Claude/DeepSeek等',
    model_id VARCHAR(100) NOT NULL COMMENT '模型ID',
    api_key VARCHAR(255) NOT NULL COMMENT 'API Key（加密存储）',
    base_url VARCHAR(255) COMMENT 'Base URL',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    config JSON COMMENT '模型配置参数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    INDEX idx_model_vendor (vendor),
    INDEX idx_model_status (status),
    INDEX idx_model_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型配置表';

-- ============================================
-- 初始化完成
-- 注意: 超级管理员需要通过初始化页面创建，不在此脚本中插入
-- ============================================
