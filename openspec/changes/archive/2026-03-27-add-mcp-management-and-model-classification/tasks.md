## 1. MCP 工具管理页面

- [x] 1.1 创建路由 `router/modules/mcp.ts`
- [x] 1.2 添加 i18n 翻译 `pureMcp: MCP工具管理`
- [x] 1.3 添加图标 `ri/tools-line` 到 `offlineIcon.ts`
- [x] 1.4 创建视图 `views/mcp/index.vue`
- [x] 1.5 复用 `components/McpToolPanel` 组件逻辑
- [x] 1.6 实现工具列表展示（名称、描述、类型、调用次数、最后执行时间）
- [x] 1.7 实现启用/禁用功能
- [x] 1.8 实现测试执行功能

## 2. 模型分类功能

- [x] 2.1 数据库迁移：给 `model_config` 表增加 `model_type` 字段
- [x] 2.2 后端：更新 `ModelConfig` 模型增加 `model_type` 属性
- [x] 2.3 后端：API 支持按类型筛选模型
- [x] 2.4 前端：更新 `views/system/model/index.vue` 表单增加类型下拉
- [x] 2.5 前端：更新模型列表增加类型筛选
- [x] 2.6 前端：模型列表显示类型标签

## 3. 数据库迁移脚本

- [x] 3.1 创建 `V1.0.25__add_model_type_field.sql` 迁移文件
