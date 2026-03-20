## Context

AIHub项目需要明确技术架构，定位为"AI能力编排平台"。需要整合Higress作为统一入口网关，协调平台服务、Agent服务、模型网关、RAG服务、MCP服务之间的通信。

当前项目使用 Java Spring Boot 实现所有后端功能，包括后台管理和 AI 能力。AI 能力实现存在以下局限：
1. Java 生态缺乏像 LangChain 这样成熟的 Agent 开发框架
2. RAG 实现复杂，需要大量手动实现向量检索、文档处理等
3. 团队希望深入学习 Python AI 开发（LangChain/LangGraph）

因此需要新增独立的 Python AI 服务，与 Java 服务并存。

## Goals / Non-Goals

**Goals:**
- 明确技术栈分层：Python用于AI服务，Java用于企业服务
- 确定Higress作为统一入口网关的路由规则
- 定义各服务间通信方式
- 确定Token统计和限流策略
- 新增 Python AI 服务（FastAPI + LangChain/LangGraph）
- 目录结构重组（backend → aihub-java，新增 aihub-python）

**Non-Goals:**
- 不包含Kubernetes容器编排方案
- 不包含多集群部署方案
- 不包含商业化SaaS架构
- 不删除现有 Java AI 代码（渐进式迁移）

## Decisions

### D1: 一个Higress实例
- **选择**: 单实例Higress处理所有流量
- **理由**: 简化运维，通过路由规则区分不同服务

### D2: 路由规则
```
/api/platform/* → 平台服务 (Java)
/api/agent/*     → Agent服务 (Python)
/api/model/*    → 模型网关 (Python)
/api/rag/*      → RAG服务 (Python)
/api/mcp/*      → MCP服务 (Java)
```

### D3: 技术栈选择
- **平台服务**: Java Spring Boot
- **Agent服务**: Python (LangChain/LangGraph)
- **模型网关**: Python (LangChain生态)
- **RAG服务**: Python (向量库生态)
- **MCP服务**: Java (传统系统集成)

### D4: Token统计方式
- **选择**: Agent服务上报 + Higress日志采集
- **理由**: 简单有效，避免模型流量回头

### D5: Python 服务实现方式
- **选择**: 创建独立的 Python 服务，使用 FastAPI + LangChain + LangGraph
- **理由**: 
  - LangChain/LangGraph 是 Python 生态，集成到 Java 项目成本高
  - 独立服务便于独立部署和扩展
  - 学习价值最大化：完整体验 Python Web 开发

### D6: 目录结构
- **选择**: 
  ```
  AIHub/
  ├── frontend/          # Vue3 前端
  ├── aihub-java/       # Java 后端（原 backend/）
  ├── aihub-python/     # Python 服务
  └── docker/           # Docker Compose
  ```
- **理由**: 结构清晰，技术栈一眼可辨，独立部署

### D7: 数据库访问方式
- **选择**: Python 直连 MySQL，读取 model_config 等表
- **理由**: 简化数据同步，直接利用现有 Java 项目的模型配置

### D8: 认证机制
- **选择**: 统一 JWT 认证，Python 服务验证前端 token
- **理由**: 复用现有认证系统，无需重建

### D9: Agent 和 MCP Skills 技术栈选择
- **选择**: Agent + MCP 放到 Python 技术栈
- **理由**:
  - **Agent 核心**: LangChain/LangGraph 是事实标准，Java 生态缺乏成熟的 Agent 框架
  - **MCP Server**: MCP 协议来自 Anthropic，官方 SDK 以 Python 为主
  - **MCP Tools**: 大多数 MCP 工具（如 Brave Search、Filesystem）有 Python SDK
- **架构示意**:
  ```
  aihub-python/
  ├── agents/              # Agent 核心 (LangChain/LangGraph)
  ├── mcp/                 # MCP Server + Tools
  │   ├── server.py       # MCP 协议服务
  │   └── tools/          # MCP 工具实现
  └── skills/             # 业务 Skills
      ├── python/         # Python 实现 (复杂 AI 逻辑)
      └── java/           # Java 实现 (简单业务逻辑，可通过 API 调用)
  ```

