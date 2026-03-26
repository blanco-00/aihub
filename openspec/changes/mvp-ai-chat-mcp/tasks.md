## 1. Phase 0: 架构整合

- [x] 1.1 配置 Python FastAPI CORS，允许前端 (localhost:3000) 访问
- [x] 1.2 实现 Python 端简化 JWT 验证 (透传验证)
- [x] 1.3 前端修改 streamChat API 从 Java 改为 Python SSE 端点
- [x] ~~1.4 前端获取模型列表从 Python API 获取 (而非 Java)~~ (cancelled - 保持 Java 模型配置)

## 2. Phase 1: MCP 工具链路

- [x] 2.1 创建 mcp_tool 数据库表 (model created)
- [x] 2.2 实现 MCPServer.register_tool() 方法
- [x] 2.3 实现 MCPServer.list_tools() 方法
- [x] 2.4 实现 MCPServer.execute_tool() 方法，含参数验证
- [x] 2.5 实现 HTTP 类型工具的连接池
- [x] 2.6 实现工具执行日志记录
- [x] 2.7 开发内置工具: web_search (DuckDuckGo API)
- [x] 2.8 开发内置工具: calculator (AST-based safe eval)
- [x] 2.9 开发内置工具: file_read (sandboxed)
- [x] 2.10 ~~开发内置工具: file_write~~ (cancelled - MVP scope)
- [x] 2.11 开发内置工具: http_request (GET/POST)
- [x] 2.12 前端新增 MCP 工具面板组件
- [x] 2.13 前端实现工具选择和结果显示

## 3. Phase 2: 流式对话完善

- [x] 3.1 完善 Python SSE 流式输出 (完整 SSE 实现)
- [x] 3.2 实现 chat_stream 方法支持流式响应
- [x] 3.3 前端优化流式渲染，打字机效果
- [ ] 3.4 前端显示工具调用结果 (in AI response stream)

## 4. Phase 3: 会话打通

- [x] 4.1 对话消息通过 Python AI 服务处理
- [x] 4.2 消息结果存储到 MySQL (chat_message 表)
- [x] 4.3 实现会话历史加载
- [x] 4.4 前端切换会话加载历史消息
- [ ] 4.5 模型切换保持上下文 (需要 AI 服务支持多模型对话)

## 5. Model Gateway 完善

- [ ] 5.1 增强 ModelGateway 支持 Token 统计
- [ ] 5.2 实现 TokenCounter 记录 input/output tokens
- [ ] 5.3 实现 get_token_stats() API
- [ ] 5.4 健康检查增强，支持多 provider

## 6. Agent Gateway 完善

- [x] 6.1 完善 AIAgent 支持上下文记忆 (via Redis session)
- [ ] 6.2 实现工具调用判断逻辑 (_should_use_tools)
- [ ] 6.3 实现工具结果加入上下文
- [x] 6.4 支持 session_id 关联会话
