# [完整AI Agent平台] Implementation Plan

> **For agentic workers**: REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.
>
> **保存到**: `docs/superpowers/plans/2026-03-20-aihub-platform.md`
>
> **创建日期**：2026-03-20

---

## 目标

在6个月内完成企业级AI Agent编排平台，分3个阶段交付。每个阶段2个月，每个阶段都可独立使用，持续交付价值。

核心目标：
1. 解决AI应用的5大市场痛点：成本失控、数据安全、集成困难、技术门槛、合规风险、项目失败
2. 实现"乐高式组装"核心价值：可视化配置，业务人员无需编程
3. 为开源用户提供清晰的功能文档，只展示已实现功能

---

## 架构

采用混合架构：
- **前端**：Vue 3 + TypeScript + Element Plus（管理界面 + AI对话界面）
- **后端**：Java 17 + Spring Boot 3.2 + MyBatis Plus（后台管理 + 基础设施）
- **AI服务**：Python + FastAPI + LangChain（Agent能力 + RAG + MCP工具）
- **数据库**：MySQL 8.0 + Redis（数据持久化 + 缓存）
- **部署**：Docker Compose（私有化部署）

模块依赖关系：
- 前端依赖Java后端API
- Java后端通过HTTP调用Python AI服务
- Python服务通过Redis共享会话和Token统计
- 所有服务共享MySQL数据持久化

---

## 技术栈

- **前端**：Vue 3.5.26, TypeScript 5.9.3, Vite 7.3.0, Element Plus 2.13.0, Pinia 3.0.4
- **后端**：Java 17, Spring Boot 3.2.0, MyBatis Plus 3.5.5, Spring Security, Lombok, Redis
- **AI服务**：Python 3.10+, FastAPI, LangChain, Chroma, OpenAI SDK
- **数据库**：MySQL 8.0, Redis, Flyway（数据库迁移）
- **开发工具**：Git, Maven, pnpm, Docker Compose

---

## 任务结构

```
docs/superpowers/plans/2026-03-20-aihub-platform.md
├── 阶段1：降低技术门槛（2个月）- v1.0
│   ├── 任务1-1：模型管理
│   ├── 任务1-2：基础对话界面
│   ├── 任务1-3：Prompt模板管理
│   ├── 任务1-4：会话管理
│   └── 任务1-5：阶段1验收与发布
│
├── 阶段2：解决成本与集成（2个月）- v2.0
│   ├── 任务2-1：Token统计与成本控制
│   ├── 任务2-2：MCP工具集成
│   ├── 任务2-3：文档上传与解析
│   ├── 任务2-4：向量数据库集成
│   ├── 任务2-5：智能检索
│   ├── 任务2-6：知识库管理
│   └── 任务2-7：阶段2验收与发布
│
└── 阶段3：完整平台能力（2个月）- v3.0
    ├── 任务3-1：Agent模板系统
    ├── 任务3-2：Skills技能库
    ├── 任务3-3：内容合规检测
    └── 任务3-4：阶段3验收与发布
```

---

## 任务1-1：模型管理

### 目标

实现多厂商LLM模型管理，支持OpenAI、Claude、智谱、通义等主流模型，解决"成本失控"问题。

### 验收标准

- ✅ 用户可添加/编辑/删除模型
- ✅ 支持至少5个主流LLM厂商（含3个国产）
- ✅ 模型测试可用，显示响应时间、Token消耗
- ✅ API密钥加密存储
- ✅ 支持模型参数配置（temperature、max_tokens、top_p）

### 文件

**后端新增**：
- `aihub-ai-infrastructure/model/` - 模型管理模块
  - `ModelVendorService.java` - 模型厂商管理
  - `ModelConfigService.java` - 模型配置服务
  - `ModelTestService.java` - 模型测试服务
  - `ModelApiController.java` - 模型API控制器

**数据库新增**：
- `model_vendor` - 模型厂商表
- `model_config` - 模型配置表
- `model_api_key` - API密钥表（加密存储）

**前端新增**：
- `frontend/src/views/ai/model-config/` - 模型配置页面
  - `frontend/src/api/model.ts` - 模型API调用
  - `frontend/src/types/model.ts` - 模型类型定义

**文档更新**：
- 更新 `docs/mvp/v1.0-capabilities.md` - 添加模型管理功能说明

### 任务

- [ ] **任务1-1-1：设计数据库表结构**
  - Files: `model_vendor`, `model_config`, `model_api_key`
  - Create: Flyway migration script `V1.0.0__create_model_tables.sql`
  - 验证: 表结构设计合理，包含所有必要字段

- [ ] **任务1-1-2：实现后端模型厂商管理**
  - Files: `ModelVendorService.java`, `ModelVendorController.java`
  - 修改: 实现模型厂商的CRUD操作
  - 验证: 可正常添加、编辑、删除模型厂商

- [ ] **任务1-1-3：实现后端模型配置管理**
  - Files: `ModelConfigService.java`, `ModelConfigController.java`
  - 修改: 实现模型配置的CRUD操作
  - 验证: 可正常配置模型参数，支持多厂商

- [ ] **任务1-1-4：实现后端模型测试**
  - Files: `ModelTestService.java`, `ModelTestController.java`
  - 新增: 实现单轮对话测试功能
  - 验证: 可发送测试消息，显示响应时间和Token消耗

- [ ] **任务1-1-5：实现后端API密钥管理**
  - Files: `ModelApiKeyService.java`, `ModelApiKeyController.java`
  - 新增: 实现API密钥的加密存储、轮换策略
  - 验证: 密钥加密存储，支持轮换，多厂商支持

- [ ] **任务1-1-6：实现前端模型配置页面**
  - Files: `frontend/src/views/ai/model-config/index.vue`, `components/ModelForm.vue`, `components/ModelTest.vue`
  - 新增: 实现可视化模型配置界面
  - 验证: 可添加/编辑/删除模型，查看测试结果

