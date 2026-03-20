## ADDED Requirements

### Requirement: Vendor management separate from model management
厂商管理和模型管理 SHALL 分离为两个独立的功能模块。

#### Scenario: User configures vendor
- **WHEN** 用户进入厂商管理页面
- **THEN** 只需要填写：厂商名称、API Key、可选的Base URL

#### Scenario: User creates model with vendor
- **WHEN** 用户在模型管理页面创建新模型
- **THEN** 可以从已配置的厂商列表中选择，并获取该厂商的模型列表

#### Scenario: User quickly switches model in chat
- **WHEN** 用户在AI对话页面切换模型
- **THEN** 模型下拉列表显示所有已启用的模型，支持快速切换

#### Scenario: Model inherits vendor configuration
- **WHEN** 模型关联到厂商后
- **THEN** 调用模型API时使用厂商配置的API Key和Base URL
