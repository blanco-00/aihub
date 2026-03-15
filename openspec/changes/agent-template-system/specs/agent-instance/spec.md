## ADDED Requirements

### Requirement: 从模板实例化Agent
系统 SHALL 支持用户选择模板并填写必要参数后创建Agent实例。

#### Scenario: 选择模板创建实例
- **WHEN** 用户选择"智能客服"模板，填写Agent名称"公司客服001"
- **THEN** 系统基于模板创建Agent实例，包含模板所有组件配置

#### Scenario: 覆盖模板默认配置
- **WHEN** 实例化时用户选择不同模型(如Claude替代GPT-4)
- **THEN** Agent使用用户选择的模型，忽略模板默认

### Requirement: Agent实例管理
系统 SHALL 支持Agent实例的CRUD操作，与模板分离管理。

#### Scenario: 查看已创建Agent列表
- **WHEN** 用户访问Agent列表页
- **THEN** 显示用户创建的所有Agent实例，包含名称、模板来源、状态、创建时间

#### Scenario: 编辑Agent配置
- **WHEN** 用户编辑已创建的Agent
- **THEN** 修改仅影响该实例，不影响模板和其他实例

#### Scenario: 删除Agent实例
- **WHEN** 用户删除Agent实例
- **THEN** 实例从数据库移除，模板及其他实例不受影响
