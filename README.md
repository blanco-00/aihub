# AI基础设施平台

> 构建企业级AI基础设施，统一管理模型、密钥、权限、Prompts、MCP与Agent工作流，降低AI应用开发复杂度，支持快速构建AI应用。

## 🎯 项目愿景

**一句话定位**: _"构建企业级AI基础设施，统一管理模型、密钥、权限、Prompts、MCP与Agent工作流，降低AI应用开发复杂度，支持快速构建AI应用"_

## ✨ 核心价值

- 学习AI技术，为AI研发岗位积累
- 解决企业或团队等多人使用AI的各类痛点

## 🚀 核心特性

- **AI Agent核心能力** - 问题分类、智能引导、并行执行、流式响应
- **RAG检索增强** - 智能检索、知识增强、多路召回、跨RAG去重
- **工具调用能力（MCP）** - MCP协议支持、工具注册管理、组合工具调用
- **会话管理** - 智能记忆、上下文管理
- **管理后台** - 仪表盘、会话管理、用户管理、统计分析
- **Agent模板化系统** - 可复用Agent模板、模板市场、配置化创建
- **统一网关体系** - Agents网关、模型网关、MCP网关、智能路由
- **精细化Token统计** - 多维度统计、成本分析、实时监控
- **权限管理** - 三级权限控制、API Key管理、服务订阅
- **内容合规检测** - 敏感词库、不合规引导检测、违规内容拦截
- **模型快速接入、切换** - 配置化接入，模型网关、动态路由策略
- **突发流量防护** - 按部门/项目划分资源池，超额请求自动进入队列或降级服务
- **知识资产沉淀** - Prompt库管理、Agent工作流市场、MCP server市场
- **故障自愈能力** - 自动切换备用模型，通知值班人员，生成故障分析报告

## 🎯 快速开始

### 方式一：Docker Compose 一键启动（推荐，最简单）

**前置要求**: Docker 20.10+、Docker Compose 2.0+

```bash
# 1. 进入 docker 目录
cd docker

# 2. 配置环境变量（可选，使用默认配置可直接跳过）
cp env.example .env
# 编辑 .env 文件修改密码和密钥（生产环境必须修改）

# 3. 一键启动所有服务
docker compose up -d

# 4. 查看服务状态
docker compose ps

# 5. 访问系统
# 前端: http://localhost:3000
# 后端: http://localhost:8080
```

**详细文档**: [Docker Compose 部署指南](./docs/deployment/docker-compose.md)

### 方式二：混合开发方式（推荐，适合日常开发）

**前置要求**: Docker 20.10+、Docker Compose 2.0+、JDK 17+、Maven 3.6+、Node.js 20.19.0+（或 22.13.0+）、pnpm >= 9

**优势**: 基础设施容器化（无需本地安装 MySQL/Redis），应用本地运行（支持热重载，开发效率高）

```bash
# 1. 启动基础设施（MySQL + Redis）
cd docker
docker compose -f docker-compose.dev.yml up -d

# 2. 本地运行后端（支持热重载，自动连接 Docker 开发环境的数据库）
cd backend/aihub-api
mvn spring-boot:run

# 4. 本地运行前端（支持热重载）
cd frontend
pnpm install  # 首次需要安装依赖
pnpm dev
```

**详细文档**: [Docker Compose 部署指南 - 本地开发](./docs/deployment/docker-compose.md#场景二本地开发推荐)

### 方式三：完全本地开发

**前置要求**:
- **后端环境**: JDK 17+、Maven 3.6+、MySQL 8.0+（需本地安装）
- **前端环境**: Node.js 20.19.0+（或 22.13.0+）、pnpm >= 9

**快速开始步骤**:

1. **创建数据库**：`mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS aihub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"`
2. **启动后端服务**：`cd backend/aihub-api && mvn spring-boot:run`
3. **安装前端依赖**：`cd frontend && pnpm install`
4. **启动前端服务**：`cd frontend && pnpm dev`
5. **访问系统**：打开浏览器访问 `http://localhost:3000`，按照页面引导完成系统初始化

**详细文档**:
- **[快速开始指南](./docs/backend/quick-start.md)** - 完整的初始化流程和详细说明
- **[数据库配置说明](./docs/backend/config.md)** - 数据库连接配置说明（开发环境已自动配置）
- **[系统初始化文档](./docs/backend/initialization.md)** - 数据库初始化和超级管理员创建

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

### 📋 功能规划与实施清单
- **[功能清单](./docs/features.md)** - 功能规划、开发状态和实施进度跟踪

### 🎨 开发指南

#### 前端开发
- **[前端开发指南](./docs/frontend/guide.md)** - 前端技术栈、项目结构、开发规范
- **[UI/UX设计文档](./docs/frontend/design.md)** - 视觉风格、布局设计、交互设计
- **[页面规划文档](./docs/frontend/pages.md)** - 所有页面的详细功能规划
- **[组件设计文档](./docs/frontend/components.md)** - 组件分类、组件设计、开发规范
- **[API对接文档](./docs/frontend/api.md)** - 前端API接口定义、对接方式、错误处理

#### 后端开发
- **[后端开发指南](./docs/backend/guide.md)** - 后端技术栈、项目结构、开发规范
- **[快速开始指南](./docs/backend/quick-start.md)** - 快速初始化项目、启动应用
- **[数据库配置说明](./docs/backend/config.md)** - 数据库连接配置和安全指南
- **[数据库设计文档](./docs/backend/database.md)** - 数据库选型、表结构设计、设计规范
- **[系统初始化文档](./docs/backend/initialization.md)** - 数据库初始化、超级管理员创建
- **[SQL 脚本管理](./docs/sql/guide.md)** - SQL 脚本存放方式和管理规范
- API设计文档（待完善）

### 🏗️ 架构设计
- **[系统架构总览](./docs/architecture/overview.md)** - 系统整体架构设计
- **[网关架构设计](./docs/architecture/gateways.md)** - Agents网关、模型网关、MCP网关详细设计
- **[模块规划](./docs/architecture/module-structure.md)** - 前端和后端模块详细规划

### 🎯 方案设计
- **[Agent模板化方案](./docs/agent-template-solution.md)** - Agent模板化方案设计、架构和实现计划

### 🚀 部署指南
- **[Docker Compose 部署指南](./docs/deployment/docker-compose.md)** - 使用 Docker Compose 一键部署

### 📋 开发规范
- **[OpenCode Skills](.opencode/skills/README.md)** - AI智能代理开发技能集（Java/前端/数据库/功能开发）
- **[AGENTS.md](./AGENTS.md)** - 快速开发指南（构建命令、代码规范摘要）

> **文档规范**: 所有代码相关的功能、模块、架构设计都应该在 `docs/` 目录下有对应的设计文档说明。文档应该与代码保持同步更新。

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
