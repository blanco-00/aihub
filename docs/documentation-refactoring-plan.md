# AIHub 文档重构方案

> **创建日期**：2026-03-20
>
> **基于调研**：LangChain、CrewAI、PyTorch等成功开源项目的文档最佳实践
>
> **目标**：让文档非常吸引人，清晰展示已实现功能，避免用户困惑

---

## 📊 调研结果总结

### 成功项目的文档模式

#### 1. **CrewAI** - 最相关参考
- **目录结构**：`/concepts/`, `/learn/`, `/guides/`, `/tools/`, `/enterprise/`
- **特点**：
  - 企业功能与开源功能清晰分离
  - 每个功能都有独立的详细文档
  - 示例代码丰富（cookbooks）
  - 变更日志详细，版本号清晰

#### 2. **LangChain** - LLM框架标杆
- **目录结构**：`/concepts/`, `/how_to/`, `/integrations/`
- **特点**：
  - 概念文档与实践指南分离
  - 废弃功能有明确的迁移指南
  - 集成文档独立管理

#### 3. **PyTorch** - 稳定性管理
- **特点**：
  - **Stable (API-Stable)** - 长期维护，向后兼容
  - **Unstable (API-Unstable)** - 可能改变
  - 版本承诺清晰

### 核心发现

**关键洞察**：成功的项目**绝不**在主文档中混合已实现和规划中的功能！

---

## 🎯 当前问题

### 问题清单

1. ❌ **README混合展示** - 同时展示v3.0功能（未实现）和已完成功能
2. ❌ **教程不可用** - tutorial.md展示RAG、MCP、Agent模板（均未实现）
3. ❌ **API文档混乱** - api-reference.md包含未实现的端点
4. ❌ **缺少版本管理** - 没有changelog、version文档
5. ❌ **缺少状态标记** - 用户无法知道哪些功能可用

### 影响

- 🔴 用户下载后尝试未实现功能 → 失望
- 🔴 项目可信度降低
- 🔴 开源社区贡献困难

---

## ✨ 重构方案

### 方案概述：双轨制文档体系

**核心理念**：
- 🟢 **用户文档** = 只展示已实现功能
- 🔵 **规划文档** = 所有未来规划，独立管理

---

## 📁 新文档结构

```
docs/
├── README.md                          # 主入口（极简、吸引人）
│
├── getting-started/                   # 快速开始（5分钟路径）
│   ├── installation.md               # 安装指南
│   ├── quickstart.md                 # 3分钟上手
│   └── first-agent.md                # 创建第一个Agent
│
├── user-guide/                        # 用户指南
│   ├── model-management/             # 模型管理
│   │   ├── overview.md              # 概述
│   │   ├── add-model.md            # 添加模型
│   │   └── test-model.md           # 测试模型
│   ├── chat-interface/              # 对话界面
│   │   ├── basic-chat.md           # 基础对话
│   │   ├── streaming.md            # 流式响应
│   │   └── session-history.md      # 会话历史
│   ├── prompt-templates/            # Prompt模板
│   │   ├── using-templates.md      # 使用模板
│   │   ├── creating-templates.md   # 创建模板
│   │   └── built-in-templates.md   # 内置模板
│   └── deployment/                  # 部署
│       ├── docker.md                # Docker部署
│       └── configuration.md         # 配置说明
│
├── api-reference/                     # API文档
│   ├── overview.md                   # API概述
│   ├── authentication.md             # 认证
│   ├── models.md                     # 模型API
│   ├── chat.md                       # 对话API
│   ├── prompts.md                    # Prompt API
│   └── errors.md                     # 错误码
│
├── examples/                          # 示例（真实场景）
│   ├── simple-chat.md               # 简单对话
│   ├── multi-model.md               # 多模型切换
│   ├── custom-prompt.md             # 自定义Prompt
│   └── production-deployment.md     # 生产部署
│
├── troubleshooting/                   # 故障排查
│   ├── common-issues.md             # 常见问题
│   ├── error-messages.md            # 错误信息
│   └── faq.md                       # FAQ
│
├── planning/                          # 规划文档（独立）
│   ├── roadmap.md                    # 完整路线图
│   ├── v2.0-features.md             # v2.0功能
│   ├── v3.0-features.md             # v3.0功能
│   ├── architecture-evolution.md    # 架构演进
│   └── contribution-guide.md        # 贡献指南
│
├── changelog/                         # 变更日志
│   └── README.md                     # 变更日志索引
│
├── version/                           # 版本文档
│   ├── v1.0.md                       # v1.0发布说明
│   └── upgrade-guide.md              # 升级指南
│
└── developer/                         # 开发者文档
    ├── backend/                       # 后端开发
    ├── frontend/                      # 前端开发
    └── contributing.md                # 贡献指南
```

