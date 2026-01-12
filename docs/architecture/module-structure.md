# 项目模块规划

> 本文档详细规划AIHub项目的前端和后端模块结构。

## 📚 相关文档

- [项目总览](../README.md) - 项目主文档入口
- [系统架构总览](./overview.md) - 系统整体架构
- [网关架构设计](./gateways.md) - 网关详细设计

---

## 🎯 模块规划原则

1. **职责清晰** - 每个模块职责单一，边界明确
2. **低耦合高内聚** - 模块间依赖最小化，模块内功能相关
3. **可扩展** - 便于后续功能扩展
4. **可复用** - 公共功能抽取为公共模块
5. **易维护** - 代码组织清晰，便于维护

---

## 📦 后端模块规划

### 整体结构

```
aihub-backend/
├── aihub-api/                    # 主应用模块（启动入口）
├── aihub-common/                  # 公共模块
│   ├── common-core/              # 核心工具类
│   ├── common-security/          # 安全相关
│   ├── common-web/               # Web相关
│   └── common-exception/         # 异常处理
├── aihub-gateway/                 # 网关模块
│   ├── agent-gateway/            # Agents网关
│   ├── model-gateway/            # 模型网关
│   └── mcp-gateway/              # MCP网关
├── aihub-service/                 # 业务服务模块
│   ├── user-service/             # 用户服务
│   ├── model-service/            # 模型管理服务
│   ├── agent-service/            # Agent服务
│   ├── prompt-service/           # Prompt服务
│   ├── mcp-service/               # MCP服务
│   ├── rag-service/              # RAG服务
│   ├── permission-service/       # 权限服务
│   └── monitoring-service/        # 监控服务
└── pom.xml                        # Maven父POM
```

### 1. aihub-api（主应用模块）

**职责**: 应用启动入口，整合所有模块

**结构**:
```
aihub-api/
├── src/main/java/com/aihub/
│   ├── AihubApplication.java     # 启动类
│   └── config/                   # 全局配置
│       ├── SwaggerConfig.java
│       ├── WebConfig.java
│       └── ...
└── src/main/resources/
    ├── application.yml
    └── application-dev.yml
```

**依赖**:
- 所有service模块
- 所有gateway模块
- common模块

### 2. aihub-common（公共模块）

#### 2.1 common-core（核心工具类）

**职责**: 提供核心工具类和基础功能

**包含**:
- 常量定义
- 工具类（StringUtils、DateUtils等）
- 基础实体类（BaseEntity）
- 通用枚举

#### 2.2 common-security（安全相关）

**职责**: 认证授权相关功能

**包含**:
- JWT工具类
- 密码加密工具
- 权限注解
- 安全配置

#### 2.3 common-web（Web相关）

**职责**: Web层公共功能

**包含**:
- 统一响应格式（Result）
- 统一异常处理（GlobalExceptionHandler）
- 请求拦截器
- 跨域配置

#### 2.4 common-exception（异常处理）

**职责**: 异常定义和处理

**包含**:
- 业务异常类
- 异常码定义
- 异常处理器

### 3. aihub-gateway（网关模块）

#### 3.1 agent-gateway（Agents网关）

**职责**: Agent请求的统一入口

**结构**:
```
agent-gateway/
├── controller/
│   └── AgentGatewayController.java
├── service/
│   ├── AgentRouteService.java      # 路由服务
│   ├── AgentLoadBalancer.java      # 负载均衡
│   └── AgentRateLimiter.java       # 限流
├── filter/
│   ├── AuthFilter.java              # 认证过滤
│   └── RateLimitFilter.java        # 限流过滤
└── config/
    └── AgentGatewayConfig.java
```

#### 3.2 model-gateway（模型网关）

**职责**: 模型调用的统一入口

**结构**:
```
model-gateway/
├── controller/
│   └── ModelGatewayController.java
├── service/
│   ├── ModelRouteService.java      # 模型路由
│   ├── ModelAdapter.java           # 模型适配器接口
│   ├── adapter/
│   │   ├── OpenAIModelAdapter.java
│   │   ├── ClaudeModelAdapter.java
│   │   └── DeepSeekModelAdapter.java
│   ├── TokenCalculator.java        # Token计算
│   └── ModelLoadBalancer.java      # 负载均衡
└── config/
    └── ModelGatewayConfig.java
```

#### 3.3 mcp-gateway（MCP网关）

**职责**: MCP工具调用的统一入口

