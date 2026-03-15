## Why

AIHub定位为"像搭乐高一样构建AI Agent"的平台，模型是核心算力组件。当前无模型管理能力，无法支撑后续Agent模板、RAG等功能的开发。优先实现模型管理，为AI能力建设打下基础。

## What Changes

1. **模型管理** - 添加/编辑/删除AI模型配置，支持API Key管理
2. **模型接入** - 适配OpenAI、智谱GLM、阿里通义、百度文心、Minimax等主流模型
3. **模型测试** - 在线测试模型对话能力
4. **模型网关** - 统一调用接口，支持动态切换模型

## Capabilities

### New Capabilities
- `model-management`: 模型的CRUD、API Key安全存储、状态管理
- `model-provider`: 多模型Provider适配器(OpenAI/智谱/通义/文心/Minimax)
- `model-gateway`: 统一模型调用接口、流式响应支持
- `model-testing`: 在线模型测试页面

### Modified Capabilities
- (无) - 新增功能

## Impact

- **后端**: 新增模型管理模块 (aihub-ai-infrastructure)
- **前端**: 新增模型管理页面、模型测试页面
- **数据库**: 新增ai_model表
- **依赖**: OkHttp(HTTP客户端)、各模型SDK(可选)
