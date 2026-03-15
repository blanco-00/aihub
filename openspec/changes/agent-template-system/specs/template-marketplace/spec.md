## ADDED Requirements

### Requirement: 内置模板库
系统 SHALL 预置至少5个常用Agent模板，覆盖客服、研发、数据分析、企业知识问答、通用对话场景。

#### Scenario: 查看内置模板
- **WHEN** 用户访问模板选择页面
- **THEN** 显示所有内置模板，每个包含名称、描述、图标、标签

### Requirement: 模板预览
系统 SHALL 支持用户在创建前预览模板配置内容。

#### Scenario: 预览模板详情
- **WHEN** 用户点击模板卡片查看详情
- **THEN** 显示模板的完整配置(包含哪些组件、默认参数等)

### Requirement: 模板搜索筛选
系统 SHALL 支持按标签、场景搜索模板。

#### Scenario: 搜索客服模板
- **WHEN** 用户输入"客服"搜索
- **THEN** 显示所有包含"客服"标签的模板
