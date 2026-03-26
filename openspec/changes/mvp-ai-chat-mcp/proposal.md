## Why

AIHub定位为"像搭乐高一样构建AI Agent"的可视化平台。当前系统已具备基础骨架（Java后端、Python AI服务、Vue前端），但核心AI能力（Agent对话、MCP工具调用）尚未串联形成闭环。前端目前只连接Java后端，Python侧的AI能力未被实际使用，导致：

1. MCP工具框架存在但无实际业务链路
2. Skills技能库框架存在但无法被Agent调用
3. 会话管理与AI对话服务未打通
4. 流式响应（SSE）实现不完整

**为什么现在**：MVP阶段需要验证核心价值流，快速迭代出可用的最小产品。

## What Changes

1. **前端直连Python AI服务** - 前端从直连Java改为按能力选择服务（对话→Python，会话→Java）
2. **MCP工具执行链路** - 完成MCPServer的工具注册、连接池、执行调度、结果返回
3. **Agent对话编排** - 完善AIAgent，支持上下文记忆、工具调用、流式响应
4. **会话与AI服务打通** - 对话消息通过Python AI服务处理，结果存储到MySQL
5. **内置5个MCP工具** - web_search、calculator、file_read、file_write、http_request

## Capabilities

### New Capabilities

- `ai-chat`: AI对话能力，支持流式响应、上下文记忆、工具调用
- `mcp-gateway`: MCP网关能力，统一管理工具注册、发现、执行
- `model-gateway`: 模型网关能力，统一模型接入、Token统计

### Modified Capabilities

- `architecture-overview`: 无需求变更 - 架构描述已涵盖当前设计

## Impact

**后端**:
- `aihub-python/src/aihub/agents/agent.py` - 需完善AIAgent编排逻辑
- `aihub-python/src/aihub/mcp/server.py` - 需完善MCPServer实现
- `aihub-python/src/aihub/services/model_gateway.py` - 需增强Token统计
- `aihub-python/src/aihub/main.py` - 需新增SSE流式对话API

**前端**:
- `frontend/src/views/ai/chat/index.vue` - 需对接Python SSE API
- `frontend/src/store/modules/chat.ts` - 需调整API调用路径

**数据库**:
- 新增 `mcp_tool` 表 - MCP工具配置存储

**服务**:
- Python AI服务 (localhost:8001) - 需配置CORS、简化认证
