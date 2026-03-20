## ADDED Requirements

### Requirement: Vendor selection auto-fills default baseUrl
当用户选择厂商时，前端 SHALL 自动填充该厂商的默认 baseUrl。

#### Scenario: User selects ZhipuAI vendor
- **WHEN** 用户在模型配置对话框中选择厂商 "智谱" (zhipuai)
- **THEN** baseUrl 字段自动填充为 "https://open.bigmodel.cn/api/paas/v4"

#### Scenario: User selects OpenAI vendor
- **WHEN** 用户在模型配置对话框中选择厂商 "OpenAI"
- **THEN** baseUrl 字段自动填充为 "https://api.openai.com/v1"

#### Scenario: User can override default baseUrl
- **WHEN** 用户手动修改了自动填充的 baseUrl
- **THEN** 用户修改的值被保存，不被默认覆盖

#### Scenario: User clears baseUrl
- **WHEN** 用户清空了 baseUrl 字段
- **THEN** 系统使用该厂商的默认 URL 进行API调用
