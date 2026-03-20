# AIHub

<p align="center">
  <img src="docs/.assets/logo.svg" alt="AIHub Logo" width="200"/>
</p>

<p align="center">
  <strong>Build AI Agents Like LEGO</strong>
</p>

<p align="center">
  <a href="#-quick-start"><strong>Get Started in 3 Minutes »</strong></a>
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

<p align="center">
  <a href="README.md">English</a> · <a href="README.zh-CN.md">中文</a>
</p>

---

## ✨ Why AIHub?

> **One-Click Deploy** · **Multi-Model Support** · **Visual Config** · **Enterprise-Grade Security**

<table>
<tr>
<td width="50%">

### 🚀 Quick Start

```bash
# 3 steps to launch
git clone https://github.com/aihub/aihub.git
cd aihub/docker
docker compose up -d
```

**Access**: http://localhost:3000

</td>
<td width="50%">

### 💡 Core Features

- ✅ **Multi-Model Management** - OpenAI/Claude/Zhipu/Tongyi
- ✅ **Streaming Chat** - Real-time response, typewriter effect
- ✅ **Prompt Templates** - 10+ built-in templates
- ✅ **Session Management** - History & context persistence

</td>
</tr>
</table>

---

## 📖 Documentation

| I want to... | Documentation |
|-------------|---------------|
| Get started quickly | [3-Minute Tutorial](docs/en/getting-started/quickstart.md) |
| Add models | [Model Management Guide](docs/en/user-guide/model-management/) |
| Create prompts | [Prompt Template Guide](docs/en/user-guide/prompt-templates/) |
| Deploy to production | [Deployment Guide](docs/en/user-guide/deployment/) |
| View APIs | [API Reference](docs/en/api-reference/) |
| Solve problems | [Troubleshooting](docs/en/troubleshooting/) |

---

## 🎯 Use Cases

### Case 1: Simple Chat (1 min)
```bash
1. Visit http://localhost:3000
2. Click "Chat"
3. Select model → Start conversation
```

### Case 2: Use Prompt Template (2 min)
```bash
1. Go to "Prompt Templates"
2. Select "Code Assistant" template
3. Start chat → Get professional answers
```

### Case 3: Custom Prompt (5 min)
```bash
1. Create new Prompt template
2. Add variables {{language}}, {{task}}
3. Save and use
```

[More Examples →](docs/en/examples/)

---

## 🔥 Key Features

### 1. One-Click Model Switching
Support for OpenAI, Claude, Zhipu GLM, Tongyi Qianwen and other mainstream models. Switch anytime to compare results.

### 2. Real-Time Streaming Response
SSE streaming, typewriter effect, first token latency < 500ms.

### 3. Prompt Template Library
10+ built-in templates (customer service, code assistant, data analysis, etc.), with variable and conditional logic support.

### 4. Session History Management
Auto-save conversation history, support search, export, and sharing.

---

## 🏗️ Tech Stack

<table>
<tr>
<td><strong>Frontend</strong></td>
<td>Vue 3 + TypeScript + Element Plus</td>
</tr>
<tr>
<td><strong>Backend</strong></td>
<td>Java 17 + Spring Boot 3.2</td>
</tr>
<tr>
<td><strong>AI Service</strong></td>
<td>Python + FastAPI + LangChain</td>
</tr>
<tr>
<td><strong>Database</strong></td>
<td>MySQL 8.0 + Redis</td>
</tr>
</table>

---

## 🚧 Coming Soon

> **Current Version**: v1.0 · **View**: [Full Roadmap](docs/en/planning/roadmap.md)

- 📚 **RAG Knowledge Base** (v2.0) - Document upload, intelligent retrieval
- 🔌 **MCP Tools** (v2.0) - 50+ pre-built tools
- 💰 **Cost Analytics** (v2.0) - Real-time token statistics
- 🤖 **Agent Templates** (v3.0) - Visual configuration
- 🎯 **Skills Library** (v3.0) - 100+ pre-built skills

---

## 🤝 Community

- 💬 [GitHub Discussions](https://github.com/aihub/aihub/discussions) - Ask & discuss
- 🐛 [GitHub Issues](https://github.com/aihub/aihub/issues) - Report bugs
- 📖 [Contributing Guide](docs/en/developer/contributing.md) - Join development

---

## 📄 License

[MIT License](LICENSE) © 2026 AIHub Team

---

<p align="center">
  <strong>Start Building Your AI Agent →</strong>
  <br><br>
  <a href="docs/en/getting-started/quickstart.md">
    <img src="https://img.shields.io/badge/Get_Started-3_Minutes-blue?style=for-the-badge" alt="Quick Start">
  </a>
</p>
