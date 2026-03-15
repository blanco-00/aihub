## Context

AIHub采用模块化架构，aihub-admin负责后台管理，aihub-ai-infrastructure负责AI基础设施。当前AI调用代码(ModelGateway)错误地放在了aihub-admin。

## Goals / Non-Goals

**Goals:**
- 将AI相关代码迁移到正确模块
- 保持架构清晰

**Non-Goals:**
- 不改变业务逻辑

## Decisions

### 包结构
```
com.aihub.ai.infrastructure
├── model/
│   ├── ModelProvider.java
│   ├── ModelProviderFactory.java
│   └── impl/
│       ├── OpenAIProvider.java
│       ├── ZhipuAIProvider.java
│       └── TongyiProvider.java
└── ModelGateway.java
```

### 依赖关系
- aihub-admin → aihub-ai-infrastructure
- aihub-api → aihub-ai-infrastructure

## Risks / Trade-offs

- [风险] 包名变更影响引用 → [规避] 同步更新所有引用
