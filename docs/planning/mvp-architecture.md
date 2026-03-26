# AIHub MVP 架构总览

> **目标**: 对话 + 会话 + 工具(MCP) 的最小可用闭环
>
> **创建日期**: 2026-03-25

---

## 一、系统架构

### 1.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           用户浏览器 (Browser)                            │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │ HTTP/HTTPS
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        Vue 3 前端 (localhost:3000)                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │
│  │  Chat UI    │  │  Session    │  │  Model      │  │  MCP Tools  │   │
│  │  对话界面    │  │  会话列表    │  │  模型选择    │  │  工具调用   │   │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘   │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
          ┌───────────────────────┼───────────────────────┐
          │                       │                       │
          ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────────┐    ┌─────────────────┐
│   Java 后端     │    │   Python AI 服务    │    │   基础设施      │
│ (localhost:8080)│    │  (localhost:8001)   │    │                 │
├─────────────────┤    ├─────────────────────┤    ├─────────────────┤
│  平台管理        │    │  AI 能力            │    │  MySQL          │
│  - 用户/权限     │    │  - AIAgent          │    │  (会话/配置)    │
│  - 模型配置      │    │  - MCP Server       │    │                 │
│  - 会话管理      │    │  - Skills Registry  │    │  Redis          │
│  - Token统计    │    │  - Model Gateway    │    │  (缓存/会话)    │
└─────────────────┘    └─────────────────────┘    └─────────────────┘
```

### 1.2 模块职责划分

| 模块 | 技术栈 | 职责 | MVP优先级 |
|------|--------|------|----------|
| **前端** | Vue 3 + Element Plus | 对话UI、会话列表、模型选择 | P0 |
| **Java后端** | Spring Boot 3.2 | 平台管理、模型配置CRUD、会话持久化 | P0 |
| **Python AI服务** | FastAPI + LangChain | Agent对话、MCP工具执行、对话历史 | P0 |
| **MySQL** | MySQL 8.0 | 存储模型配置、会话消息、用户数据 | P0 |
| **Redis** | Redis | 会话缓存、Token缓存 | P0 |

---

## 二、模块详细设计

### 2.1 Java 后端模块 (aihub-java)

**已有模块**:
- `aihub-admin` - 用户管理、角色权限、系统监控 ✅
- `aihub-common` - 通用工具、Result封装、异常处理 ✅
- `aihub-ai-infrastructure` - 模型网关骨架、Provider工厂 ✅

**MVP需要完善**:
- `aihub-ai-infrastructure/model/` - 添加Token统计、成本计算（后续P1）
- `aihub-ai-infrastructure/model/` - 添加限流熔断（后续P2）

**核心接口**:

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 会话管理 | GET | `/api/chat/session/list` | 会话列表 |
| 会话管理 | POST | `/api/chat/session/create` | 创建会话 |
| 会话管理 | DELETE | `/api/chat/session/{id}` | 删除会话 |
| 消息管理 | GET | `/api/chat/session/{id}/messages` | 消息历史 |
| 模型配置 | GET | `/api/model-configs` | 模型列表 |
| 模型配置 | GET | `/api/model-configs/{id}` | 模型详情 |
| 模型配置 | POST | `/api/model-configs` | 创建模型 |
| 模型配置 | PUT | `/api/model-configs/{id}` | 更新模型 |
| 模型配置 | GET | `/api/model-configs/enabled` | 可用模型 |

### 2.2 Python AI 服务模块 (aihub-python)

**已有模块**:
- `agents/agent.py` - AIAgent (LangChain封装) ✅
- `agents/session.py` - SessionManager (Redis) ✅
- `mcp/server.py` - MCPServer (工具注册框架) ✅
- `skills/registry.py` - SkillRegistry (技能注册框架) ✅
- `services/model_gateway.py` - ModelGateway (简单Provider) ⚠️ 需完善
- `tools/registry.py` - ToolRegistry (LangChain tools) ✅

**MVP需要完善**:
- `services/model_gateway.py` - 增强: Token统计、成本计算
- `mcp/server.py` - 完善: 连接池、流控、工具执行链路
- 新增 `api/chat.py` - 流式对话API (SSE)

**核心接口**:

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 流式对话 | POST | `/api/chat/stream` | SSE流式对话 |
| 普通对话 | POST | `/api/chat` | 非流式对话 |
| MCP工具列表 | GET | `/api/mcp/tools` | 可用工具列表 |
| MCP工具执行 | POST | `/api/mcp/execute` | 执行指定工具 |
| 健康检查 | GET | `/health` | 服务健康状态 |

### 2.3 前端模块 (frontend)

**已有模块**:
- `views/ai/chat/index.vue` - 对话界面 ✅
- `views/ai/model-testing/` - 模型测试页 ✅
- `views/ai/prompt-templates/` - Prompt模板页 ✅
- `store/modules/chat.ts` - 会话状态管理 ✅
- `api/streamChat.ts` - SSE流式调用 ✅

**MVP需要完善**:
- `views/ai/chat/index.vue` - 对接Python SSE API
- 新增 `components/McpToolPanel.vue` - MCP工具面板
- 新增 `views/ai/tools/` - 工具管理页

---

## 三、数据流设计

### 3.1 对话流程 (MVP核心)

```
1. 用户在Vue前端输入消息
         │
         ▼
