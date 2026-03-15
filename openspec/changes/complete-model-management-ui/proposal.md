## Why

AIHub 后端已实现模型管理功能（model-management 变更），包括模型配置管理、多模型Provider适配、统一调用接口和在线测试。但前端尚未提供完整的用户界面，用户无法通过 Web 界面管理模型。需要完善前端页面以完成模型管理功能的端到端体验。

## What Changes

1. **模型管理列表页** - 显示所有已配置的模型，支持分页、搜索、筛选
2. **模型添加/编辑对话框** - 表单式交互，支持配置模型基本信息、API Key、Provider 参数
3. **模型删除功能** - 支持删除模型配置（带确认提示）
4. **模型在线测试页面** - 对话式测试界面，实时验证模型配置是否正确
5. **前端 API 对接** - 调用后端 ModelGateway、ModelProvider 相关接口
6. **功能测试** - 验证模型的增删改查、在线测试、状态管理等核心功能

## Capabilities

### New Capabilities
- `model-management-ui`: 模型管理前端页面，包括列表、表单、删除、测试功能
- `model-testing-ui`: 模型在线测试前端页面，提供对话式测试界面

### Modified Capabilities
- (无) - 现有后端能力不变，仅补充前端 UI

## Impact

- **前端**: 新增模型管理相关 Vue 组件和页面
- **后端**: 无需修改（使用现有 ModelGateway、ModelProvider API）
- **数据库**: 无变更
- **依赖**: Element Plus（UI 组件库）、axios（HTTP 客户端）
