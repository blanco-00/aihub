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

<p align="center">
  <a href="https://github.com/aihub/aihub/stargazers">
    <img src="https://img.shields.io/github/stars/aihub/aihub?style=flat" alt="Stars">
  </a>
  <a href="https://github.com/aihub/aihub/releases">
    <img src="https://img.shields.io/github/v/release/aihub/aihub?include_prereleases&style=flat" alt="Version">
  </a>
  <a href="https://opensource.org/licenses/MIT">
    <img src="https://img.shields.io/github/license/aihub/aihub?style=flat" alt="License">
  </a>
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