### D10: 路由规则修正
- **选择**: MCP 服务也指向 Python
- **理由**: MCP SDK 原生支持 Python，与 Agent 集成更紧密
- **修正后的路由**:
  ```
  /api/platform/* → 平台服务 (Java)
  /api/agent/*     → Agent服务 (Python)
  /api/model/*    → 模型网关 (Python)
  /api/rag/*      → RAG服务 (Python)
  /api/mcp/*      → MCP服务 (Python)
  ```

### D11: 文档体系优化策略
- **选择**: 参照成功开源项目（LangChain、FastAPI）优化文档
- **理由**:
  - 好的文档是开源项目成功的关键因素
  - LangChain 130k+ Star，FastAPI 96k+ Star，它们的文档值得学习
- **学习点**:
  | 特点 | LangChain | FastAPI | AIHub 现状 |
  |------|-----------|---------|-----------|
  | Logo | ✅ | ✅ | ❌ 需设计 |
  | 一行安装 | pip install | pip install | Docker 一键 |
  | 徽章 | 10+ | 8+ | 2-3 个 |
  | 视频演示 | ❌ | ✅ Mini Documentary | ❌ |
  | 名人背书 | ❌ | Microsoft/Uber/Netflix | ❌ |
  | Sponsor 区 | ❌ | ✅ | ❌ 长期 |
  | 代码示例 | 简短 | 完整可运行 | 较长 |
  | 文档结构 | 完善 | 完善 | 较乱 |

### D12: README 优化方案
- **选择**: 简化 README，保留核心内容
- **理由**: 当前 README 387行太长，用户一眼看不到重点
- **优化方案**:
  ```
  # AIHub
  [Logo]
  
  > 一句话描述
  
  [徽章: Star, License, Tech Stack]
  
  ## 一行启动命令
  docker compose up -d
  
  ## 3-4 个核心特性
  - 特性1
  - 特性2
  - 特性3
  
  ## 快速开始（精简）
  - 1-2 行命令
  
  ## 文档导航
  链接到详细文档
  ```

### D13: 文档结构重组
- **选择**: 重新组织 docs/ 目录结构
- **理由**: 当前 29 个 md 文件较混乱
- **目标结构**:
  ```
  docs/
  ├── README.md           # 入口（简化）
  ├── quickstart.md      # 快速开始
  ├── tutorial.md        # 教程（场景化）
  ├── concepts.md        # 核心概念
  ├── api-reference.md   # API 参考
  ├── deployment/        # 部署文档
  ├── development/       # 开发指南
  ├── architecture/      # 架构设计
  ├── examples/          # 示例
  │   ├── customer-service.md
  │   ├── code-assistant.md
  │   └── data-analysis.md
  └── changelog.md       # 更新日志
  ```

### D14: 品牌建设
- **选择**: 设计 Logo 和品牌元素
- **理由**: 品牌是开源项目的第一印象
- **待做**:
  - 设计 AIHub Logo
  - 设计 Slogan
  - 统一视觉风格

## Risks / Trade-offs

- [风险] 多技术栈维护成本 → [规避] 初期保持简单，Python仅用于AI相关，渐进式迁移
- [风险] 服务间通信安全 → [规避] 内部网络隔离，Higress做服务间鉴权
- [风险] 数据库连接管理 → [规避] 使用 SQLAlchemy 连接池，配置合理连接数
- [风险] 前后端 API 切换 → [规避] 渐进式切换，保持两套 API 同时可用

## Migration Plan

### 阶段 0：准备（1 天）
1. 创建 `aihub-python/` 目录结构
2. 配置 pyproject.toml 和依赖
3. 配置 Docker 环境

### 阶段 1：基础服务（2-3 天）
1. FastAPI 基础框架搭建
2. JWT 认证中间件
3. MySQL/Redis 连接配置
4. 健康检查接口

### 阶段 2：AI 对话（3-5 天）
1. 模型网关实现
2. LangChain Agent 集成
3. 对话 API 实现
4. 与前端联调

### 阶段 3：目录重组（1 天）
1. `backend/` → `aihub-java/`
2. 更新 docker-compose
3. 更新文档引用
