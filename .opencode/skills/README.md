# OpenCode Skills - AIHub

这是 AIHub 项目的 OpenCode skills，为 AI 编码助手提供项目特定的开发指导。

## 📁 目录结构

```
.opencode/skills/
├── java-development/         # Java 开发技能
│   ├── SKILL.md              # 主文件（触发条件、快速参考）
│   ├── reference/            # 详细规范（通过 progressive disclosure 加载）
│   │   ├── code-style.md
│   │   ├── logging.md
│   │   └── error-handling.md
│   └── examples/             # 代码示例
│       └── controller-example.md
│
├── frontend-development/      # 前端开发技能
│   ├── SKILL.md
│   ├── reference/
│   │   ├── code-style.md
│   │   └── ui-design.md
│   └── examples/
│       └── vue-component-example.md
│
├── database-operations/     # 数据库操作技能
│   ├── SKILL.md
│   ├── reference/
│   │   └── sql-optimization.md
│   └── examples/
│       └── mapper-example.md
│
└── feature-development/     # 功能开发技能（综合）
    ├── SKILL.md
    ├── reference/
    │   ├── development-flow.md
    │   └── checklist.md
    └── examples/
        └── complete-feature-example.md
```

## 🚀 如何使用

### 自动触发

当 AI 助手检测到相关请求时，会自动加载对应的 skill：

| 用户请求 | 触发 Skill |
|---------|-----------|
| "编写 Java 代码", "创建 Java 类", "实现 Service" | `java-development` |
| "开发前端页面", "创建 Vue 组件", "API 调用" | `frontend-development` |
| "优化 SQL", "数据库查询", "MyBatis Mapper" | `database-operations` |
| "开发新功能", "添加功能模块", "实现业务功能" | `feature-development` |

### 手动触发

在任何对话中，可以明确指定要使用的 skill：

```
Please use java-development skill to create a new UserService.
```

## 📖 设计原则

### 高内聚低耦合
- **java-development/**: 所有 Java 开发相关
- **frontend-development/**: 所有前端开发相关
- **database-operations/**: 所有数据库操作相关
- **feature-development/**: 跨模块的功能开发流程

### Progressive Disclosure
- **SKILL.md**: 包含触发条件、决策树、关键摘要（<500 行）
- **reference/**: 详细规范，只在需要时加载
- **examples/**: 代码示例，按需引用

### 无冗余
- 单一数据源：详细规范只在 `reference/` 中存在
- 多个 skill 可以引用相同的 reference 文件
- 删除了 `.cursor/` 目录，所有规范已迁移

### 中英混合
- **结构性内容**：中文（更直观）
- **代码示例**：英文（自然）
- **技术术语**：保留英文（准确）

## 🎯 技能说明

### java-development
- 适用场景：编写或修改 Java 代码
- 涵盖：代码风格、日志、异常处理、Service/Controller 模式
- 参考：代码规范、日志规范、错误处理

### frontend-development
- 适用场景：创建或修改 Vue 组件、页面、API 调用
- 涵盖：代码风格、UI/UX 设计、组件模式、API 集成
- 参考：代码风格、UI 设计规范

### database-operations
- 适用场景：编写 SQL、MyBatis Mapper、设计数据库表
- 涵盖：SQL 优化、命名约定、MyBatis 模式、性能优化
- 参考：SQL 优化、数据库设计、MyBatis 模式

### feature-development
- 适用场景：从头开发新功能
- 涵盖：完整开发流程（数据库 → 后端 → 前端 → 配置 → 测试 → 文档）
- 参考：开发流程、检查清单

## 🔧 维护指南

### 添加新规范

1. **确定所属技能**：选择最合适的 skill 目录（java-development/frontend-development/database-operations/feature-development）
2. **创建或更新 reference 文件**：在 `reference/` 目录添加或更新规范文档
3. **更新 SKILL.md**：在主文件中添加快速参考或更新决策树
4. **更新 README.md**：如果需要，更新本文件的结构说明

### 添加新技能

1. **创建技能目录**：`.opencode/skills/new-skill/`
2. **创建 SKILL.md**：必需文件，包含 frontmatter 和触发条件
3. **创建 reference/**：可选，存放详细规范
4. **创建 examples/**：可选，存放代码示例

### 技能文件格式

SKILL.md 必须以以下 frontmatter 开头：

```yaml
---
name: skill-name
description: Brief description of what this Skill does and when to use it
---
```

- `name`: 1-64 字符，小写字母数字和连字符
- `description`: 1-1024 字符，用于自动发现

## 📚 相关文档

- **[AGENTS.md](../../AGENTS.md)** - 快速开发指南（build 命令、代码规范摘要）
- **[.cursor/backup/rules](.cursor/backup/rules/)** - 原始 Cursor 规则（已归档）
- **[README.md](../../README.md)** - 项目主文档

## 🔄 迁移历史

### 2026-01-20
- 从 `.cursor/rules/` 迁移到 `.opencode/skills/`
- 删除 `.cursor/` 目录
- 采用 progressive disclosure 架构（SKILL.md + reference/）
- 使用中英混合模式（结构中文，代码英文，术语保留英文）
- 实现高内聚低耦合设计
