## Why

AIHub需要明确技术架构，定位为"AI能力编排平台"，统一管理Agent、RAG、MCP等AI服务。通过Higress作为统一入口网关，实现流量治理、鉴权、限流，避免重复造轮子。

现状问题：
1. Java 生态缺乏成熟的 Agent 框架，难以实现复杂的工作流
2. LangChain/LangGraph 是 Python 生态的 AI 开发标准，学习价值高
3. 现有 AI 功能与后台管理耦合，架构不够清晰

## What Changes

1. **技术栈分层** - Python用于AI相关服务(模型网关/RAG/Agent/MCP)，Java用于企业服务(平台)
2. **统一入口网关** - 使用Higress作为单一入口，处理鉴权、限流、日志
3. **多服务架构** - 平台服务、Agent服务、模型网关、RAG服务、MCP服务独立部署
4. **Higress路由规则** - 按路径路由到不同服务，统一插件配置
5. **新增 Python AI 服务**
   - 创建 `aihub-python/` 目录，使用 FastAPI + LangChain + LangGraph
   - 独立部署，端口 8000
6. **目录结构重组**
   - `backend/` → `aihub-java/`
   - 新增 `aihub-python/`
   - 统一 `docker/` 目录管理所有服务
7. **AI 功能迁移（渐进式）**
   - 第一阶段：AI 对话功能（LangChain Agent）
   - 第二阶段：模型网关（统一调用入口）
   - 第三阶段：RAG 知识库
8. **数据共享**
   - Python 直连 MySQL，共享 model_config 等表
   - 共用 Redis，读取用户会话
9. **统一认证** - 前后端统一 JWT 认证
10. **文档体系优化** - 参照成功开源项目（LangChain、FastAPI）优化文档

## Capabilities

### New Capabilities
- `architecture-overview`: 系统架构总览和技术选型说明
- `higress-config`: Higress网关配置规范
- `python-ai-service`: 独立的 Python AI 服务，提供基于 LangChain/LangGraph 的 AI 对话、模型网关和 RAG 功能
- `ai-directory-restructuring`: 目录结构重组，将 backend 重命名为 aihub-java，新增 aihub-python
- `documentation-optimization`: 文档体系优化，参照成功开源项目经验

### Modified Capabilities
- (无) - 架构文档类

## Impact

- **前端**：新增/修改 AI 对话页面 API 指向
- **后端**：Java AI 代码暂时保留，逐步迁移
- **数据库**：无新增表，复用现有 model_config 等表
- **部署**：docker-compose 新增 Python 服务
- **依赖**：新增 Python 依赖（FastAPI, LangChain, LangGraph 等）
- **文档**：README 简化，文档结构重组，新增教程和示例
