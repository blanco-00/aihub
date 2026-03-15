## Context

AIHub需要支持多种AI模型的接入和管理。当前系统无模型管理能力，Agent、RAG等功能均依赖模型。此模块为AI能力的基础设施。

## Goals / Non-Goals

**Goals:**
- 实现多模型(5+主流)接入配置
- API Key安全存储
- 统一模型调用接口
- 在线模型测试

**Non-Goals:**
- 模型微调训练
- 模型用量详细统计(P1放到Token统计模块)
- 模型价格计费

## Decisions

### D1: HTTP客户端选型
- **选择**: OkHttp + RestTemplate双方案
- **理由**: OkHttp连接池管理优秀，RestTemplate与Spring集成好

### D2: API Key存储
- **选择**: 数据库加密存储
- **理由**: 平衡安全性和运维便利，使用AES加密

### D3: 模型调用方式
- **选择**: 统一接口 + Provider适配器模式
- **理由**: 新增模型只需实现Provider接口，符合开闭原则

### D4: 流式响应
- **选择**: Server-Sent Events (SSE)
- **理由**: 与OpenAI兼容，实现简单

## Risks / Trade-offs

- [风险] 国产模型API不稳定 → [规避] 先接入最成熟的智谱/通义，验证后再扩展
- [风险] API Key泄露 → [规避] 加密存储 + 敏感字段脱敏展示
- [风险] 模型响应超时 → [规避] 配置超时参数，MVP暂不加熔断
