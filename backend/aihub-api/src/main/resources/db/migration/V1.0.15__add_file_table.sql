-- ============================================
-- AIHub 数据库迁移脚本 V1.0.15
-- 创建时间: 2026-01-15
-- 说明: 创建文件元数据表，用于管理上传的文件信息
-- ============================================

-- ============================================
-- 文件表 (file)
-- ============================================
CREATE TABLE IF NOT EXISTS file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
    filename VARCHAR(255) NOT NULL COMMENT '原始文件名',
    storage_filename VARCHAR(255) NOT NULL COMMENT '存储文件名（UUID）',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径（相对路径）',
    file_url VARCHAR(500) NOT NULL COMMENT '文件访问URL',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    content_type VARCHAR(100) COMMENT '文件MIME类型',
    category VARCHAR(50) COMMENT '文件分类（avatar-头像, document-文档, image-图片等）',
    upload_user_id BIGINT COMMENT '上传用户ID',
    upload_username VARCHAR(50) COMMENT '上传用户名（冗余字段，便于查询）',
    business_type VARCHAR(50) COMMENT '业务类型（user_avatar-用户头像, article_image-文章图片等）',
    business_id BIGINT COMMENT '业务ID（关联的业务对象ID）',
    reference_count INT DEFAULT 0 COMMENT '引用次数（用于统计文件被引用次数）',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    INDEX idx_file_category (category),
    INDEX idx_file_upload_user (upload_user_id),
    INDEX idx_file_business (business_type, business_id),
    INDEX idx_file_status (status),
    INDEX idx_file_is_deleted (is_deleted),
    INDEX idx_file_created_at (created_at),
    INDEX idx_file_deleted_created (is_deleted, created_at),
    FOREIGN KEY (upload_user_id) REFERENCES user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';
