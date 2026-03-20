-- 会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) COMMENT '会话标题',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    model_id BIGINT COMMENT '模型ID',
    model_name VARCHAR(100) COMMENT '模型名称（冗余存储）',
    message_count INT DEFAULT 0 COMMENT '消息数量',
    total_tokens INT DEFAULT 0 COMMENT '总Token数',
    last_message_at DATETIME COMMENT '最后消息时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0,
    INDEX idx_chat_session_user (user_id),
    INDEX idx_chat_session_model (model_id),
    INDEX idx_chat_session_last_msg (last_message_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色 user/assistant/system',
    content TEXT NOT NULL COMMENT '消息内容',
    tokens INT DEFAULT 0 COMMENT 'Token数量',
    model_id BIGINT COMMENT '使用的模型ID',
    model_name VARCHAR(100) COMMENT '模型名称',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_chat_message_session (session_id),
    INDEX idx_chat_message_created (created_at),
    CONSTRAINT fk_chat_message_session FOREIGN KEY (session_id) REFERENCES chat_session(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';
