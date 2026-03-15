## ADDED Requirements

### Requirement: 模型配置CRUD
系统 SHALL 支持管理员添加、编辑、删除AI模型配置，包含名称、Provider类型、API Endpoint、API Key、模型名称、请求超时等参数。

#### Scenario: 添加新模型
- **WHEN** 管理员填写模型名称"智谱GLM-4"、选择Provider"智谱"、填写API Key、选择模型"glm-4"
- **THEN** 模型配置成功保存，状态默认为"启用"

#### Scenario: 编辑模型
- **WHEN** 管理员修改模型的API Key
- **THEN** 更新后的配置生效，旧API Key被覆盖

#### Scenario: 删除模型
- **WHEN** 管理员删除模型配置
- **THEN** 模型从数据库移除，确认删除时提示影响范围

#### Scenario: 模型状态管理
- **WHEN** 管理员禁用模型
- **THEN** 模型不可被Agent使用，但配置保留

### Requirement: 模型Provider适配
系统 SHALL 支持多种模型Provider的统一接入，通过适配器模式支持OpenAI、智谱GLM、阿里通义、百度文心、Minimax等。

#### Scenario: 添加智谱模型
- **WHEN** 选择Provider为"智谱GLM"，填写智谱API配置
- **THEN** 系统自动使用智谱API格式调用

#### Scenario: Provider扩展
- **WHEN** 需要接入新Provider
- **THEN** 只需实现Provider接口，无需修改调用层代码

### Requirement: API Key安全存储
系统 SHALL 对API Key进行加密存储，前端展示时脱敏处理。

#### Scenario: 查看模型列表
- **WHEN** 普通用户查看模型列表
- **THEN** API Key显示为"****"或部分隐藏

#### Scenario: 编辑API Key
- **WHEN** 管理员重新填写API Key
- **THEN** 新Key加密存储，旧Key被覆盖