- [ ] **任务1-1-7：实现前端模型API调用**
  - Files: `frontend/src/api/model.ts`, `composables/useModel.ts`
  - 新增: 实现与后端API的集成
  - 验证: 可正常调用模型配置API，模型切换正常

- [ ] **任务1-1-8：更新README文档**
  - Files: `README.md`
  - 修改: 在当前可用功能部分添加模型管理功能说明
  - 验证: README清晰标注v1.0新增功能

---

## 任务1-2：基础对话界面

### 目标

实现基础AI对话界面，支持SSE流式响应、多模型切换、会话历史，解决"技术门槛"问题。

### 验收标准

- ✅ 支持SSE流式响应，首字延迟<500ms
- ✅ 消息显示清晰，支持Markdown渲染、代码高亮
- ✅ 对话中可切换模型，上下文保留
- ✅ 会话列表显示最近对话，支持搜索
- ✅ 上下文长度自适应（4K/8K/16K）

### 文件

**后端新增**：
- `aihub-python/agents/` - 会话管理模块
  - `chat_session.py` - 会话管理
  - `chat_message.py` - 消息记录
  - `agent_orchestrator.py` - Agent编排器（处理模型切换、上下文管理）
  - `api/chat.py` - 对话API

**前端新增**：
- `frontend/src/views/ai/chat/` - 对话界面
  - `components/ChatWindow.vue` - 对话窗口组件
  - `components/MessageList.vue` - 消息列表组件
  - `components/ModelSelector.vue` - 模型选择器组件
  - `frontend/src/api/chat.ts` - 对话API调用
  - `frontend/src/stores/chat.ts` - 对话状态管理

**文档更新**：
- 更新 `docs/mvp/v1.0-capabilities.md` - 添加基础对话功能说明

### 任务

- [ ] **任务1-2-1：设计会话数据库表结构**
  - Files: `chat_session`, `chat_message`
  - Create: Flyway migration script `V1.0.0__create_chat_tables.sql`
  - 验证: 表结构设计合理，支持会话和消息存储

- [ ] **任务1-2-2：实现Python会话管理**
  - Files: `chat_session.py`, `chat_message.py`
  - 新增: 实现会话CRUD、会话列表、搜索、上下文管理
  - 验证: 可正常创建、查看、删除会话，搜索历史记录

- [ ] **任务1-2-3：实现Python Agent编排器**
  - Files: `agent_orchestrator.py`
  - 新增: 实现模型选择、上下文管理、Token统计、智能截断
  - 验证: 可正确处理模型切换，上下文长度自适应

- [ ] **任务1-2-4：实现Python对话API**
  - Files: `api/chat.py`
  - 新增: 实现SSE流式响应接口、WebSocket支持
  - 验证: SSE流式响应可用，首字延迟<500ms

- [ ] **任务1-2-5：实现前端对话界面**
  - Files: `frontend/src/views/ai/chat/index.vue`, `ChatWindow.vue`, `MessageList.vue`, `ModelSelector.vue`
  - 新增: 实现对话界面，消息显示，模型切换
  - 验证: 界面美观，交互流畅，模型切换无卡顿

- [ ] **任务1-2-6：实现前端对话状态管理**
  - Files: `frontend/src/stores/chat.ts`, `composables/useChat.ts`
  - 新增: 实现会话状态持久化
  - 验证: 刷新页面会话状态保留

- [ ] **任务1-2-7：更新README文档**
  - Files: `README.md`
  - 修改: 在当前可用功能部分添加基础对话功能说明
  - 验证: README清晰标注v1.0新增功能

---

## 任务1-3：Prompt模板管理

### 目标

实现Prompt模板库管理，支持变量、条件逻辑、模板分类，解决"技术门槛"问题，业务人员无需编写提示词。

### 验收标准

