-- Model Test History Table
CREATE TABLE IF NOT EXISTS model_test_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    model_id BIGINT NOT NULL COMMENT '模型配置ID',
    model_name VARCHAR(100) NOT NULL COMMENT '模型名称',
    vendor VARCHAR(50) NOT NULL COMMENT '厂商 (openai/zhipu/tongyi等)',
    model_display_id VARCHAR(100) NOT NULL COMMENT '模型ID (如: gpt-4, glm-4等)',
    user_message TEXT NOT NULL COMMENT '用户发送的消息',
    model_response TEXT COMMENT '模型返回的回复',
    success TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否成功 0-失败 1-成功',
    error_message VARCHAR(500) COMMENT '错误信息',
    response_time_ms BIGINT COMMENT '响应时间(毫秒)',
    token_usage INT COMMENT 'Token使用量',
    user_id BIGINT COMMENT '用户ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_model_id (model_id),
    INDEX idx_created_at (created_at),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型测试历史记录表';
