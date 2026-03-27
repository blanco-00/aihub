-- ============================================
-- 添加模型类型字段
-- 创建时间: 2026-03-27
-- ============================================

-- 添加 model_type 字段到 model_config 表
ALTER TABLE model_config ADD COLUMN model_type VARCHAR(50) NOT NULL DEFAULT 'chat' COMMENT '模型类型: chat=对话模型, embedding=向量模型, image=文生图模型, audio=语音模型, rerank=重排序模型' AFTER is_default;

-- 更新现有记录为 chat 类型
UPDATE model_config SET model_type = 'chat' WHERE model_type IS NULL OR model_type = '';

-- 为 model_type 添加索引（用于筛选查询）
CREATE INDEX idx_model_config_model_type ON model_config(model_type);