- ✅ 用户可创建/编辑/删除Prompt模板
- ✅ 支持变量占位符（{{变量名}}）
- ✅ 支持条件逻辑（{{#if condition}}...{{/if}}）
- ✅ 支持模板分类管理
- ✅ 模板测试功能可用（预览渲染）
- ✅ 至少10个内置模板（客服、分析、写作等）

### 文件

**后端新增**：
- `aihub-ai-infrastructure/prompt/` - Prompt管理模块
  - `PromptTemplateService.java` - Prompt模板服务
  - `PromptTemplateController.java` - Prompt模板控制器

**数据库新增**：
- `prompt_template` - Prompt模板表
- `prompt_category` - Prompt分类表

**前端新增**：
- `frontend/src/views/ai/prompt-templates/` - Prompt模板管理页面
  - `components/PromptEditor.vue` - Prompt编辑器组件
  - `components/PromptPreview.vue` - 模板预览组件
  - `frontend/src/api/prompt.ts` - Prompt API调用
  - `frontend/src/types/prompt.ts` - Prompt类型定义

**文档更新**：
- 更新 `docs/mvp/v1.0-capabilities.md` - 添加Prompt模板管理功能说明

### 任务

- [ ] **任务1-3-1：设计Prompt数据库表结构**
  - Files: `prompt_template`, `prompt_category`
  - Create: Flyway migration script `V1.0.0__create_prompt_tables.sql`
  - 验证: 表结构设计合理，包含模板内容、分类、变量

- [ ] **任务1-3-2：实现后端Prompt模板管理**
  - Files: `PromptTemplateService.java`, `PromptTemplateController.java`
  - 修改: 实现Prompt模板的CRUD操作
  - 验证: 可正常创建、编辑、删除Prompt模板

- [ ] **任务1-3-3：实现后端Prompt变量与条件逻辑**
  - Files: `PromptTemplateService.java`
  - 修改: 实现变量解析、条件逻辑判断
  - 验证: 支持{{变量名}}语法，支持条件逻辑

- [ ] **任务1-3-4：实现后端Prompt模板预览**
  - Files: `PromptTemplateService.java`, `PromptTemplateController.java`
  - 新增: 实现模板预览API
  - 验证: 可正常预览模板渲染结果

- [ ] **任务1-3-5：实现前端Prompt模板管理页面**
  - Files: `frontend/src/views/ai/prompt-templates/index.vue`, `PromptEditor.vue`, `PromptPreview.vue`
  - 新增: 实现可视化Prompt模板管理界面
  - 验证: 可正常创建、编辑、删除模板，预览模板

- [ ] **任务1-3-6：创建10个内置Prompt模板**
  - Files: `frontend/src/utils/built-in-prompts.ts` - 内置模板
  - 新增: 预置10个Prompt模板（客服、代码助手、数据分析等）
  - 验证: 内置模板可用，分类正确

- [ ] **任务1-3-7：更新README文档**
  - Files: `README.md`
  - 修改: 在当前可用功能部分添加Prompt模板管理功能说明
  - 验证: README清晰标注v1.0新增功能

---

## 任务1-4：会话管理

### 目标

实现会话历史与上下文管理，支持会话列表、搜索、上下文长度自适应，解决"技术门槛"问题。

### 验收标准

- ✅ 会话列表显示最近对话
- ✅ 会话历史可搜索
- ✅ 上下文长度自适应（4K/8K/16K）
- ✅ 支持会话导出（Markdown）
- ✅ 支持会话分享（生成分享链接）

### 文件

**后端新增**：
- 使用现有: `chat_session.py`, `chat_message.py`, `agent_orchestrator.py`
- 增强: `chat_session.py` - 新增会话导出、分享功能
- 新增: `chat_export.py` - 会话导出工具

**前端新增**：
- 使用现有: `frontend/src/views/ai/chat/` - 对话界面
- 增强: `MessageList.vue` - 新增搜索、导出、分享功能
- 增强: `frontend/src/api/chat.ts` - 新增导出、分享API调用

**文档更新**：
- 更新 `docs/mvp/v1.0-capabilities.md` - 添加会话管理功能说明

### 任务

- [ ] **任务1-4-1：增强Python会话管理（导出与分享）**
  - Files: `chat_session.py`
  - 修改: 新增会话导出（Markdown格式）、会话分享（生成链接）
  - 验证: 可正常导出会话记录，生成分享链接

- [ ] **任务1-4-2：增强前端会话列表功能**
  - Files: `frontend/src/views/ai/chat/index.vue`, `MessageList.vue`
  - 修改: 新增搜索框、导出按钮、分享按钮
  - 验证: 可正常搜索会话，导出会话，生成分享链接

- [ ] **任务1-4-3：更新README文档**
  - Files: `README.md`
  - 修改: 在当前可用功能部分添加会话管理功能说明
  - 验证: README清晰标注v1.0新增功能

---

## 任务1-5：阶段1验收与发布

### 目标

完成阶段1所有功能开发，进行验收测试，发布v1.0版本，让开源用户立即使用基础AI能力。

### 验收标准

- ✅ 所有11个功能开发完成
- ✅ 功能测试通过（单元测试 + 集成测试）
- ✅ 用户文档更新完成
- ✅ README.md更新完成
- ✅ v1.0版本标签发布（git tag）

### 文件

**文档新增**：
- `docs/mvp/v1.0-capabilities.md` - 阶段1完整功能文档（已完成）
- `docs/mvp/version-comparison.md` - 版本功能对比表
- `CHANGELOG.md` - v1.0版本变更日志

**测试**：
- Files: `tests/` - 测试目录
- 新增: `test_model_management.py` - 模型管理测试
- 新增: `test_chat_api.py` - 对话API测试
- 新增: `test_prompt_api.py` - Prompt API测试

**发布**：
- 命令: `git tag v1.0.0 && git push origin main`
- Files: `.gitignore` - 确保git忽略正确配置

### 任务

- [ ] **任务1-5-1：运行所有测试**
  - 命令: `pytest tests/ -v`
  - 验证: 所有测试通过
  - 验证标准: 测试覆盖率>80%，所有测试通过

- [ ] **任务1-5-2：创建v1.0发布说明**
  - Files: `docs/mvp/v1.0-capabilities.md`
  - 新增: 添加v1.0版本发布说明、更新日志
  - 验证: 发布说明清晰，包含功能列表、已知问题

- [ ] **任务1-5-3：发布v1.0版本**
  - 命令: `git tag v1.0.0 && git push origin main && git push --tags`
  - 验证: Git标签创建成功，版本发布成功

- [ ] **任务1-5-4：更新README主文档**
  - Files: `README.md`
  - 修改: 在当前可用功能部分添加v1.0标注，更新版本号
  - 验证: README清晰标注v1.0已发布，突出新增功能

---

## 任务2-1：Token统计与成本控制

### 目标

实现Token实时统计、成本计算、预算控制，解决"成本失控"问题，让企业CFO能够透明监控AI使用成本。

### 验收标准

- ✅ Token实时统计延迟<100ms
- ✅ 成本计算准确率100%
- ✅ 预算超限拦截100%
- ✅ 报告生成时间<30s
- ✅ 多维度统计（用户/部门/Agent/时间）

### 文件

**后端新增**：
- `aihub-ai-infrastructure/token/` - Token统计模块
  - `TokenUsageService.java` - Token使用统计服务
  - `TokenUsageController.java` - Token统计控制器
  - `CostCalculatorService.java` - 成本计算服务
  - `BudgetControlService.java` - 预算控制服务
  - `token_usage` - Token使用记录表
  - `cost_config` - 成本配置表
  - `budget_control` - 预算控制表

**前端新增**：
- `frontend/src/views/ai/cost-control/` - 成本控制页面
  - `frontend/src/api/token.ts` - Token API调用
  - `components/CostChart.vue` - 成本图表组件
  - `components/BudgetSetting.vue` - 预算设置组件
  - `frontend/src/stores/cost.ts` - 成本状态管理

**文档更新**：
- 更新 `docs/mvp/v2.0-capabilities.md` - 添加Token统计与成本控制功能说明
- 新增: `docs/mvp/cost-control-guide.md` - 成本控制使用指南

### 任务

- [ ] **任务2-1-1：设计Token统计数据库表结构**
  - Files: `token_usage`, `cost_config`, `budget_control`
  - Create: Flyway migration script `V2.0.0__create_token_tables.sql`
  - 验证: 表结构设计合理，支持多维度统计和预算控制

- [ ] **任务2-1-2：实现后端Token统计服务**
  - Files: `TokenUsageService.java`, `TokenUsageController.java`
  - 新增: 实现Token统计、成本计算、预算控制的完整后端逻辑
  - 验证: 可实时统计Token，生成报表

- [ ] **任务2-1-3：实现后端成本计算服务**
  - Files: `CostCalculatorService.java`, `CostCalculatorController.java`
  - 新增: 实现多模型定价配置、分层计费、成本计算
  - 验证: 成本计算准确，支持Input/Output分层计费

- [ ] **任务2-1-4：实现后端预算控制服务**
  - Files: `BudgetControlService.java`, `BudgetController.java`
  - 新增: 实现预算管理、超限拦截、预警通知
  - 验证: 预算超限时100%拦截，预警及时

- [ ] **任务2-1-5：实现前端成本控制页面**
  - Files: `frontend/src/views/ai/cost-control/index.vue`, `CostChart.vue`, `BudgetSetting.vue`
  - 新增: 实现可视化成本控制界面
  - 验证: 可查看Token统计、设置预算、查看成本报表

- [ ] **任务2-1-6：创建成本控制使用指南**
  - Files: `docs/mvp/cost-control-guide.md`
  - 新增: 详细说明如何使用成本控制功能
  - 验证: 指南清晰，用户可独立使用

---

## 任务2-2：MCP工具集成

### 目标

实现MCP协议支持、工具注册管理、50+预置工具，解决"集成困难"问题，降低企业集成现有系统的门槛。

### 验收标准

- ✅ MCP协议实现可用
- ✅ 工具注册管理功能完整
- ✅ 50+预置工具可用（文件操作、数据库查询、网络请求等）
- ✅ 工具调用成功率>95%

### 文件

**后端新增**：
- `aihub-python/mcp/` - MCP工具模块
  - `mcp_server.py` - MCP服务器
  - `mcp_tool.py` - MCP工具定义
  - `mcp_tool_registry.py` - 工具注册表
  - `api/mcp.py` - MCP API

**预置工具**：
- `aihub-python/mcp/tools/` - 工具实现目录
  - `file_operation_tool.py` - 文件操作工具
  - `database_query_tool.py` - 数据库查询工具
  - `http_request_tool.py` - HTTP请求工具
  - `shell_command_tool.py` - Shell命令工具
  - `data_processing_tool.py` - 数据处理工具（CSV/JSON转换）

**前端新增**：
- `frontend/src/views/ai/mcp-tools/` - MCP工具管理页面
- `frontend/src/api/mcp.ts` - MCP API调用
- `components/McpToolForm.vue` - MCP工具表单
- `components/ToolMarket.vue` - 工具市场展示

**文档更新**：
- 更新 `docs/mvp/v2.0-capabilities.md` - 添加MCP工具集成功能说明

### 任务

- [ ] **任务2-2-1：设计MCP工具数据库表结构**
  - Files: `mcp_tool`, `mcp_tool_registry`
  - Create: Flyway migration script `V2.0.0__create_mcp_tables.sql`
  - 验证: 表结构设计合理，支持工具定义和注册

- [ ] **任务2-2-2：实现后端MCP服务器**
  - Files: `mcp_server.py`
  - 新增: 实现MCP协议服务器，支持工具注册、调用
  - 验证: MCP协议正确实现，工具可正常调用

- [ ] **任务2-2-3：实现5个预置MCP工具**
  - Files: `file_operation_tool.py`, `database_query_tool.py`, `http_request_tool.py`, `shell_command_tool.py`
  - 新增: 实现完整的文件操作、数据库查询、网络请求、数据处理、CSV转换工具
  - 验证: 所有5个工具功能正常工作

- [ ] **任务2-2-4：实现后端MCP工具管理**
  - Files: `mcp_tool.py`, `mcp_tool_registry.py`, `api/mcp.py`
  - 新增: 实现工具注册、工具测试、工具市场
  - 验证: 可正常注册工具，查看工具列表

- [ ] **任务2-2-5：实现前端MCP工具管理页面**
  - Files: `frontend/src/views/ai/mcp-tools/index.vue`, `McpToolForm.vue`, `ToolMarket.vue`
  - 新增: 实现工具注册界面、工具测试界面、工具市场展示
  - 验证: 可正常注册自定义工具，测试工具可用，查看工具市场

- [ ] **任务2-2-6：创建MCP工具使用文档**
  - Files: `docs/mvp/mcp-usage-guide.md`
  - 新增: 详细说明如何注册和使用MCP工具
  - 验证: 文档清晰，用户可独立使用工具

---

## 任务2-3：文档上传与解析

### 目标

实现文档上传与解析功能，支持PDF、DOCX、TXT、MD、HTML等格式，解决"企业知识"痛点。

### 验收标准

- ✅ 支持至少5种文档格式（PDF、DOCX、TXT、MD、HTML）
- ✅ 解析准确率>95%
- ✅ 支持批量上传（10+文件）
- ✅ 解析时间<5秒/文档

### 文件

**后端新增**：
- `aihub-python/rag/` - RAG模块
  - `document_parser.py` - 文档解析器
  - `document_chunker.py` - 文档分块器
  - `api/document.py` - 文档API

**前端新增**：
- `frontend/src/views/ai/document-upload/` - 文档上传页面
- `frontend/src/api/document.ts` - 文档API调用
  - `components/DocumentUpload.vue` - 文档上传组件
  - `components/DocumentList.vue` - 文档列表组件

**文档更新**：
- 更新 `docs/mvp/v2.0-capabilities.md` - 添加文档上传与解析功能说明
- 新增: `docs/mvp/document-guide.md` - 文档上传使用指南

### 任务

- [ ] **任务2-3-1：设计文档数据库表结构**
  - Files: `document`, `document_chunk`
  - Create: Flyway migration script `V2.0.0__create_document_tables.sql`
  - 验证: 表结构设计合理，支持文档元数据和分块存储

- [ ] **任务2-3-2：实现后端文档解析**
  - Files: `document_parser.py`
  - 新增: 实现PDF、DOCX、TXT、MD、HTML格式的文档解析
  - 验证: 可正常解析多种格式，提取文本内容

- [ ] **任务2-3-3：实现后端文档分块**
  - Files: `document_chunker.py`
  - 新增: 实现智能文档分块（按段落、按语义）
  - 验证: 文档分块准确率>95%

- [ ] **任务2-3-4：实现后端文档API**
  - Files: `api/document.py`
  - 新增: 实现文档上传、列表、预览、删除的完整API
  - 验证: 可正常上传文档，查看文档列表，预览文档内容

- [ ] **任务2-3-5：实现前端文档上传界面**
  - Files: `frontend/src/views/ai/document-upload/index.vue`, `DocumentUpload.vue`, `DocumentList.vue`
  - 新增: 实现拖拽上传、进度显示、批量上传
  - 验证: 可正常上传文档，显示进度，支持批量操作

- [ ] **任务2-3-6：创建文档上传使用指南**
  - Files: `docs/mvp/document-guide.md`
  - 新增: 详细说明如何上传文档、支持格式、批量操作
  - 验证: 指南清晰，用户可独立使用

---

## 任务2-4：向量数据库集成

### 目标

集成向量数据库（Chroma/Milvus），实现文档自动向量化，支持语义检索。

### 验收标准

- ✅ 向量数据库集成可用
- ✅ 文档自动向量化时间<1秒/文档
- ✅ 向量索引创建时间<1秒/文档
- ✅ 支持向量检索（ANN搜索、余弦相似度）
- ✅ 向量库配置化（可切换Chroma/Milvus）

### 文件

**后端新增**：
- `aihub-ai-infrastructure/vector/` - 向量存储模块
  - `VectorStoreService.java` - 向量存储服务
  - `VectorStoreController.java` - 向量存储控制器

**Python配置**：
- Files: `aihub-python/requirements.txt` - 添加Chroma/Milvus依赖
- Files: `aihub-python/rag/vector_store.py` - 向量数据库集成
- Files: `aihub-python/rag/embedding.py` - 文档向量化

**前端新增**：
- 新增: `frontend/src/api/vector.ts` - 向量存储API调用
- 新增: `frontend/src/components/VectorConfig.vue` - 向量库配置组件

**文档更新**：
- 更新 `docs/mvp/v2.0-capabilities.md` - 添加向量数据库集成功能说明
- 新增: `docs/mvp/rag-guide.md` - RAG使用指南

### 任务

- [ ] **任务2-4-1：设计向量数据库集成方案**
  - Files: `aihub-ai-infrastructure/vector/VectorStoreService.java`
  - 新增: 设计向量存储架构，支持Chroma/Milvus切换
  - 验证: 架构设计合理，支持多向量库

- [ ] **任务2-4-2：实现Python文档向量化**
  - Files: `aihub-python/rag/embedding.py`
  - 新增: 实现文档自动向量化，支持多种embedding模型
  - 验证: 文档可正常向量化

- [ ] **任务2-4-3：实现Python向量数据库集成**
  - Files: `aihub-python/rag/vector_store.py`
  - 新增: 实现向量数据库CRUD、向量化、索引创建
  - 验证: 可正常添加文档、自动向量化、创建索引

- [ ] **任务2-4-4：实现前端向量库配置界面**
  - Files: `frontend/src/views/ai/vector-config/`, `VectorConfig.vue`
  - 新增: 实现向量库配置界面
  - 验证: 可配置向量库类型、测试连接性

---

## 任务2-5：智能检索

### 目标

实现智能检索功能，结合向量检索和关键词检索（BM25），提升检索准确率，实现"企业知识"价值。

### 验收标准

- ✅ 支持向量检索（ANN搜索、余弦相似度）
- ✅ 支持关键词检索（BM25）
- ✅ 支持混合检索（加权融合、RRF）
- ✅ 检索响应时间<500ms
- ✅ 检索准确率>80%

### 文件

**后端新增**：
- `aihub-python/rag/retriever.py` - 检索器
  - `api/retrieval.py` - 检索API

**前端新增**：
- `frontend/src/views/ai/retrieval-test/` - 检索测试页面
- `frontend/src/api/retrieval.ts` - 检索API调用
- `components/RetrievalResult.vue` - 检索结果展示组件

**文档更新**：
- 更新 `docs/mvp/v2.0-capabilities.md` - 添加智能检索功能说明

### 任务

- [ ] **任务2-5-1：设计检索算法**
  - Files: `aihub-python/rag/retriever.py`
  - 新增: 实现向量检索（ANN）、BM25检索、混合检索、重排序
  - 验证: 检索算法正确实现

- [ ] **任务2-5-2：实现后端检索API**
  - Files: `api/retrieval.py`
  - 新增: 实现检索接口、参数配置、结果排序
  - 验证: 可正常检索，支持多种检索策略

- [ ] **任务2-5-3：实现前端检索测试界面**
  - Files: `frontend/src/views/ai/retrieval-test/`, `RetrievalResult.vue`
  - 新增: 实现检索测试界面，支持不同检索策略测试
  - 验证: 可测试不同检索策略，查看检索结果

- [ ] **任务2-5-4：创建RAG使用指南**
  - Files: `docs/mvp/rag-guide.md`
  - 新增: 详细说明如何配置检索、上传文档、使用检索功能
  - 验证: 指南清晰，用户可独立使用RAG功能

---

## 任务2-6：知识库管理

### 目标

实现知识库CRUD、分类、权限控制，支持企业知识沉淀，解决"企业知识"问题。

### 验收标准

- ✅ 知识库CRUD完整
- ✅ 支持知识库分类管理
- ✅ 支持知识库权限控制（读取/编辑/删除）
- ✅ 文档与知识库关联正常
- ✅ 搜索功能可用

### 文件

**后端新增**：
- `aihub-ai-infrastructure/vector/KnowledgeBaseService.java` - 知识库服务
- `KnowledgeBaseController.java` - 知识库控制器

**前端新增**：
- `frontend/src/views/ai/knowledge-base/` - 知识库管理页面
- `components/KnowledgeBaseForm.vue` - 知识库表单
- `components/KnowledgeBaseList.vue` - 知识库列表组件

**文档更新**：
- 更新 `docs/mvp/v2.0-capabilities.md` - 添加知识库管理功能说明

### 任务

- [ ] **任务2-6-1：设计知识库数据库表结构**
  - Files: `knowledge_base`, `document`, `document_tag`
  - Create: Flyway migration script `V2.0.0__create_knowledge_base_tables.sql`
  - 验证: 表结构设计合理，支持知识库和文档关联

- [ ] **任务2-6-2：实现后端知识库管理**
  - Files: `KnowledgeBaseService.java`, `KnowledgeBaseController.java`
  - 新增: 实现知识库CRUD、分类、权限控制
  - 验证: 可正常创建、编辑、删除知识库，管理分类

- [ ] **任务2-6-3：实现前端知识库管理页面**
  - Files: `frontend/src/views/ai/knowledge-base/index.vue`, `KnowledgeBaseForm.vue`, `KnowledgeBaseList.vue`
  - 新增: 实现可视化知识库管理界面
  - 验证: 可正常管理知识库、分类、文档关联

- [ ] **任务2-6-4：更新README文档**
  - Files: `README.md`
  - 修改: 在当前可用功能部分添加知识库管理功能说明
  - 验证: README清晰标注v2.0新增功能

---

## 任务2-7：阶段2验收与发布

### 目标

完成阶段2所有功能开发（12个功能），进行验收测试，发布v2.0版本，提供Token统计和MCP工具生态。

### 验收标准

- ✅ 所有12个功能开发完成
- ✅ 功能测试通过
- ✅ 用户文档更新完成
- ✅ README.md更新完成
- ✅ v2.0版本标签发布

### 文件

**文档新增**：
- `docs/mvp/v2.0-capabilities.md` - 阶段2完整功能文档（已完成）
- `docs/mvp/version-comparison.md` - 更新版本功能对比表
- `CHANGELOG.md` - v2.0版本变更日志

**测试**：
- Files: `tests/` - 测试目录
- 新增: `test_token_statistics.py` - Token统计测试
- 新增: `test_mcp_tools.py` - MCP工具测试
- 新增: `test_document_parser.py` - 文档解析测试
- 新增: `test_vector_store.py` - 向量存储测试
- 新增: `test_retriever.py` - 检索功能测试

**发布**：
- 命令: `git tag v2.0.0 && git push origin main && git push --tags`
- Files: `.gitignore` - 确保git忽略正确配置

### 任务

- [ ] **任务2-7-1：运行所有测试**
  - 命令: `pytest tests/ -v`
  - 验证: 所有测试通过
  - 验证标准: 测试覆盖率>80%，所有测试通过

- [ ] **任务2-7-2：创建v2.0发布说明**
  - Files: `docs/mvp/v2.0-capabilities.md`
  - 新增: 添加v2.0版本发布说明、更新日志
  - 验证: 发布说明清晰，包含功能列表、已知问题

- [ ] **任务2-7-3：发布v2.0版本**
  - 命令: `git tag v2.0.0 && git push origin main && git push --tags`
  - 验证: Git标签创建成功，版本发布成功

- [ ] **任务2-7-4：更新README主文档**
  - Files: `README.md`
  - 修改: 在当前可用功能部分添加v2.0标注，更新版本号
  - 验证: README清晰标注v2.0已发布，突出新增功能

---

## 任务3-1：Agent模板系统

### 目标

实现Agent模板系统，包括模板定义、实例化引擎、模板继承、5个内置模板，实现"乐高式组装"核心价值。

### 验收标准

- ✅ 用户可创建/编辑/删除Agent模板
- ✅ 模板可配置6大组件（模型、RAG、工具、Skills、Prompt）
- ✅ 支持模板继承（单继承、多继承、Mixin）
- ✅ 5个内置模板可用且可运行
- ✅ 模板实例化引擎可用
- ✅ 模板管理UI可视化

### 文件

**后端新增**：
- `aihub-ai-applications/agent/` - Agent应用模块
  - `AgentTemplateService.java` - Agent模板服务
  - `AgentTemplateController.java` - Agent模板控制器
  - `AgentInstanceService.java` - Agent实例服务
  - `AgentInstanceController.java` - Agent实例控制器
  - `agent_template` - Agent模板表
  - `agent_template_component` - 模板组件表
  - `agent_instance` - Agent实例表

**前端新增**：
- `frontend/src/views/ai/agent-template/` - Agent模板管理页面
- `components/AgentTemplateForm.vue` - 模板表单组件
- `components/ComponentSelector.vue` - 6大组件选择器
- `components/AgentPreview.vue` - Agent预览组件
- `components/TemplateList.vue` - 模板列表组件
- `frontend/src/api/agent.ts` - Agent API调用
- `frontend/src/types/agent.ts` - Agent类型定义

**文档更新**：
- 更新 `docs/mvp/v3.0-capabilities.md` - 添加Agent模板系统功能说明
- 新增: `docs/mvp/agent-creation-guide.md` - Agent创建教程
- 新增: `docs/mvp/built-in-templates.md` - 内置模板说明文档

### 任务

- [ ] **任务3-1-1：设计Agent模板数据库表结构**
  - Files: `agent_template`, `agent_template_component`, `agent_instance`
  - Create: Flyway migration script `V3.0.0__create_agent_tables.sql`
  - 验证: 表结构设计合理，支持模板、组件、实例存储

- [ ] **任务3-1-2：实现后端Agent模板管理**
  - Files: `AgentTemplateService.java`, `AgentTemplateController.java`
  - 修改: 实现Agent模板的CRUD操作
  - 验证: 可正常创建、编辑、删除Agent模板

- [ ] **任务3-1-3：实现后端模板组件管理**
  - Files: `AgentTemplateService.java`
  - 修改: 实现模板组件的CRUD操作
  - 验证: 可正常管理6大组件

- [ ] **任务3-1-4：实现后端Agent实例化引擎**
  - Files: `AgentInstanceService.java`
  - 新增: 实现从模板创建Agent实例的逻辑
  - 验证: 可正常从模板创建Agent实例

- [ ] **任务3-1-5：实现5个内置Agent模板**
  - Files: `frontend/src/utils/built-in-templates.ts`
  - 新增: 预置5个模板的配置文件
  - 验证: 5个模板可用，分类正确

- [ ] **任务3-1-6：实现前端Agent模板管理页面**
  - Files: `frontend/src/views/ai/agent-template/index.vue`, `AgentTemplateForm.vue`, `ComponentSelector.vue`, `AgentPreview.vue`, `TemplateList.vue`
  - 新增: 实现可视化Agent模板管理界面
  - 验证: 可正常创建/编辑模板，预览Agent效果

- [ ] **任务3-1-7：创建Agent创建教程**
  - Files: `docs/mvp/agent-creation-guide.md`
  - 新增: 详细说明如何创建Agent的完整流程
  - 验证: 教程清晰，用户可独立创建Agent

- [ ] **任务3-1-8：创建内置模板说明文档**
  - Files: `docs/mvp/built-in-templates.md`
  - 新增: 详细说明5个内置模板的配置和使用方法
  - 验证: 文档详细，用户可独立使用内置模板

---

## 任务3-2：Skills技能库

### 目标

实现Skills技能库，包括技能管理、分类、10+预置技能，降低开发成本，解决"技术门槛"问题。

### 验收标准

- ✅ 用户可创建/编辑/删除Skills技能
- ✅ 支持技能分类管理
- ✅ 10+预置Skills可用（翻译、代码生成、数据分析等）
- ✅ Skills可组合使用

### 文件

**后端新增**：
- `aihub-ai-applications/skills/` - Skills模块
  - `SkillLibraryService.java` - Skills库服务
  - `SkillLibraryController.java` - Skills库控制器
  - `skill_library` - Skills库表
  - `skill_category` - Skills分类表

**预置Skills**：
- `aihub-python/skills/presets/` - 预置Skills目录
  - `translation_skill.py` - 翻译技能
  - `code_generation_skill.py` - 代码生成技能
  - `data_analysis_skill.py` - 数据分析技能
  - `text_summary_skill.py` - 文本摘要技能
  - 情感分析技能
  - `ner_skill.py` - 实体识别技能
  - `keyword_extraction_skill.py` - 关键词提取技能
  - `data_cleaning_skill.py` - 数据清洗技能
  - `format_conversion_skill.py` - 格式转换技能
  - `document_parsing_skill.py` - 文档解析技能

**前端新增**：
- `frontend/src/views/ai/skills/` - Skills管理页面
- `components/SkillForm.vue` - Skill表单
- `components/SkillList.vue` - Skills列表组件

**文档更新**：
- 更新 `docs/mvp/v3.0-capabilities.md` - 添加Skills技能库功能说明

### 任务

- [ ] **任务3-2-1：设计Skills数据库表结构**
  - Files: `skill_library`, `skill_category`
  - Create: Flyway migration script `V3.0.0__create_skills_tables.sql`
  - 验证: 表结构设计合理，支持技能和分类存储

- [ ] **任务3-2-1：实现后端Skills库服务**
  - Files: `SkillLibraryService.java`, `SkillLibraryController.java`
  - 新增: 实现Skills库的CRUD操作
  - 验证: 可正常创建、编辑、删除Skills技能

- [ ] **任务3-2-2：实现10个预置Skills**
  - Files: 所有预置Skills Python文件
  - 新增: 实现所有10个预置技能的完整功能
  - 验证: 所有技能可用，功能完整

- [ ] **任务3-2-3：实现前端Skills管理页面**
  - Files: `frontend/src/views/ai/skills/index.vue`, `SkillForm.vue`, `SkillList.vue`
  - 新增: 实现可视化Skills管理界面
  - 验证: 可正常管理Skills、查看技能列表

- [ ] **任务3-2-4：更新README文档**
  - Files: `README.md`
  - 修改: 在当前可用功能部分添加Skills技能库功能说明
  - 验证: README清晰标注v3.0新增功能

---

## 任务3-3：内容合规检测

### 目标

实现内容安全检测，包括敏感词过滤、AI内容安全API集成、RBAC权限、审计日志，解决"合规风险"问题。

### 验收标准

- ✅ 敏感词过滤准确率>98%
- ✅ 内容安全检测延迟<500ms
- ✅ 权限验证准确率100%
- ✅ 审计日志完整性100%

### 文件

**后端新增**：
- `aihub-ai-infrastructure/compliance/` - 合规检测模块
  - `SensitiveWordService.java` - 敏感词服务
  - `ContentSafetyService.java` - 内容安全检测服务
  - `PermissionService.java` - 权限验证服务
  - `AuditLogService.java` - 审计日志服务
  - `sensitive_word` - 敏感词表
  - `sensitive_word_category` - 敏感词分类
  - `content_safety_log` - 内容安全日志
  - `permission_audit_log` - 权限审计日志

**前端新增**：
- `frontend/src/views/system/compliance/` - 合规管理页面
- `components/SensitiveWordEditor.vue` - 敏感词编辑器
- `components/AuditLogViewer.vue` - 审计日志查看器
- `components/PermissionManager.vue` - 权限管理器

**文档更新**：
- 更新 `docs/mvp/v3.0-capabilities.md` - 添加内容合规检测功能说明

### 任务

- [ ] **任务3-3-1：设计合规检测数据库表结构**
  - Files: 所有合规相关表
  - Create: Flyway migration script `V3.0.0__create_compliance_tables.sql`
  - 验证: 表结构设计合理，支持所有合规功能

- [ ] **任务3-3-2：实现后端敏感词过滤**
  - Files: `SensitiveWordService.java`, `SensitiveWordController.java`
  - 新增: 实现敏感词库管理、多模式匹配
  - 验证: 敏感词过滤准确率>98%

- [ ] **任务3-3-3：实现后端内容安全检测**
  - Files: `ContentSafetyService.java`, `ContentSafetyController.java`
  - 新增: 实现AI内容安全API集成（阿里云/腾讯云）
  - 验证: 内容安全检测可用，实时拦截违规内容

- [ ] **任务3-3-4：实现后端权限验证**
  - Files: `PermissionService.java`, `PermissionController.java`
  - 新增: 实现RBAC权限验证、权限缓存
  - 验证: 权限验证准确率100%

- [ ] **任务3-3-5：实现后端审计日志**
  - Files: `AuditLogService.java`, `AuditLogController.java`
  - 新增: 实现操作日志记录、查询、异常告警
  - 验证: 审计日志完整性100%，异常及时告警

- [ ] **任务3-3-6：实现前端合规管理页面**
  - Files: `frontend/src/views/system/compliance/index.vue`, `SensitiveWordEditor.vue`, `AuditLogViewer.vue`, `PermissionManager.vue`
  - 新增: 实现完整的合规管理界面
  - 验证: 可管理敏感词、查看审计日志、配置权限

- [ ] **任务3-3-7：更新README文档**
  - Files: `README.md`
  - 修改: 在当前可用功能部分添加内容合规检测功能说明
  - 验证: README清晰标注v3.0新增功能

---

## 任务3-4：阶段3验收与发布

### 目标

完成阶段3所有功能开发（11个功能），进行验收测试，发布v3.0版本，实现完整"乐高式组装"企业级AI Agent平台。

### 验收标准

- ✅ 所有11个功能开发完成
- ✅ 功能测试通过
- ✅ 用户文档更新完成
- ✅ README.md更新完成
- ✅ v3.0版本标签发布

### 文件

**文档新增**：
- `docs/mvp/v3.0-capabilities.md` - 阶段3完整功能文档（已完成）
- `docs/mvp/version-comparison.md` - 更新版本功能对比表（添加v3.0行）
- `CHANGELOG.md` - v3.0版本变更日志

**测试**：
- Files: `tests/` - 测试目录
- 新增: `test_agent_template.py` - Agent模板系统测试
- 新增: `test_skills_library.py` - Skills库测试
- 新增: `test_compliance.py` - 合规检测测试
- 新增: `test_integration.py` - 集成测试

**发布**：
- 命令: `git tag v3.0.0 && git push origin main && git push --tags`
- Files: `.gitignore` - 确保git忽略正确配置

### 任务

- [ ] **任务3-4-1：运行所有测试**
  - 命令: `pytest tests/ -v`
  - 验证: 所有测试通过
  - 验证标准: 测试覆盖率>80%，所有测试通过

- [ ] **任务3-4-2：创建v3.0发布说明**
  - Files: `docs/mvp/v3.0-capabilities.md`
  - 新增: 添加v3.0版本发布说明、更新日志
  - 验证: 发布说明清晰，包含完整功能列表

- [ ] **任务3-4-3：发布v3.0版本**
  - 命令: `git tag v3.0.0 && git push origin main && git push --tags`
  - 验证: Git标签创建成功，版本发布成功

- [ ] **任务3-4-4：更新README主文档**
  - Files: `README.md`
  - 修改: 添加完整的AIHub功能列表（v1.0+v2.0+v3.0）
  - 修改: 更新版本号为v3.0，标注完整平台能力
  - 验证: README清晰标注v3.0已发布，突出"乐高式组装"核心价值

---

## 关键原则

1. **问题驱动开发**：每个阶段都针对明确的痛点，验证标准可衡量
2. **阶段独立可用**：每个阶段2个月完成后都可独立使用，用户无需等待
3. **持续交付价值**：每个里程碑都可验证用户价值，快速迭代
4. **文档同步更新**：每个版本发布后立即更新文档，保持信息同步
5. **技术债务管理**：每个阶段结束前评估技术债务，优先修复阻塞性能问题
6. **开源友好**：用户文档只展示已实现功能，规划文档单独管理

---

> 本计划基于 `docs/mvp/development-plan.md` 制定，确保每个阶段都解决核心问题。
