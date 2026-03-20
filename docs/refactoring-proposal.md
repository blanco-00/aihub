# AIHub 文档重构方案

> **创建日期**：2026-03-20
>
> **基于调研**：LangChain、CrewAI、PyTorch等成功开源项目的文档最佳实践
>
> **目标**：重构文档结构，确保开源用户只看到已实现功能，规划文档独立管理

---

## 🎯 现状问题分析

### 问题1：文档混乱，用户困惑
**当前问题**：
- README.md同时标注了v3.0可用功能（实际上还未实现）
- tutorial.md展示了大量未实现功能（RAG、MCP工具、Agent模板）
- api-reference.md包含大量未实现的API接口
- 用户无法区分哪些功能可用、哪些是计划中

**影响**：
- 开源用户下载项目后尝试未实现功能 → 用户体验差
- 项目可信度降低（"说一套，做一套"）

---

## 🎯 解决方案：受众分离 + 版本管理

### 方案1：文档目录重构（推荐）

**新目录结构**：

```
docs/
├── README.md                        # 主入口（仅当前可用功能）
│
├── current/                        # 当前可用功能详情
│   ├── v1.0-capabilities.md        # v1.0功能说明
│   ├── quickstart.md               # 快速开始（基于已实现功能）
│   ├── tutorial.md                # 使用教程（仅v1.0场景）
│   └── api-reference.md          # API文档（仅v1.0端点）
│
├── planning/                       # 规划文档（所有未实现功能）
│   ├── roadmap.md                  # 完整路线图
│   ├── future-features.md          # 未来功能规划
│   └── architecture-evolution.md  # 架构演进
│
└── implementation-status.md       # 实施进度跟踪
│
├── guides/                        # 用户指南
│   ├── getting-started/              # 入门教程
│   ├── building-agents/            # Agent创建教程
│   ├── using-features/            # 功能使用指南
│   └── deployment/                 # 部署指南
│   └── api-reference/             # 完整API参考
│
├── changelog/                      # 变更日志
│   └── version/                    # 版本发布历史
└── deprecation/                  # 废弃通知
│
└── security/                    # 安全指南
│
├── examples/                     # 示例代码
│
└── troubleshooting/             # 问题排查
```

**关键原则**：
1. **`current/` 目录** → 仅包含当前稳定版本的功能
2. **`planning/` 目录** → 完整的未来规划
3. **受众清晰分离** → 用户指南在guides/，开发者文档在docs/
4. **版本独立管理** → 每个稳定版本有独立页面

---

### 方案2：README.md重构（推荐）

**新的README结构**：

```markdown
# AIHub

<p align="center">
  <img src="docs/.assets/logo.svg" alt="AIHub Logo" width="200"/>
</p>

<p align="center">
  <strong>企业级AI Agent编排平台</strong>
</p>

---

## ✨ 当前可用功能（v1.0）

> **注意**：以下功能均已完成并经过测试，可直接使用

### 📱 核心对话能力
- ✅ **模型管理**：支持多厂商LLM，API密钥加密存储
- ✅ **基础对话**：SSE流式响应，多模型切换，会话历史
- ✅ **Prompt模板**：变量支持，10+内置模板，模板测试

### 🚀 快速开始
```bash
# 克隆项目
git clone https://github.com/aihub/aihub.git
cd aihub

# 启动所有服务
cd docker && docker compose up -d

# 访问系统
# 前端: http://localhost:3000
# 后端: http://localhost:8080
```

[完整功能文档](docs/current/v1.0-capabilities.md) · [在线演示](https://demo.aihub.com)

### 📖 详细文档
- [用户指南](docs/guides/getting-started.md)
- [Agent创建教程](docs/guides/building-agents.md)
- [部署指南](docs/guides/deployment.md)
- [API参考](docs/api-reference.md)

---

## 🚧 规划中功能（开发中）

> **当前状态**：[查看开发进度](docs/planning/implementation-status.md)

> **预计发布时间**：v2.0（2026-07-XX）、v3.0（2026-09-XX）

### 📚 完整路线图

[开发路线图](docs/planning/roadmap.md) · [架构演进](docs/planning/architecture-evolution.md)

---

## 🤝 社区

- 💬 [讨论](https://github.com/aihub/aihub/discussions)
- 🐛 [问题反馈](https://github.com/aihub/aihub/issues)

---

## 📄 许可证

MIT License - 见 [LICENSE](LICENSE)

---

> **文档更新说明**
- 功能更新：在发布新版本后，将旧版本文档移至`/version/`目录
- Changelog：[变更日志](docs/changelog.md)
- 版本状态：[状态跟踪](docs/planning/implementation-status.md)

---

**开源用户请注意**
- ✅ 当前可用功能已完全实现并测试
- ⚠️ 规划中功能正在开发，可通过实施状态跟踪进度
- 📝 所有功能文档都会明确标注实现状态

---

## 🎯 核心价值主张

> 像搭乐高一样构建AI智能体
> 可视化配置，业务人员无需编程
> 多模型支持（OpenAI、Claude、国产LLM）
> RAG知识库，企业知识问答
> MCP工具生态，50+预置工具
> 成本精算体系，企业级合规
> 私有化部署，数据安全

---

<p align="center">
  如果这个项目对你有帮助，请给我们一个 ⭐
</p>
