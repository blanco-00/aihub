## Context

**当前状态**:
- Java后端 (8080): 平台管理、模型配置CRUD、会话持久化已完成
- Python AI服务 (8001): Agent框架、MCP框架、Skills框架存在但未串联
- Vue前端 (3000): 对话UI已完成，但目前直连Java

**核心问题**:
1. 前端只连Java，Python侧的AI能力未被使用
2. MCP工具框架存在，但只有2个示例工具，无执行链路
3. 流式响应(SSE)实现不完整

**约束**:
- MVP阶段简化安全：统一认证，JWT透传验证
- Python服务间通信接受当前限制
- 工具执行结果MVP阶段只记录日志，后续持久化

## Goals / Non-Goals

**Goals:**
- 实现对话+会话+MCP工具的最小闭环
- 前端能调用Python AI服务的流式对话
- MCP工具能被Agent调用并返回结果
- 会话消息能正确存储和加载

**Non-Goals:**
- 完整的Token统计和成本计算（后续v1.1）
- Agent模板系统（后续v2.0）
- RAG知识库（后续v2.1）
- 完整的权限认证体系（后续逐步完善）

## Decisions

### D1: 前端按能力选择服务

**选择**: 前端直连Java(会话/配置)或Python(AI对话/MCP工具)

**理由**: 
- 保持服务职责单一
- Python擅长AI能力，Java擅长业务管理
- 避免中间层转发延迟

### D2: Python侧简化认证

**选择**: JWT透传，Python简单验证

**理由**:
- MVP阶段快速验证核心价值流
- 后续可调用Java统一认证服务

### D3: MCP工具执行链路

**选择**: 关键词触发 + 内置工具为主

**理由**:
- 快速验证工具调用流程
- HTTP类型工具用连接池管理
- 避免复杂的多Agent协作

### D4: 流式响应方案

**选择**: Python SSE → 前端

**理由**:
- LangChain支持流式输出
- SSE比WebSocket更轻量
- 前端已有SSE处理逻辑

## Risks / Trade-offs

| 风险 | 影响 | 规避 |
|------|------|------|
| Python-Java服务间通信不稳定 | 中 | MVP接受限制，后续优化 |
| 流式响应延迟高 | 中 | 优化首字延迟 |
| MCP工具执行失败 | 中 | 降级到纯对话模式 |
| 两边ModelGateway重复 | 低 | MVP后统一 |

## Migration Plan

**Phase 0 (1-2天)**: 架构整合
- 前端对接Python SSE
- Java提供模型配置API
- Python CORS配置
- 简化认证方案

**Phase 1 (3-5天)**: MCP工具链路
- 创建mcp_tool表
- MCP Gateway完善
- 内置5个工具
- MCP工具面板UI

**Phase 2 (2-3天)**: 流式对话完善
- Python SSE流式输出
- 前端渲染优化
- 工具结果展示

**Phase 3 (2-3天)**: 会话打通
- 消息存储MySQL
- 会话历史加载
- 模型切换上下文

## Open Questions

1. 工具执行结果是否需要持久化存储？
2. Python侧用户权限粒度？
3. 会话消息是否需要加密存储？
