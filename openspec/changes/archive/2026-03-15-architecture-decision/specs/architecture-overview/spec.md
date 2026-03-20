## ADDED Requirements

### Requirement: 统一入口网关
系统 SHALL 使用Higress作为统一入口，处理外部请求的鉴权、限流、日志，并路由到对应内部服务。

#### Scenario: 外部请求入口
- **WHEN** 用户发送请求到AIHub
- **THEN** 请求经过Higress入口网关，进行鉴权和限流检查

#### Scenario: 按路径路由
- **WHEN** 请求路径为 /api/agent/*
- **THEN** Higress路由到Agent服务处理

### Requirement: 技术栈分层
系统 SHALL 按照业务类型选择技术栈，AI相关用Python，企业相关用Java。

#### Scenario: Python服务
- **WHEN** 需要模型调用、RAG检索
- **THEN** 使用Python服务（模型网关、RAG服务）

#### Scenario: Java服务
- **WHEN** 需要业务逻辑、传统系统集成
- **THEN** 使用Java服务（平台服务、Agent服务、MCP服务）

### Requirement: Token统计
系统 SHALL 通过Agent服务上报和Higress日志采集两种方式统计Token用量。

#### Scenario: Token上报
- **WHEN** Agent服务调用模型完成后
- **THEN** 记录input/output tokens，上报到监控系统
