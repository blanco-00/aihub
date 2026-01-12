# AI基础设施平台

> 构建企业级AI基础设施，统一管理模型、密钥、权限、Prompts、MCP与Agent工作流，降低AI应用开发复杂度，支持快速构建AI应用。

## 🎯 项目愿景

**一句话定位**: _"构建企业级AI基础设施，统一管理模型、密钥、权限、Prompts、MCP与Agent工作流，降低AI应用开发复杂度，支持快速构建AI应用"_

## ✨ 核心价值

- 解决企业或团队等多人使用AI的各类痛点
- 进则争取打磨产品（AI基础设施、AI SAAS通用服务、企业级AI平台）
- 退则学习AI技术，为AI研发岗位积累

## 🚀 核心功能

1. **模型快速接入、切换** - 配置化接入，模型网关、动态路由策略
2. **权限颗粒化管理** - API-KEY管理 + 服务订阅，三级权限控制
3. **LLM Token消耗追踪** - 精细化token消耗追踪，部门、项目、人分解消耗
4. **内容合规检测** - 敏感词库、不合规引导检测、违规内容实时拦截
5. **Token优化** - 压缩冗余字段，超过模型tokens阈值返回正确错误信息
6. **突发流量防护** - 按部门/项目划分资源池，超额请求自动进入队列或降级服务
7. **法律合规审计追踪** - 全链路溯源，自动生成合规报告
8. **知识资产沉淀** - Prompt库管理、Agent工作流市场、MCP server市场
9. **故障自愈能力** - 自动切换备用模型，通知值班人员，生成故障分析报告
10. **打破数据孤岛** - 市场/客服/研发部门共享同一套AI基础设施

## 📋 项目阶段

- **MVP阶段**：最小可行产品
- **AI基础设施**：具备基础AI管理能力
- **AI SAAS通用服务**：快速开发saas化的AI服务
- **企业级AI平台**：为企业提供快速使用AI服务的能力

## 🛠️ 技术栈

### 前端
- **Vue 3** + TypeScript
- **Element Plus** (UI组件库)
- **Vite** (构建工具)
- **Pinia** (状态管理)
- **Vue Router** (路由)
- **Axios** (HTTP客户端)

### 后端
- **Java** (Spring Boot) - 主要后端框架
- **Python** - 仅在必要场景使用（如AI模型调用、数据处理等）
- **MySQL** - 主数据库（推荐MySQL 8.0+）
- **Redis** - 缓存和会话存储
- **JWT** - 认证授权

## 📁 项目结构

```
AIHub/
├── README.md           # 项目主文档（本文件）- 文档入口
├── LICENSE             # MIT 开源许可证
├── .cursor/
│   └── rules/          # Cursor IDE 开发规范
│       ├── java-code-style.mdc    # Java 代码规范
│       ├── logging.mdc            # 日志规范
│       ├── documentation.mdc      # 文档规范
│       ├── rule-management.mdc    # 规范管理
│       └── frontend-ui-design.mdc # 前端UI/UX设计规范
├── docs/              # 项目文档
│   ├── architecture/  # 架构设计
│   ├── frontend/      # 前端文档
│   └── backend/       # 后端文档
├── frontend/          # 前端代码（待创建）
└── backend/           # 后端代码（待创建）
```

> **注意**: 本文档（README.md）是项目的唯一文档入口，所有文档都应该直接或间接地与本文档保持引用关系。

## 📚 文档导航

### 🎨 前端文档
- **[前端开发指南](./docs/frontend/guide.md)** - 前端技术栈、项目结构、开发规范
- **[UI/UX设计文档](./docs/frontend/design.md)** - 视觉风格、布局设计、交互设计
- **[页面规划文档](./docs/frontend/pages.md)** - 所有页面的详细功能规划
- **[组件设计文档](./docs/frontend/components.md)** - 组件分类、组件设计、开发规范
- **[API对接文档](./docs/frontend/api.md)** - 前端API接口定义、对接方式、错误处理

