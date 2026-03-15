## Why

AIHub需要明确技术架构，定位为"AI能力编排平台"，统一管理Agent、RAG、MCP等AI服务。通过Higress作为统一入口网关，实现流量治理、鉴权、限流，避免重复造轮子。

## What Changes

1. **技术栈分层** - Python用于AI相关服务(模型网关/RAG)，Java用于企业服务(平台/MCP)
2. **统一入口网关** - 使用Higress作为单一入口，处理鉴权、限流、日志
3. **多服务架构** - 平台服务、Agent服务、模型网关、RAG服务、MCP服务独立部署
4. **Higress路由规则** - 按路径路由到不同服务，统一插件配置

## Capabilities

### New Capabilities
- `architecture-overview`: 系统架构总览和技术选型说明
- `higress-config`: Higress网关配置规范

### Modified Capabilities
- (无) - 架构文档类

## Impact

- **部署架构**: Docker Compose多服务部署
- **依赖组件**: Higress, MySQL, Redis, Python服务
- **文档更新**: 需要更新部署文档和架构文档
