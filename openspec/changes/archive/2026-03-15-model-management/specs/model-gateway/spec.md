## ADDED Requirements

### Requirement: 统一模型调用接口
系统 SHALL 提供统一的模型调用接口，屏蔽不同Provider的API差异。

#### Scenario: 文本生成调用
- **WHEN** 调用统一接口发送prompt请求
- **THEN** 系统根据配置的模型Provider转发请求，返回生成文本

#### Scenario: 指定模型调用
- **WHEN** 请求指定模型ID
- **THEN** 调用对应模型的API，忽略默认模型配置

### Requirement: 流式响应
系统 SHALL 支持Server-Sent Events (SSE)流式输出，减少等待时间。

#### Scenario: 流式文本生成
- **WHEN** 请求开启流式输出
- **THEN** 以SSE格式逐步返回生成内容

### Requirement: 模型健康检查
系统 SHALL 提供模型可用性检测接口。

#### Scenario: 检查模型状态
- **WHEN** 调用健康检查接口
- **THEN** 返回模型可用状态和响应时间
