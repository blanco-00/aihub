# AIHub MVP 提案

> **提案名称**: AIHub最小可用产品(MVP)开发计划
> 
> **提案日期**: 2026-03-25
> 
> **提案状态**: 待评审
> 
> **提案人**: AIHub Team

---

## 一、背景与目标

### 1.1 为什么做这个项目

AIHub定位为"像搭乐高一样构建AI Agent"的可视化平台。当前系统已具备基础骨架（Java后端、Python AI服务、Vue前端），但核心AI能力（Agent对话、MCP工具、Skills技能库）尚未串联形成闭环。

**核心问题**:
- 前端目前只连接Java后端，Python侧的AI能力未被使用
- MCP工具、Skills技能库只有框架，无实际业务链路
- 会话管理与AI对话服务未打通
- 流式响应（SSE）实现不完整

### 1.2 目标是什么

**MVP目标**: 实现 **对话 + 会话 + MCP工具** 的最小可用闭环

```
用户 ──▶ 前端 ──▶ Python AI服务 ──▶ 外部AI模型
              │           │
              │           ├── MCP工具调用
              │           │
              ▼           ▼
           Java后端    会话历史
              │           │
              ▼           ▼
           MySQL      Redis
```

### 1.3 成功标准

| 指标 | 目标值 |
|------|--------|
| 对话响应时间 | 首字延迟 < 1s |
| 工具调用成功率 | > 95% |
| 会话切换响应 | < 500ms |
| 基础MCP工具 | 5个可用 |

---

## 二、架构决策

### 2.1 技术架构

| 组件 | 技术选型 | 说明 |
|------|----------|------|
| 前端 | Vue 3 + Element Plus | 现有前端框架 |
| Java后端 | Spring Boot 3.2 | 平台管理、模型配置、会话持久化 |
| Python AI服务 | FastAPI + LangChain | Agent对话、MCP工具执行 |
| 数据库 | MySQL 8.0 | 存储模型配置、会话消息 |
| 缓存 | Redis | 会话缓存 |

### 2.2 服务划分

| 服务 | 端口 | 职责 |
|------|------|------|
| Vue前端 | 3000 | 对话UI、会话列表、模型选择 |
| Java后端 | 8080 | 平台管理、模型配置CRUD、会话持久化 |
| Python AI | 8001 | AI对话、MCP工具执行、流式响应 |

### 2.3 前端调用策略

| 能力 | 调用服务 | 路径 |
|------|----------|------|
| 会话CRUD | Java | `/api/chat/session/*` |
| 模型配置 | Java | `/api/model-configs/*` |
| AI对话 | Python | `/api/chat/*` (SSE) |
| MCP工具 | Python | `/api/mcp/*` |

---

## 三、Gateway体系设计

### 3.1 三大Gateway职责

| Gateway | 位置 | 核心职责 | 当前完成度 |
|---------|------|----------|------------|
| **Model Gateway** | Python | 模型路由、Provider管理、Token统计 | ~20% |
| **MCP Gateway** | Python | 工具注册、连接池、执行调度 | ~30% |
| **Agent Gateway** | Python | Agent编排、会话管理、流式响应 | ~15% |

### 3.2 Model Gateway (模型网关)

**职责**:
- 统一模型接入（OpenAI/Claude/ZhipuAI/Tongyi）
- 模型健康检查
- Token使用统计
- 成本计算（后续P1）

**MVP接口**:
```python
class ModelGateway:
    def register_model(name, vendor, model_id, api_key, base_url):
        """注册模型配置"""
        
    def chat(model_name, messages, stream=True):
        """对话，支持流式"""
        
    def get_token_stats(user_id, date_range):
        """Token统计"""
```

### 3.3 MCP Gateway (MCP网关)

**职责**:
- 统一工具注册和管理
- 工具发现（list_tools）
- 工具执行（execute_tool）
- 连接池管理
- 并发控制

**MVP接口**:
```python
class MCPGateway:
    def register_tool(tool: ToolDefinition):
        """注册工具"""
        
    def list_tools(category=None):
        """工具列表，支持分类筛选"""
        
    def execute_tool(name, arguments, user_id):
        """执行工具"""
```

**MVP内置工具（5个）**:
| 工具 | 类型 | 说明 |
|------|------|------|
| web_search | builtin | 搜索工具 |
| calculator | builtin | 计算器 |
| file_read | builtin | 文件读取 |
| file_write | builtin | 文件写入 |
| http_request | http | HTTP请求 |

### 3.4 Agent Gateway (Agent网关)

**职责**:
- 统一Agent入口
- 对话编排（Prompt + 上下文 + 工具）
- 会话管理
- 流式响应

**MVP接口**:
```python
class AgentGateway:
    async def chat_stream(user_id, session_id, message, model_name, tools=None):
        """流式对话主流程"""
        
    def _should_use_tools(message) -> bool:
        """判断是否需要工具调用"""
```

---

## 四、数据库设计

### 4.1 已有表

| 表名 | 所属 | 说明 |
|------|------|------|
| model_config | Java | 模型配置 ✅ |
| chat_session | Java | 会话表 ✅ |
| chat_message | Java | 消息表 ✅ |