### ⚙️ 后端文档
- **[后端开发指南](./docs/backend/guide.md)** - 后端技术栈、项目结构、开发规范
- **[数据库设计文档](./docs/backend/database.md)** - 数据库选型、表结构设计、设计规范
- **[系统初始化文档](./docs/backend/initialization.md)** - 数据库初始化、超级管理员创建
- **[SQL 脚本管理](./docs/sql/guide.md)** - SQL 脚本存放方式和管理规范
- API设计文档（待完善）

### 🎯 方案设计
- **[Agent模板化方案](./docs/agent-template-solution.md)** - Agent模板化方案设计、架构和实现计划
- **[系统架构总览](./docs/architecture/overview.md)** - 系统整体架构设计
- **[网关架构设计](./docs/architecture/gateways.md)** - Agents网关、模型网关、MCP网关详细

### ✨ 功能文档
- **[AI功能清单](./docs/features.md)** - 已实现功能和未来规划完整清单

### 🏗️ 架构文档
- **[系统架构总览](./docs/architecture/overview.md)** - 系统整体架构设计
- **[网关架构设计](./docs/architecture/gateways.md)** - Agents网关、模型网关、MCP网关详细设计
- **[模块规划](./docs/architecture/module-structure.md)** - 前端和后端模块详细规划

### 📊 市场分析
- **[项目价值与市场分析](./docs/market-analysis.md)** - 项目价值分析、市场定位、竞品对比

### 📋 开发规范
- **[Java 代码规范](.cursor/rules/java-code-style.mdc)** - Import规范、代码简洁性、抽象复用、实用主义
- **[日志规范](.cursor/rules/logging.mdc)** - 日志等级使用、避免重复日志
- **[文档规范](.cursor/rules/documentation.mdc)** - 代码文档对应要求、文档同步更新
- **[规范管理](.cursor/rules/rule-management.mdc)** - 规范添加流程、规则文件组织

> **文档规范**: 所有代码相关的功能、模块、架构设计都应该在 `docs/` 目录下有对应的设计文档说明。文档应该与代码保持同步更新。

## 🎯 快速开始

> 项目正在开发中，敬请期待...

## 🤝 贡献指南

欢迎贡献代码、提出建议或报告问题！

## 📄 许可证

本项目采用 [MIT License](./LICENSE) 开源许可证。

## 📦 开源软件声明

本项目基于以下优秀的开源软件构建，特此致谢：

### 前端技术栈
- **[Vue 3](https://github.com/vuejs/core)** - MIT License - 渐进式 JavaScript 框架
- **[Element Plus](https://github.com/element-plus/element-plus)** - MIT License - Vue 3 企业级 UI 组件库
- **[Vite](https://github.com/vitejs/vite)** - MIT License - 下一代前端构建工具
- **[Pinia](https://github.com/vuejs/pinia)** - MIT License - Vue 官方状态管理库
- **[Vue Router](https://github.com/vuejs/router)** - MIT License - Vue.js 官方路由管理器
- **[Axios](https://github.com/axios/axios)** - MIT License - 基于 Promise 的 HTTP 客户端
- **[TypeScript](https://github.com/microsoft/TypeScript)** - Apache License 2.0 - JavaScript 的超集

### 后端技术栈
- **[Spring Boot](https://github.com/spring-projects/spring-boot)** - Apache License 2.0 - Java 应用框架
- **[MySQL](https://www.mysql.com/)** - GPL 2.0 / Commercial - 关系型数据库管理系统
- **[Redis](https://github.com/redis/redis)** - BSD 3-Clause License - 内存数据结构存储

### 其他工具和库
- 其他依赖项请参考各子项目的 `package.json` 和 `pom.xml` 文件

> **注意**: 本项目遵循所有依赖项的开源许可证要求。如需了解具体依赖项的许可证信息，请查看各依赖项的官方文档。

## 🔗 相关链接

- [项目规划文档](../Documents/Obsidian/AI基础建设PPDC.md)