**结构**:
```
mcp-gateway/
├── controller/
│   └── MCPGatewayController.java
├── service/
│   ├── MCPRouteService.java        # 工具路由
│   ├── MCPConnectionPool.java      # 连接池
│   ├── MCPClient.java              # MCP客户端
│   └── MCPToolInvoker.java         # 工具调用器
└── config/
    └── MCPGatewayConfig.java
```

### 4. aihub-service（业务服务模块）

#### 4.1 user-service（用户服务）

**职责**: 用户管理相关功能

**结构**:
```
user-service/
├── controller/
│   └── UserController.java
├── service/
│   ├── UserService.java
│   └── AuthService.java
├── mapper/
│   └── UserMapper.java
└── entity/
    └── User.java
```

#### 4.2 model-service（模型管理服务）

**职责**: 模型配置管理

**结构**:
```
model-service/
├── controller/
│   └── ModelController.java
├── service/
│   ├── ModelService.java
│   └── ModelConfigService.java
├── mapper/
│   └── ModelMapper.java
└── entity/
    └── ModelConfig.java
```

#### 4.3 agent-service（Agent服务）

**职责**: Agent模板和实例管理、Agent执行引擎

**结构**:
```
agent-service/
├── controller/
│   └── AgentController.java
├── service/
│   ├── AgentTemplateService.java   # 模板管理
│   ├── AgentInstanceService.java   # 实例管理
│   ├── AgentExecutor.java          # 执行引擎（核心）
│   │   ├── ReactModeExecutor.java  # React模式执行器
│   │   └── SimpleModeExecutor.java # 简单模式执行器
│   └── AgentOptimizer.java         # 性能优化器
│       ├── ContextCompressor.java  # 上下文压缩
│       ├── ToolCache.java          # 工具缓存
│       └── PlanFirstStrategy.java  # 规划先行策略
├── mapper/
│   ├── AgentTemplateMapper.java
│   └── AgentInstanceMapper.java
└── entity/
    ├── AgentTemplate.java
    └── AgentInstance.java
```

#### 4.4 prompt-service（Prompt服务）

**职责**: Prompt管理

**结构**:
```
prompt-service/
├── controller/
│   └── PromptController.java
├── service/
│   ├── PromptService.java
│   └── PromptVersionService.java
├── mapper/
│   └── PromptMapper.java
└── entity/
    └── Prompt.java
```

#### 4.5 mcp-service（MCP服务）

**职责**: MCP Server和工具管理

**结构**:
```
mcp-service/
├── controller/
│   └── MCPController.java
├── service/
│   ├── MCPServerService.java
│   └── MCPToolService.java
├── mapper/
│   ├── MCPServerMapper.java
│   └── MCPToolMapper.java
└── entity/
    ├── MCPServer.java
    └── MCPTool.java
```

#### 4.6 rag-service（RAG服务）

**职责**: RAG库管理和检索

**结构**:
```
rag-service/
├── controller/
│   └── RAGController.java
├── service/
│   ├── RAGLibraryService.java
│   ├── DocumentService.java       # 文档管理
│   ├── VectorService.java          # 向量化服务
│   └── SearchService.java          # 检索服务
├── mapper/
│   ├── RAGLibraryMapper.java
│   └── DocumentMapper.java
└── entity/
    ├── RAGLibrary.java
    └── Document.java
```

#### 4.7 permission-service（权限服务）

**职责**: 权限管理

**结构**:
```
permission-service/
├── controller/
│   └── PermissionController.java
├── service/
│   ├── RoleService.java
│   ├── PermissionService.java
│   └── ApiKeyService.java
├── mapper/
│   ├── RoleMapper.java
│   └── PermissionMapper.java
└── entity/
    ├── Role.java
    └── Permission.java
```

#### 4.8 monitoring-service（监控服务）

**职责**: 监控和统计

**结构**:
```
monitoring-service/
├── controller/
│   └── MonitoringController.java
├── service/
│   ├── TokenStatService.java       # Token统计
│   ├── PerformanceService.java     # 性能监控
│   └── AlertService.java           # 告警服务
├── mapper/
│   └── TokenStatMapper.java
└── entity/
    └── TokenStat.java
```

---

## 🎨 前端模块规划

### 整体结构

