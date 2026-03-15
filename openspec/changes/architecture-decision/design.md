## Context

AIHub项目需要明确技术架构，定位为"AI能力编排平台"。需要整合Higress作为统一入口网关，协调平台服务、Agent服务、模型网关、RAG服务、MCP服务之间的通信。

## Goals / Non-Goals

**Goals:**
- 明确技术栈分层：Python用于AI服务，Java用于企业服务
- 确定Higress作为统一入口网关的路由规则
- 定义各服务间通信方式
- 确定Token统计和限流策略

**Non-Goals:**
- 不包含Kubernetes容器编排方案
- 不包含多集群部署方案
- 不包含商业化SaaS架构

## Decisions

### D1: 一个Higress实例
- **选择**: 单实例Higress处理所有流量
- **理由**: 简化运维，通过路由规则区分不同服务

### D2: 路由规则
```
/api/platform/* → 平台服务 (Java)
/api/agent/*     → Agent服务 (Java)
/api/model/*    → 模型网关 (Python)
/api/rag/*      → RAG服务 (Python)
/api/mcp/*      → MCP服务 (Java)
```

### D3: 技术栈选择
- **平台服务**: Java Spring Boot
- **Agent服务**: Java (业务逻辑) / Python (复杂编排)
- **模型网关**: Python (LangChain生态)
- **RAG服务**: Python (向量库生态)
- **MCP服务**: Java (传统系统集成)

### D4: Token统计方式
- **选择**: Agent服务上报 + Higress日志采集
- **理由**: 简单有效，避免模型流量回头

## Risks / Trade-offs

- [风险] 多技术栈维护成本 → [规避] 初期保持简单，Python仅用于AI相关
- [风险] 服务间通信安全 → [规避] 内部网络隔离，Higress做服务间鉴权
