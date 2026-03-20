-- Prompt模板表
CREATE TABLE IF NOT EXISTS prompt_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    description VARCHAR(500) COMMENT '模板描述',
    category_id BIGINT COMMENT '分类ID',
    content TEXT NOT NULL COMMENT '模板内容',
    variables JSON COMMENT '变量定义JSON',
    is_builtin TINYINT(1) DEFAULT 0 COMMENT '是否内置模板',
    status TINYINT(1) DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_by BIGINT COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0,
    INDEX idx_prompt_template_category (category_id),
    INDEX idx_prompt_template_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt模板表';

-- Prompt分类表
CREATE TABLE IF NOT EXISTS prompt_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    code VARCHAR(50) NOT NULL COMMENT '分类编码',
    description VARCHAR(200) COMMENT '分类描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0,
    UNIQUE KEY uk_prompt_category_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt分类表';

-- 初始化Prompt分类数据
INSERT INTO prompt_category (name, code, description, sort_order) VALUES
('角色提示词', 'role', '用于定义AI角色的提示词', 1),
('通用提示词', 'general', '通用场景的提示词', 2),
('功能提示词', 'function', '特定功能的提示词', 3),
('行业提示词', 'industry', '行业专用提示词', 4);

-- 初始化内置Prompt模板
INSERT INTO prompt_template (name, description, category_id, content, variables, is_builtin, status, created_by) VALUES
('通用助手', '通用的AI助手，适用于日常对话', 1, '你是一个乐于助人的AI助手。请用友好、专业的方式回答用户的问题。', '[]', 1, 1, 1),
('代码助手', '专业的编程助手，支持多种编程语言', 3, '你是一位经验丰富的{{language}}开发专家。请帮助用户解决编程问题，提供清晰的代码示例和解释。

用户问题：{{question}}

请提供：
1. 问题分析
2. 代码解决方案
3. 关键解释', '[{"name": "language", "description": "编程语言", "default": "Python"}, {"name": "question", "description": "用户问题", "default": ""}]', 1, 1, 1),
('翻译助手', '多语言翻译专家', 3, '你是一位专业的翻译专家。请将以下内容从{{source_lang}}翻译成{{target_lang}}，保持原文的语气和风格。

原文：
{{text}}

翻译：', '[{"name": "source_lang", "description": "源语言", "default": "中文"}, {"name": "target_lang", "description": "目标语言", "default": "英文"}, {"name": "text", "description": "待翻译文本", "default": ""}]', 1, 1, 1),
('文本摘要', '自动提取文本关键信息', 3, '请为以下文本生成简洁的摘要，突出关键信息：

{{text}}

摘要要求：
- 控制在100字以内
- 包含主要观点
- 语言简洁明了', '[{"name": "text", "description": "待摘要文本", "default": ""}]', 1, 1, 1),
('客服话术', '专业的客户服务回复', 4, '你是一位专业的客服代表。请用友好、耐心的态度回复客户问题。

产品信息：{{product_info}}
客户问题：{{customer_question}}

回复要求：
- 语气亲切专业
- 提供有效解决方案
- 必要时表示歉意', '[{"name": "product_info", "description": "产品信息", "default": ""}, {"name": "customer_question", "description": "客户问题", "default": ""}]', 1, 1, 1);