```
aihub-frontend/
├── src/
│   ├── api/                       # API接口定义
│   ├── components/                # 组件
│   │   ├── layout/                # 布局组件
│   │   ├── common/                # 通用组件
│   │   └── business/              # 业务组件
│   ├── views/                     # 页面视图
│   │   ├── dashboard/             # 仪表盘
│   │   ├── models/                # 模型管理
│   │   ├── agents/                # Agent管理
│   │   ├── prompts/               # Prompt管理
│   │   ├── mcp/                   # MCP管理
│   │   ├── rag/                   # RAG管理
│   │   ├── permissions/           # 权限管理
│   │   ├── monitoring/            # 监控统计
│   │   └── settings/              # 系统设置
│   ├── stores/                    # 状态管理
│   ├── composables/               # 组合式函数
│   ├── utils/                     # 工具函数
│   ├── types/                     # TypeScript类型
│   ├── router/                    # 路由配置
│   └── assets/                    # 静态资源
├── public/                        # 公共资源
└── package.json
```

### 1. api/（API接口定义）

**职责**: 统一管理所有API接口

**结构**:
```
api/
├── index.ts                       # API统一导出
├── request.ts                     # Axios封装
├── types.ts                       # API类型定义
├── modules/
│   ├── auth.ts                    # 认证相关
│   ├── model.ts                   # 模型相关
│   ├── agent.ts                   # Agent相关
│   ├── prompt.ts                  # Prompt相关
│   ├── mcp.ts                     # MCP相关
│   ├── rag.ts                     # RAG相关
│   ├── permission.ts              # 权限相关
│   ├── monitoring.ts              # 监控相关
│   └── gateway.ts                 # 网关相关
└── interceptors/                  # 拦截器
    ├── request.ts
    └── response.ts
```

### 2. components/（组件）

#### 2.1 layout/（布局组件）

**包含**:
- `AppLayout.vue` - 主布局
- `Header.vue` - 顶部导航
- `Sidebar.vue` - 侧边栏
- `Breadcrumb.vue` - 面包屑
- `Footer.vue` - 底部

#### 2.2 common/（通用组件）

**包含**:
- `Table.vue` - 表格组件（封装）
- `Form.vue` - 表单组件（封装）
- `Dialog.vue` - 对话框组件
- `Pagination.vue` - 分页组件
- `SearchBar.vue` - 搜索栏
- `StatusTag.vue` - 状态标签
- `Loading.vue` - 加载组件
- `Empty.vue` - 空状态

#### 2.3 business/（业务组件）

**包含**:
- `AgentCard.vue` - Agent卡片
- `ModelCard.vue` - 模型卡片
- `PromptEditor.vue` - Prompt编辑器
- `RAGUploader.vue` - RAG文档上传
- `MCPToolCard.vue` - MCP工具卡片
- `TokenChart.vue` - Token统计图表
- `AgentExecutor.vue` - Agent执行器（测试用）

### 3. views/（页面视图）

#### 3.1 dashboard/（仪表盘）

**包含**:
- `index.vue` - 仪表盘主页
- `components/`
  - `StatCard.vue` - 统计卡片
  - `TokenChart.vue` - Token图表
  - `ModelUsageChart.vue` - 模型使用图表
  - `AlertList.vue` - 告警列表

#### 3.2 models/（模型管理）

**包含**:
- `index.vue` - 模型列表
- `create.vue` - 创建模型
- `edit.vue` - 编辑模型
- `detail.vue` - 模型详情
- `components/`
  - `ModelForm.vue` - 模型表单
  - `ModelTest.vue` - 模型测试

#### 3.3 agents/（Agent管理）

**包含**:
- `index.vue` - Agent列表
- `templates/` - Agent模板管理
  - `index.vue` - 模板列表
  - `create.vue` - 创建模板
- `create.vue` - 创建Agent（步骤式）
- `edit.vue` - 编辑Agent
- `detail.vue` - Agent详情
- `components/`
  - `AgentForm.vue` - Agent表单
  - `RAGSelector.vue` - RAG库选择器
  - `MCPToolSelector.vue` - MCP工具选择器
  - `PromptSelector.vue` - Prompt选择器
  - `AgentTester.vue` - Agent测试器

#### 3.4 prompts/（Prompt管理）

**包含**:
- `index.vue` - Prompt列表
- `create.vue` - 创建Prompt
- `edit.vue` - 编辑Prompt
- `detail.vue` - Prompt详情
- `components/`
  - `PromptEditor.vue` - Prompt编辑器
  - `PromptPreview.vue` - Prompt预览

#### 3.5 mcp/（MCP管理）

**包含**:
- `index.vue` - MCP工具列表
- `servers/` - MCP Server管理
  - `index.vue` - Server列表
  - `create.vue` - 创建Server
- `tools/` - 工具管理
  - `index.vue` - 工具列表
  - `detail.vue` - 工具详情
- `components/`
  - `MCPServerForm.vue` - Server表单
  - `ToolTester.vue` - 工具测试器

