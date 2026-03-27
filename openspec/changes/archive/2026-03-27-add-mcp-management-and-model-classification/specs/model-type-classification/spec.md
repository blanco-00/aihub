## ADDED Requirements

### Requirement: 模型分类字段
系统 SHALL 在 `model_config` 表中增加 `model_type` 字段，用于标识模型类型。

### Requirement: 模型类型枚举
`model_type` SHALL 支持以下枚举值：chat（对话模型）、embedding（向量模型）、image（文生图模型）、audio（语音模型）、rerank（重排序模型），默认值为 chat。

### Requirement: 模型列表按类型筛选
系统 SHALL 在模型管理页面提供模型类型下拉筛选，支持按类型筛选模型列表。

### Requirement: 模型类型展示
系统 SHALL 在模型列表中显示每条记录的类型标签。

#### Scenario: 新增模型时选择类型
- **WHEN** 用户点击"新增模型"
- **THEN** 表单中包含模型类型下拉选择，默认值为 chat

#### Scenario: 按类型筛选模型
- **WHEN** 用户在模型管理页面选择类型筛选条件
- **THEN** 列表仅显示该类型的模型

#### Scenario: 模型列表显示类型
- **WHEN** 用户查看模型列表
- **THEN** 每条记录显示对应的类型标签（如 chat、embedding）

#### Scenario: 模型类型枚举值
- **WHEN** 系统查询模型类型
- **THEN** 类型字段的值为 chat、embedding、image、audio 或 rerank 之一