2. 前端调用 Java `/api/chat/session/create` 创建会话 (如需要)
         │
         ▼
3. 前端调用 Python `/api/chat/stream` (SSE)
   - 同时展示模型选择和MCP工具
         │
         ▼
4. Python AIAgent:
   a. 解析用户消息
   b. 判断是否需要调用MCP工具
   c. 如需工具 → 调用 MCPServer.execute_tool()
   d. 组织Prompt + 工具结果
   e. 调用 ModelGateway.chat()
         │
         ▼
5. ModelGateway:
   a. 根据模型vendor选择Provider
   b. 调用对应Provider (OpenAI/Claude/ZhipuAI)
   c. 返回响应 (流式或普通)
         │
         ▼
6. Python SSE流式返回前端
7. 前端渲染打字机效果
8. 消息存入MySQL (chat_message表)
```

### 3.2 会话管理流程

```
用户打开会话列表
         │
         ▼
前端调用 Java `/api/chat/session/list`
         │
         ▼
Java返回会话列表 (标题、最后消息时间、模型名)
         │
         ▼
用户选择会话
         │
         ▼
前端调用 Java `/api/chat/session/{id}/messages`
         │
         ▼
Java返回历史消息
         │
         ▼
前端继续对话 → 回到对话流程
```

---

## 四、数据库设计

### 4.1 MVP需要的表

| 表名 | 所属服务 | 说明 | 状态 |
|------|----------|------|------|
| `model_config` | Java | 模型配置 (名称、vendor、API Key) | ✅ 已有 |
| `chat_session` | Java | 会话表 (用户ID、标题、模型ID) | ✅ 已有 |
| `chat_message` | Java | 消息表 (会话ID、角色、内容) | ✅ 已有 |
| `mcp_tool` | Python/MySQL | MCP工具定义 | ⚠️ 需要创建 |
| `agent_template` | Python/MySQL | Agent模板 | ⏳ 后续P1 |

### 4.2 表结构

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

## 五、API对接设计

### 5.1 前端API调用策略

**当前状态**: 前端直连Java

**MVP目标**: 前端按能力选择服务

| 能力 | 调用服务 | 路径 | 说明 |
|------|----------|------|------|
| 会话CRUD | Java | `/api/chat/session/*` | 持久化存储 |
| 模型配置 | Java | `/api/model-configs/*` | 配置管理 |
| AI对话 | Python | `/api/chat/*` | AI能力 |
| MCP工具 | Python | `/api/mcp/*` | 工具能力 |
| SSE流式 | Python | `/api/chat/stream` | 流式响应 |

### 5.2 CORS配置

Python FastAPI需要配置CORS，允许前端访问:

```python
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],  # 前端地址
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
```

### 5.3 认证方案 (简化版)

**原则**: 统一认证，MVP阶段优先级低

```python
# Python侧 - 简单Token验证
async def verify_token(token: str) -> dict:
    # MVP阶段: 直接验证JWT格式，不调用Java验证服务
    # 后续: 可以调用Java的统一认证服务
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
        return payload
    except:
        raise HTTPException(status_code=401)
```

---

## 六、部署架构 (MVP开发模式)

### 6.1 本地开发服务

```bash
# 终端1: MySQL + Redis (Docker)
cd aihub/docker
docker-compose -f docker-compose.dev.yml up -d

# 终端2: Java后端
cd aihub-java
mvn spring-boot:run  # localhost:8080

# 终端3: Python AI服务
cd aihub-python
uvicorn src.aihub.main:app --reload --port 8001  # localhost:8001

# 终端4: Vue前端
cd frontend
pnpm dev  # localhost:3000
```

### 6.2 服务依赖

```
Vue (3000) ─┬─▶ Java (8080) ──▶ MySQL (3306)
            │                 │
            │                 └──▶ Redis (6379)
            │
            └─▶ Python (8001) ──▶ Redis (6379)
                              │
                              └──▶ OpenAI/Claude/ZhipuAI API
```

---

## 七、Gateway体系设计

> 核心模块: 模型网关、MCP网关、Agent网关

### 7.1 三大网关职责

| 网关 | 位置 | 核心职责 | MVP完成度 |
|------|------|----------|----------|
| **Model Gateway** | Python | 模型路由、Provider管理、Token统计 | ~20% |
| **MCP Gateway** | Python | 工具注册、连接池、执行调度 | ~30% |
| **Agent Gateway** | Python | Agent编排、会话管理、响应生成 | ~15% |

### 7.2 Model Gateway (模型网关)

**设计目标**:
- 统一模型接入 (OpenAI/Claude/ZhipuAI/Tongyi)
- 模型健康检查
- Token使用统计
- 成本计算
- 限流熔断 (后续P2)

**当前状态**:
```python
# services/model_gateway.py - 仅有简单注册
PROVIDERS = {"openai": ChatOpenAI, "anthropic": ChatAnthropic}

def register_model(name, provider, model_id, api_key, base_url):
    # 简单的Map存储
    pass
```

**MVP完善**:
```python
class ModelGateway:
    """MVP版本"""

    def __init__(self):
        self._providers = {}
        self._token_counter = TokenCounter()  # 新增

    def register_model(self, name, vendor, model_id, api_key, base_url):
        # 存储配置
        self._providers[name] = {...}

    def chat(self, name, messages):
        # 1. 获取Provider
        # 2. 调用模型
        # 3. 统计Token
        # 4. 记录成本
        return response

    def get_token_stats(self, user_id, date_range):
        # 返回Token使用统计
        return self._token_counter.get_stats(user_id, date_range)
```

### 7.3 MCP Gateway (MCP网关)

**设计目标**:
- 统一工具注册和管理
- 工具发现 (list_tools)
- 工具执行 (execute_tool)
- 连接池管理 (HTTP工具)
- 并发控制

**当前状态**:
```python
# mcp/server.py - 框架有，示例2个工具
class MCPServer:
    def register_tool(name, description, input_schema, handler):
        self._tools[name] = {...}

    def execute_tool(name, arguments):
        return handler(arguments)  # 直接调用，无池化
```

**MVP完善**:
```python
class MCPGateway:
    """MVP版本"""

    def __init__(self):
        self._tools = {}  # name -> ToolDefinition
        self._pools = {}  # handler_type -> ConnectionPool

    def register_tool(self, tool: ToolDefinition):
        self._tools[tool.name] = tool

        # HTTP类型工具创建连接池
        if tool.handler_type == "http":
            self._pools[tool.name] = HttpConnectionPool(
                max_connections=10,
                max_keepalive=30
            )

    def execute_tool(self, name, arguments, user_id):
        tool = self._tools[name]

        # 1. 参数验证 (Schema校验)
        self._validate(tool.input_schema, arguments)

        # 2. 执行 (根据类型)
        if tool.handler_type == "builtin":
            result = tool.handler(arguments)
        elif tool.handler_type == "http":
            result = self._execute_http(tool, arguments)
        else:
            result = tool.handler(arguments)

        # 3. 记录执行日志
        self._log_execution(user_id, tool.name, arguments, result)

        return result

    def list_tools(self, category=None):
        """工具列表，支持分类筛选"""
        tools = self._tools.values()
        if category:
            tools = [t for t in tools if t.category == category]
        return [t.to_schema() for t in tools]
```

### 7.4 Agent Gateway (Agent网关)

**设计目标**:
- 统一Agent入口
- 对话编排 (Prompt + 上下文 + 工具)
- 会话管理
- 流式响应

**当前状态**:
```python
# agents/agent.py - 简单LangChain封装
class AIAgent:
    def chat(self, input_text, chat_history):
        # 简单的消息组装 + LLM调用
        messages = [SystemMessage(...)] + history + [HumanMessage(...)]
        return self.llm.invoke(messages)
```

**MVP完善**:
```python
class AgentGateway:
    """MVP版本 - Agent编排核心"""

    def __init__(self, model_gateway: ModelGateway, mcp_gateway: MCPGateway):
        self.model_gateway = model_gateway
        self.mcp_gateway = mcp_gateway

    async def chat_stream(self, user_id, session_id, message, model_name, tools=None):
        """流式对话主流程"""

        # 1. 获取会话上下文
        context = self._get_context(session_id)

        # 2. 构建Prompt
        prompt = self._build_prompt(message, context)

        # 3. 判断是否需要工具调用
        if tools and self._should_use_tools(message):
            # 工具调用模式
            tool_calls = self._extract_tool_calls(message)
            for tool_call in tool_calls:
                tool_result = self.mcp_gateway.execute_tool(
                    tool_call.name,
                    tool_call.arguments,
                    user_id
                )
                # 将工具结果加入上下文
                context.add_tool_result(tool_call.id, tool_result)

        # 4. 流式调用模型
        async for chunk in self.model_gateway.chat_stream(prompt, model_name):
            yield chunk

    def _should_use_tools(self, message) -> bool:
        """简单判断是否需要工具 (后续可加LLM判断)"""
        # 关键词触发
        tool_keywords = ["查", "找", "计算", "搜索", "文件"]
        return any(kw in message for kw in tool_keywords)
```

---

## 八、MVP开发路线图

### Phase 0: 架构整合 (1-2天)

| 任务 | 负责 | 说明 |
|------|------|------|
| 打通Java↔Python通信 | 后端 | 确认API设计 |
| 前端对接Python SSE | 前端 | 从Java改为Python |
| 统一认证方案 | 后端 | JWT透传，简化验证 |

### Phase 1: MCP工具链路 (3-5天)

| 任务 | 负责 | 说明 |
|------|------|------|
| 创建mcp_tool表 | Java | 工具配置存储 |
| MCP Gateway完善 | Python | 工具注册/执行/连接池 |
| 内置5个MCP工具 | Python | 搜索、计算、文件等 |
| MCP工具面板UI | 前端 | 工具选择和结果显示 |

### Phase 2: 流式对话完善 (2-3天)

| 任务 | 负责 | 说明 |
|------|------|------|
| Python SSE流式输出 | Python | 完整SSE实现 |
| 前端流式渲染优化 | 前端 | 打字机效果 |
| 工具结果展示 | 前端 | 显示工具调用结果 |

### Phase 3: 会话打通 (2-3天)

| 任务 | 负责 | 说明 |
|------|------|------|
| 会话与AI服务对接 | 后端 | 消息存储到MySQL |
| 会话历史加载 | 前端 | 切换会话加载历史 |
| 模型切换保持上下文 | 前端 | 切换模型不丢上下文 |

---

## 九、已知问题与风险

### 9.1 架构冗余

| 问题 | 现状 | 影响 | 建议 |
|------|------|------|------|
| Java和Python都有ModelGateway | 重复建设 | 维护成本 | MVP后统一 |
| 前端目前只连Java | 未对接Python | AI能力未使用 | Phase 0整合 |
| Python侧Session未持久化 | 仅存Redis | 重启丢失 | MVP先接受，后续MySQL |

### 9.2 安全考量 (优先级低)

**原则**: MVP阶段简化安全，后续逐步完善

| 问题 | MVP方案 | 后续方案 |
|------|---------|----------|
| API认证 | JWT透传，Python简单验证 | 统一认证服务 |
| 工具执行 | 直接调用 | 沙箱隔离 |
| 数据传输 | HTTP | HTTPS |

---

## 十、文档索引

| 文档 | 位置 | 说明 |
|------|------|------|
| 系统架构 | `docs/architecture/system-architecture.md` | 整体架构设计 |
| 模块架构 | `docs/backend/module-architecture.md` | Java模块划分 |
| 功能清单 | `docs/planning/features.md` | 137个功能状态 |
| 开发计划 | `docs/planning/mvp/development-plan.md` | 3阶段开发规划 |
| **本文档** | `docs/planning/mvp-architecture.md` | MVP架构总览 |

---

## 附录：当前代码状态图

```
aihub-python/src/aihub/
├── agents/
│   ├── agent.py          # ⚠️ 简单LangChain封装，需完善
│   └── session.py        # ✅ Redis会话管理
├── mcp/
│   └── server.py         # ⚠️ 框架有，2个示例工具，需完善
├── skills/
│   └── registry.py       # ⚠️ 框架有，2个示例Skill，需完善
├── services/
│   ├── model_gateway.py  # ⚠️ 简单注册，缺Token统计
│   └── model_config_service.py  # ✅
├── tools/
│   └── registry.py       # ✅ 2个示例工具
├── api/
│   └── routes.py         # ⚠️ 基础API，需完善SSE
└── main.py               # ✅ FastAPI骨架

aihub-java/aihub-ai-infrastructure/
└── infrastructure/
    ├── ModelGateway.java        # ✅ Provider工厂
    └── model/
        ├── ModelProvider.java   # ✅ 接口定义
        ├── ModelProviderFactory.java  # ✅
        └── impl/
            ├── OpenAIProvider.java   # ✅
            ├── TongyiProvider.java   # ✅
            └── ZhipuAIProvider.java # ✅
```