#### 3.6 rag/（RAG管理）

**包含**:
- `index.vue` - RAG库列表
- `create.vue` - 创建RAG库
- `edit.vue` - 编辑RAG库
- `detail.vue` - RAG库详情
- `components/`
  - `RAGForm.vue` - RAG表单
  - `DocumentUploader.vue` - 文档上传
  - `SearchTester.vue` - 检索测试

#### 3.7 permissions/（权限管理）

**包含**:
- `users/` - 用户管理
- `roles/` - 角色管理
- `api-keys/` - API Key管理

#### 3.8 monitoring/（监控统计）

**包含**:
- `token/` - Token统计
- `performance/` - 性能监控
- `alerts/` - 告警管理

#### 3.9 settings/（系统设置）

**包含**:
- `index.vue` - 系统设置
- `compliance/` - 合规配置
- `traffic/` - 流量配置

### 4. stores/（状态管理）

**结构**:
```
stores/
├── index.ts                       # Store统一导出
├── user.ts                        # 用户状态
├── app.ts                         # 应用状态
├── models/
│   ├── model.ts                   # 模型状态
│   └── modelGateway.ts            # 模型网关状态
├── agents/
│   ├── agent.ts                   # Agent状态
│   └── agentTemplate.ts           # 模板状态
└── ...
```

### 5. composables/（组合式函数）

**职责**: 可复用的逻辑封装

**包含**:
- `useTable.ts` - 表格逻辑封装
- `useForm.ts` - 表单逻辑封装
- `usePagination.ts` - 分页逻辑封装
- `useSearch.ts` - 搜索逻辑封装
- `useUpload.ts` - 上传逻辑封装
- `useWebSocket.ts` - WebSocket封装

### 6. utils/（工具函数）

**包含**:
- `request.ts` - HTTP请求封装
- `storage.ts` - 本地存储封装
- `format.ts` - 格式化工具
- `validate.ts` - 验证工具
- `constants.ts` - 常量定义

### 7. types/（TypeScript类型）

**结构**:
```
types/
├── index.ts                       # 类型统一导出
├── api.ts                         # API类型
├── model.ts                       # 数据模型类型
├── agent.ts                       # Agent相关类型
└── ...
```

---

## 🔗 模块依赖关系

### 后端模块依赖

```
aihub-api
  ├─→ aihub-service (所有服务模块)
  ├─→ aihub-gateway (所有网关模块)
  └─→ aihub-common (所有公共模块)

aihub-service
  ├─→ aihub-common
  └─→ 其他service模块（按需）

aihub-gateway
  ├─→ aihub-common
  └─→ 相关service模块
```

### 前端模块依赖

```
views/
  ├─→ components/
  ├─→ stores/
  ├─→ api/
  ├─→ composables/
  └─→ utils/

components/
  ├─→ stores/ (按需)
  └─→ utils/
```

---

## 📝 模块开发建议

### 后端开发建议

1. **按模块开发** - 先开发common模块，再开发service模块，最后开发gateway模块
2. **接口先行** - 先定义接口，再实现
3. **测试驱动** - 重要功能编写单元测试
4. **文档同步** - 代码和文档同步更新

### 前端开发建议

1. **组件化开发** - 先开发通用组件，再开发业务组件
2. **页面渐进** - 先实现静态页面，再对接API
3. **类型安全** - 充分利用TypeScript类型系统
4. **代码复用** - 使用composables封装可复用逻辑

---

## 🎯 模块优先级

### 第一阶段（MVP核心模块）

**后端**:
1. aihub-common（公共模块）
2. user-service（用户服务）
3. model-service（模型管理）
4. model-gateway（模型网关基础）
5. agent-service（Agent服务基础）

**前端**:
1. 基础布局和路由
2. 模型管理页面
3. Agent管理页面（基础）

### 第二阶段（完整功能）

**后端**:
6. agent-gateway（Agents网关）
7. mcp-service + mcp-gateway（MCP管理）
8. rag-service（RAG管理）
9. prompt-service（Prompt管理）

**前端**:
4. RAG/MCP/Prompt管理页面
5. 监控统计页面

### 第三阶段（增强功能）

**后端**:
10. permission-service（权限管理）
11. monitoring-service（监控服务）

**前端**:
6. 权限管理页面
7. 系统设置页面

---

## 📝 下一步

1. ✅ 完成模块规划（本文档）
2. ⏳ 创建后端项目结构
3. ⏳ 创建前端项目结构
4. ⏳ 开始开发第一阶段模块

