## Why

当前 AIHub 的 MCP 工具只能通过代码注册，没有独立的管理界面；同时模型配置没有分类，所有类型的模型混在一起，导致管理不便。需要为 MCP 工具提供独立的 CRUD 管理页面，并为模型增加类型分类功能。

## What Changes

1. **新增 MCP 工具管理页面**
   - 独立菜单 `/mcp` 访问 MCP 工具管理
   - 支持工具的增删改查、启用/禁用、测试执行
   - 展示工具调用次数和最后执行时间

2. **新增模型分类功能**
   - 模型增加 `model_type` 字段（chat/embedding/image/audio/rerank）
   - 前端按类型筛选模型
   - 不同类型模型显示不同配置项

## Capabilities

### New Capabilities
- `mcp-tool-management`: MCP 工具管理，支持增删改查、启用禁用、测试执行
- `model-type-classification`: 模型分类管理，支持按类型筛选和配置

### Modified Capabilities
- `model-config` (existing): 在现有模型配置能力基础上增加类型分类属性

## Impact

- **前端**: 新增 `views/mcp/index.vue` 页面，新增 `router/modules/mcp.ts` 路由
- **后端 Python**: MCP 工具管理 API (`/api/mcp/tools`)
- **数据库**: `model_config` 表增加 `model_type` 字段
- **国际化**: 新增 `pureMcp`, `pureSkills` 等 i18n 键
