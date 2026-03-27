## Context

AIHub 目前有两项功能需要完善：

1. **MCP 工具管理**: MCP 工具目前通过代码硬编码注册（`register_builtin_tools`），没有独立的管理页面，只能在聊天侧边栏查看和测试。

2. **模型分类**: 模型配置 (`model_config` 表) 没有分类字段，所有类型模型混在一起，无法按类型筛选和管理。

**现状分析**:
- 后端已有 `mcp_tool` 表存储工具元数据
- 后端已有 `/api/mcp/tools` 等 CRUD API
- 前端聊天页面已有 `McpToolPanel` 组件展示工具列表
- 模型配置已有完整 CRUD，但缺少分类

## Goals / Non-Goals

**Goals:**
- MCP 工具提供独立的 `/mcp` 管理页面，支持增删改查
- 模型配置增加 `model_type` 分类字段，前端支持按类型筛选
- 复用现有后端 API，仅扩展前端能力

**Non-Goals:**
- 不修改 MCP 工具的注册机制（保持代码注册方式）
- 不实现 MCP 工具的动态注册（需要额外架构设计）
- 不修改现有模型的 API 响应格式（兼容现有调用方）

## Decisions

### Decision 1: MCP 工具管理页面架构

**选择**: 新建独立路由 `/mcp` 和页面组件

**理由**:
- 符合之前菜单拆分的设计原则（独立功能独立菜单）
- 复用现有的 `McpToolPanel` 组件逻辑
- 复用后端已有的 `/api/mcp/tools` API

**替代方案**:
- 在聊天侧边栏扩展（功能堆砌，不符合单一职责）

### Decision 2: 模型分类字段

**选择**: `model_type` 字段，使用枚举值

```sql
ALTER TABLE model_config ADD COLUMN model_type VARCHAR(50) DEFAULT 'chat';
```

| 值 | 说明 |
|-----|------|
| chat | 对话模型 |
| embedding | 向量模型 |
| image | 文生图模型 |
| audio | 语音模型 |
| rerank | 重排序模型 |

**理由**:
- 简单直接，兼容现有数据（默认 chat）
- 前端通过下拉框筛选
- 不同类型可显示不同配置项（如 image 模型需要 size 参数）

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| 模型类型枚举未来需扩展 | V1 仅支持 chat/embedding，后续按需扩展 |
| MCP 工具仍是代码注册，非动态 | 文档说明如何通过代码添加新工具 |
| 模型分类影响现有查询 | 默认值兼容现有数据 |

## Open Questions

1. MCP 工具是否需要"分组"功能（如按功能类型分组）？
2. 模型测试页面 (`/model-testing`) 是否需要根据模型类型显示不同测试界面？