### 4.2 新增表

**mcp_tool (MCP工具表)**

```sql
CREATE TABLE `mcp_tool` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '工具名称',
  `description` TEXT COMMENT '工具描述',
  `input_schema` JSON NOT NULL COMMENT '输入参数Schema',
  `handler_type` VARCHAR(50) COMMENT '处理器类型: function/http/builtin',
  `handler_config` JSON COMMENT '处理器配置',
  `category` VARCHAR(50) COMMENT '分类: file/http/calc/search',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0
) COMMENT 'MCP工具表';
```

---

## 五、开发计划

### 5.1 开发阶段

| 阶段 | 周期 | 目标 |
|------|------|------|
| **Phase 0** | 1-2天 | 架构整合，打通Java↔Python通信 |
| **Phase 1** | 3-5天 | MCP工具链路，5个内置工具 |
| **Phase 2** | 2-3天 | 流式对话完善 |
| **Phase 3** | 2-3天 | 会话打通，数据持久化 |

**总计**: 约 8-13天

### 5.2 详细任务

#### Phase 0: 架构整合

| 任务 | 负责 | 验收标准 |
|------|------|----------|
| 前端对接Python SSE | 前端 | 能收到流式响应 |
| Java提供模型配置API | 后端 | 前端能获取模型列表 |
| Python CORS配置 | Python | 前端能跨域调用 |
| 简化认证方案 | 后端 | JWT透传验证 |

#### Phase 1: MCP工具链路

| 任务 | 负责 | 验收标准 |
|------|------|----------|
| 创建mcp_tool表 | 后端 | 表结构正确 |
| MCP Gateway完善 | Python | 支持工具注册/执行/列表 |
| 内置5个MCP工具 | Python | 工具可正常调用 |
| MCP工具面板UI | 前端 | 能选择工具，看到结果 |

#### Phase 2: 流式对话完善

| 任务 | 负责 | 验收标准 |
|------|------|----------|
| Python SSE流式输出 | Python | 完整SSE实现 |
| 前端流式渲染 | 前端 | 打字机效果 |
| 工具结果展示 | 前端 | 显示工具调用结果 |

#### Phase 3: 会话打通

| 任务 | 负责 | 验收标准 |
|------|------|----------|
| 消息存储到MySQL | 后端 | 对话后消息持久化 |
| 会话历史加载 | 前端 | 切换会话加载历史 |
| 模型切换保持上下文 | 前端 | 切换模型不丢上下文 |

---

## 六、资源估算

### 6.1 人力估算

| 角色 | 工作量 |
|------|--------|
| 后端（Java + Python） | 5-7天 |
| 前端（Vue） | 3-5天 |
| 测试验证 | 1-2天 |

### 6.2 技术依赖

| 依赖 | 说明 |
|------|------|
| LangChain | Python AI能力 |
| SSE | 流式响应 |
| Redis | 会话缓存 |
| MySQL | 持久化存储 |

---

## 七、风险与应对

| 风险 | 概率 | 影响 | 应对 |
|------|------|------|------|
| Python-Java服务通信不稳定 | 低 | 中 | MVP阶段接受限制，后续优化 |
| 流式响应延迟高 | 中 | 中 | 逐步优化，首字优先 |
| MCP工具执行失败 | 中 | 中 | 降级到纯对话模式 |

---

## 八、后续规划

MVP完成后，可按以下路线逐步完善：

| 阶段 | 目标 | 功能 |
|------|------|------|
| **v1.1** | 完善MCP | 连接池、流控、更多内置工具 |
| **v1.2** | Token统计 | 成本计算、实时监控 |
| **v2.0** | Agent模板 | 模板定义、实例化、内置模板 |
| **v2.1** | RAG | 文档解析、向量检索、知识库 |
| **v3.0** | Skills | 技能库、组合、预置技能 |

---

## 九、提案审批

| 角色 | 意见 | 签字 | 日期 |
|------|------|------|------|
| 技术负责人 | | | |
| 产品负责人 | | | |
| 项目负责人 | | | |

---

## 附录

### A. 相关文档

| 文档 | 位置 |
|------|------|
| MVP架构总览 | `docs/planning/mvp-architecture.md` |
| 系统架构 | `docs/architecture/system-architecture.md` |
| 模块架构 | `docs/backend/module-architecture.md` |
| 功能清单 | `docs/planning/features.md` |
| 开发计划 | `docs/planning/mvp/development-plan.md` |

### B. 当前代码状态

```
aihub-python/src/aihub/
├── agents/agent.py          # ⚠️ 需完善
├── agents/session.py         # ✅
├── mcp/server.py            # ⚠️ 框架有，需完善
├── skills/registry.py       # ⚠️ 框架有，需完善
├── services/model_gateway.py # ⚠️ 需完善
├── tools/registry.py         # ✅
└── main.py                  # ✅

aihub-java/aihub-ai-infrastructure/
└── infrastructure/
    ├── ModelGateway.java    # ✅
    └── model/impl/          # ✅ OpenAI/Tongyi/ZhipuAI
```
