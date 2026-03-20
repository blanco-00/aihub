## Why

当前系统已实现模型配置功能（`/system/model`），用户可以添加和管理各种AI模型（OpenAI、智谱、阿里等）。但用户只能通过"模型测试"页面（`/system/model/test`）进行简单的单轮对话测试，没有一个面向普通用户的正式聊天界面。

需要添加一个功能完善的"AI聊天"页面，作为系统的核心功能入口，让用户可以选择已配置的模型进行多轮对话。

## What Changes

1. **新增AI聊天页面** - 创建 `/ai/chat` 路由，对应前端页面 `frontend/src/views/ai/chat/index.vue`
2. **侧边栏菜单入口** - 在系统菜单中添加"AI聊天"入口，图标使用 `ri-chat-voice-line`
3. **模型选择器** - 页面顶部模型下拉选择器，可切换当前聊天的模型
4. **多轮对话支持** - 支持连续多轮对话，保持上下文
5. **对话历史展示** - 美观的聊天消息展示（类似ChatGPT风格）
6. **清空对话功能** - 支持一键清空当前对话记录
7. **发送功能** - 支持 Enter 键发送和按钮发送

## Capabilities

### New Capabilities

- `model-chat`: 新增模型聊天功能页面，支持选择已配置的模型进行对话交流

### Modified Capabilities

- 无

## Impact

- **前端**: 新增 `frontend/src/views/ai/chat/index.vue` 页面
- **前端**: 需要在菜单配置中添加"AI聊天"菜单项（通过数据库 SQL 添加）
- **API**: 复用现有 `/api/ai/chat` 接口，无需新增后端代码