---

## 🎨 README.md 设计（极简吸引人）

```markdown
# AIHub

<p align="center">
  <img src="docs/.assets/logo.svg" alt="AIHub Logo" width="200"/>
</p>

<p align="center">
  <strong>像搭乐高一样构建 AI Agent</strong>
</p>

<p align="center">
  <a href="#-快速开始"><strong>3分钟上手 »</strong></a>
</p>

---

## ✨ 为什么选择 AIHub？

> **一键部署** · **多模型支持** · **可视化配置** · **企业级安全**

<table>
<tr>
<td width="50%">

### 🚀 极速开始

```bash
# 3步启动
git clone https://github.com/aihub/aihub.git
cd aihub/docker
docker compose up -d
```

**访问**: http://localhost:3000

</td>
<td width="50%">

### 💡 核心能力

- ✅ **多模型管理** - OpenAI/Claude/智谱/通义
- ✅ **流式对话** - 实时响应，打字机效果
- ✅ **Prompt模板** - 10+内置模板
- ✅ **会话管理** - 历史记录，上下文保持

</td>
</tr>
</table>

---

## 📖 文档导航

| 我想... | 文档 |
|--------|------|
| 快速上手 | [3分钟教程](docs/getting-started/quickstart.md) |
| 添加模型 | [模型管理指南](docs/user-guide/model-management/) |
| 创建Prompt | [Prompt模板指南](docs/user-guide/prompt-templates/) |
| 部署到生产 | [部署指南](docs/user-guide/deployment/) |
| 查看API | [API参考文档](docs/api-reference/) |
| 解决问题 | [故障排查](docs/troubleshooting/) |

---

## 🎯 使用场景

### 场景1：简单对话（1分钟）
```bash
1. 访问 http://localhost:3000
2. 点击「对话」
3. 选择模型 → 开始对话
```

### 场景2：使用Prompt模板（2分钟）
```bash
1. 进入「Prompt模板」
2. 选择「代码助手」模板
3. 开始对话 → 获得专业回答
```

### 场景3：自定义Prompt（5分钟）
```bash
1. 创建新的Prompt模板
2. 添加变量 {{language}}, {{task}}
3. 保存并使用
```

[更多示例 →](docs/examples/)

---

## 🔥 特色功能

### 1. 多模型一键切换
支持 OpenAI、Claude、智谱GLM、通义千问等主流模型，随时切换对比效果。

### 2. 流式实时响应
SSE流式传输，打字机效果，首字延迟<500ms。

### 3. Prompt模板库
10+内置模板（客服、代码助手、数据分析等），支持变量和条件逻辑。

### 4. 会话历史管理
自动保存对话历史，支持搜索、导出、分享。

---

## 🏗️ 技术栈

<table>
<tr>
<td><strong>前端</strong></td>
<td>Vue 3 + TypeScript + Element Plus</td>
</tr>
<tr>
<td><strong>后端</strong></td>
<td>Java 17 + Spring Boot 3.2</td>
</tr>
<tr>
<td><strong>AI服务</strong></td>
<td>Python + FastAPI + LangChain</td>
</tr>
<tr>
<td><strong>数据库</strong></td>
<td>MySQL 8.0 + Redis</td>
</tr>
</table>

---

## 🚧 规划中功能

> **当前版本**: v1.0 · **查看**: [完整路线图](docs/planning/roadmap.md)

- 📚 **RAG知识库** (v2.0) - 文档上传，智能检索
- 🔌 **MCP工具** (v2.0) - 50+预置工具
- 💰 **成本统计** (v2.0) - Token实时统计
- 🤖 **Agent模板** (v3.0) - 可视化配置
- 🎯 **Skills技能库** (v3.0) - 100+预置技能

---

## 🤝 社区

- 💬 [GitHub Discussions](https://github.com/aihub/aihub/discussions) - 提问交流
- 🐛 [GitHub Issues](https://github.com/aihub/aihub/issues) - 报告问题
- 📖 [贡献指南](docs/developer/contributing.md) - 参与开发

---

## 📄 许可证

[MIT License](LICENSE) © 2026 AIHub Team

---

<p align="center">
  <strong>开始构建你的AI Agent →</strong>
  <br><br>
  <a href="docs/getting-started/quickstart.md">
    <img src="https://img.shields.io/badge/开始使用-3分钟上手-blue?style=for-the-badge" alt="Quick Start">
  </a>
</p>
```

