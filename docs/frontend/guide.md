# 前端开发文档

> 本文档是前端开发的完整指南，包含技术栈选型、项目结构、开发规范等内容。

## 📚 文档导航

- [项目总览](../README.md) - 项目主文档入口
- [UI/UX设计文档](./design.md) - 视觉风格、布局设计、交互设计
- [页面规划文档](./pages.md) - 所有页面的详细功能规划
- [组件设计文档](./components.md) - 组件分类、组件设计、开发规范
- [API对接文档](./api.md) - 前端API接口定义、对接方式、错误处理

## 概述

前端采用现代化的Web技术栈，提供直观、高效的管理界面，让用户能够轻松管理AI基础设施的各个组件。

## 技术栈

### 已选型方案
- **框架**: **Vue 3** (Composition API)
- **UI组件库**: **Element Plus** (Vue 3企业级组件库)
- **状态管理**: **Pinia** (Vue官方推荐的状态管理)
- **路由**: **Vue Router 4**
- **HTTP客户端**: **Axios**
- **构建工具**: **Vite**
- **TypeScript**: 强烈推荐使用

### 为什么选择这些技术？
- **Vue 3**: 
  - Composition API提供更好的逻辑复用和类型推导
  - 性能优秀，包体积小
  - 学习曲线平缓，易于协作
  - 中文文档完善，社区活跃
  
- **Element Plus**: 
  - Vue 3官方推荐的企业级UI组件库
  - 组件丰富，开箱即用
  - 设计风格统一，适合后台管理系统
  
- **Pinia**: 
  - Vue官方推荐，替代Vuex
  - TypeScript支持优秀
  - 代码简洁，易于维护
  
- **Vite**: 
  - 开发体验极佳，热更新速度快
  - 构建速度快
  - 原生ESM支持

## 项目结构

```
frontend/
├── src/
│   ├── components/        # 公共组件
│   │   ├── layout/       # 布局组件
│   │   ├── common/       # 通用组件
│   │   └── business/     # 业务组件
│   ├── pages/            # 页面组件
│   │   ├── dashboard/    # 仪表盘
│   │   ├── models/       # 模型管理
│   │   ├── agents/       # Agent管理
│   │   ├── prompts/      # Prompt管理
│   │   ├── mcp/          # MCP管理
│   │   ├── permissions/  # 权限管理
│   │   ├── monitoring/   # 监控统计
│   │   └── settings/     # 系统设置
│   ├── services/         # API服务
│   ├── stores/           # 状态管理
│   ├── utils/            # 工具函数
│   ├── types/            # TypeScript类型定义
│   └── assets/           # 静态资源
├── public/               # 公共资源
└── package.json
```

## 核心页面规划

### 1. 仪表盘 (Dashboard)
- 系统概览
- Token消耗统计
- 模型使用情况
- 告警信息
- 快速操作入口

### 2. 模型管理 (Models)
- 模型列表（支持多厂商：OpenAI、Claude、DeepSeek等）
- 模型配置（API Key、Endpoint、参数）
- 模型测试
- 模型路由策略配置

### 3. Agent管理 (Agents)
- Agent列表
- Agent创建/编辑
- Agent工作流配置
- Agent测试与调试

### 4. Prompt管理 (Prompts)
- Prompt库
- Prompt版本管理
- Prompt效果评分
- Prompt复用统计

### 5. MCP管理 (MCP Servers)
- MCP Server列表
- MCP Server配置
- MCP Server市场

### 6. 权限管理 (Permissions)
- 用户管理
- 角色管理
- 资源权限配置
- API Key管理

### 7. 监控统计 (Monitoring)
- Token消耗追踪（按部门/项目/人）
- 成本分析
- 使用趋势
- 告警配置

### 8. 系统设置 (Settings)
- 系统配置
- 合规检测配置
- 流量防护配置
- 审计日志

## 设计原则

1. **用户体验优先**: 界面简洁、操作直观
2. **响应式设计**: 支持PC、平板、移动端
3. **性能优化**: 懒加载、虚拟滚动、代码分割
4. **可访问性**: 遵循WCAG标准
5. **国际化**: 预留i18n支持

## 开发规范

- 使用TypeScript严格模式
- 组件采用Composition API + `<script setup>`
- 遵循ESLint和Prettier规范
- 组件命名采用PascalCase
- 文件命名采用kebab-case
- 提交信息遵循Conventional Commits
- 使用Vue 3推荐的组合式函数(Composables)进行逻辑复用

## 下一步

1. 完成UI/UX设计稿
2. 搭建项目脚手架
3. 实现核心页面
4. 对接后端API
5. 完善交互细节