---

## 📝 关键文档内容

### 1. 快速开始文档

**docs/getting-started/quickstart.md**

```markdown
# 3分钟快速开始

> **前置要求**: Docker 20.10+, Docker Compose 2.0+

## 第一步：启动服务（2分钟）

```bash
# 克隆项目
git clone https://github.com/aihub/aihub.git
cd aihub

# 一键启动
cd docker && docker compose up -d

# 查看状态
docker compose ps
```

## 第二步：访问系统（30秒）

打开浏览器访问：
- **前端**: http://localhost:3000
- **后端API**: http://localhost:8080

**默认账号**:
- 用户名: `admin`
- 密码: `admin123`

## 第三步：开始对话（30秒）

1. 登录系统
2. 点击左侧菜单「AI对话」
3. 选择模型（如 GPT-3.5）
4. 输入消息，开始对话！

## 🎉 恭喜！

你已经成功创建了第一个AI对话。接下来可以：

- 📖 [添加更多模型](../user-guide/model-management/add-model.md)
- 🎨 [使用Prompt模板](../user-guide/prompt-templates/using-templates.md)
- 📚 [查看更多示例](../examples/)

---

## 遇到问题？

- [常见问题](../troubleshooting/common-issues.md)
- [错误信息](../troubleshooting/error-messages.md)
- [GitHub Issues](https://github.com/aihub/aihub/issues)
```

---

### 2. 功能状态标记规范

**在所有功能文档中使用统一的状态标记**：

```markdown
> **功能状态**: ✅ 已实现 (v1.0+)
> **最后更新**: 2026-03-20
> **API稳定性**: Stable

## 功能说明
[功能描述]

## 使用方法
[使用步骤]

## 示例
[代码示例]
```

**状态标记说明**：
- ✅ **已实现** - 当前稳定版本可用
- 🚧 **开发中** - Beta版本，可能改变
- ⏸️ **已暂停** - 暂时不可用
- ❌ **已废弃** - 将在下一版本移除

---

### 3. Changelog格式

**docs/changelog/README.md**

```markdown
# AIHub 变更日志

所有重要变更都会记录在此文档中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/),
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### 新增
- 待发布的新功能

## [1.0.0] - 2026-03-20

### 新增
- ✨ 模型管理 - 支持多厂商LLM配置
- ✨ 基础对话 - SSE流式响应
- ✨ Prompt模板 - 10+内置模板
- ✨ 会话管理 - 历史记录与上下文

### 变更
- 📝 重构文档结构

### 修复
- 🐛 修复会话历史显示问题

## [0.1.0] - 2026-01-12

### 新增
- 🎉 初始发布
- ✨ 用户认证与权限管理
- ✨ 系统监控与日志

[Unreleased]: https://github.com/aihub/aihub/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/aihub/aihub/releases/tag/v1.0.0
[0.1.0]: https://github.com/aihub/aihub/releases/tag/v0.1.0
```

---

## 🚀 实施计划

### 阶段1：创建新结构（优先）

1. ✅ 创建目录结构
2. ✅ 创建README.md（新版本）
3. ✅ 创建getting-started文档
4. ✅ 创建user-guide核心文档
5. ✅ 创建changelog

### 阶段2：迁移现有文档

1. 迁移backend/ → developer/backend/
2. 迁移frontend/ → developer/frontend/
3. 迁移deployment/ → user-guide/deployment/
4. 创建planning/目录，移动规划文档

### 阶段3：清理与优化

1. 删除重复文档
2. 更新所有内部链接
3. 添加缺失的示例代码
4. 优化文档排版

---

## 📊 成功指标

- ✅ 用户能在3分钟内启动并使用
- ✅ 所有已实现功能都有详细文档
- ✅ 规划功能独立管理，不造成困惑
- ✅ 每个功能都有状态标记
- ✅ 变更日志完整

---

## 🎯 文档原则

1. **极简主义** - 少即是多
2. **用户至上** - 只展示可用功能
3. **示例驱动** - 代码胜于文字
4. **状态清晰** - 明确标记实现状态
5. **版本管理** - 每个版本都有文档

---

> 本方案基于成功开源项目的最佳实践制定
> 参考：CrewAI、LangChain、PyTorch等
