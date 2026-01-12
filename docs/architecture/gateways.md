# 网关架构设计

> 本文档详细描述Agents网关、模型网关、MCP网关的设计和实现。

## 📚 相关文档

- [项目总览](../README.md) - 项目主文档入口
- [系统架构总览](./overview.md) - 返回架构总览
- [Agent模板方案](../agent-template-solution.md) - Agent模板化设计

## 🎯 网关设计原则

1. **统一入口** - 所有请求通过网关
2. **路由转发** - 智能路由到后端服务
3. **负载均衡** - 支持多实例负载均衡
4. **流量控制** - 限流、熔断、降级
5. **监控统计** - 请求统计、性能监控
6. **安全控制** - 认证、鉴权、审计

---

## 1. Agents网关

### 1.1 功能概述

Agents网关是Agent服务的统一入口，负责：
- Agent请求路由转发
- 负载均衡（多Agent实例）
- 流量控制和限流
- 请求监控和统计
- Agent实例管理

### 1.2 架构设计

```
客户端请求
  ↓
Agents网关
  ├─→ 认证鉴权
  ├─→ 路由匹配 (根据Agent ID)
  ├─→ 负载均衡 (多实例)
  ├─→ 流量控制 (限流、熔断)
  ├─→ 请求转发
  └─→ 响应返回
```

### 1.3 核心功能

#### 路由转发
```java
// 伪代码示例
class AgentGateway {
    // 根据Agent ID路由到对应的Agent实例
    route(agentId, request) {
        agent = agentService.getAgent(agentId);
        instance = loadBalancer.select(agent.instances);
        return forward(instance, request);
    }
}
```

#### 负载均衡
- **策略**: 轮询、加权轮询、最少连接、一致性哈希
- **实现**: Spring Cloud LoadBalancer 或自定义实现

#### 流量控制
- **限流**: 基于Agent ID、用户ID、IP等维度
- **熔断**: 服务异常时自动熔断
- **降级**: 高负载时降级处理

#### 监控统计
- 请求量统计
- 响应时间统计
- 错误率统计
- Agent使用情况

### 1.4 API设计

#### Agent交互接口
```
POST /api/v1/agents/{agentId}/chat
Content-Type: application/json

Request:
{
  "message": "用户消息",
  "conversationId": "会话ID（可选）",
  "stream": false  // 是否流式返回
}

Response:
{
  "code": 200,
  "data": {
    "message": "Agent回复",
    "conversationId": "会话ID",
    "metadata": {
      "tokens": 100,
      "latency": 1500
    }
  }
}
```

#### WebSocket接口（流式）
```
WS /api/v1/agents/{agentId}/chat/stream
```

### 1.5 实现要点

1. **路由规则配置**
   - Agent ID到实例的映射
   - 支持动态路由配置

2. **负载均衡器**
   - 集成Spring Cloud LoadBalancer
   - 支持健康检查

3. **限流器**
   - 使用Redis实现分布式限流
   - 支持多种限流策略

4. **监控集成**
   - 集成Micrometer
   - 导出到Prometheus

---

## 2. 模型网关

### 2.1 功能概述

模型网关是LLM模型调用的统一入口，负责：
- 多种模型统一接入（OpenAI、Claude、DeepSeek等）
- 模型路由和负载均衡
- Token消耗统计
- 模型切换和故障转移
- 流量控制和成本控制

### 2.2 架构设计

```
Agent服务请求
  ↓
模型网关
  ├─→ 模型选择策略
  ├─→ 负载均衡
  ├─→ 流量控制
  ├─→ 调用外部API
  ├─→ Token统计
  └─→ 返回结果
```

### 2.3 核心功能

#### 模型路由
```java
// 伪代码示例
class ModelGateway {
    // 根据策略选择模型
    selectModel(request) {
        strategy = getRoutingStrategy(request);
        models = modelService.getAvailableModels(strategy);
        return loadBalancer.select(models);
    }
    
    // 路由策略
    routingStrategy(request) {
        // 策略1: 按模型类型
        // 策略2: 按成本
        // 策略3: 按性能
        // 策略4: 按用户配置
    }
}
```

#### 负载均衡
- **策略**: 轮询、加权轮询、最少连接
- **权重**: 根据模型性能、成本设置权重

#### Token统计
- 输入Token统计
- 输出Token统计
- 按部门/项目/人员统计
- 成本计算

#### 模型切换
- **故障切换**: 模型异常时自动切换
- **降级切换**: 高负载时切换到备用模型
- **成本优化**: 根据成本自动切换

### 2.4 API设计

#### 模型调用接口
```
POST /api/v1/models/chat
Content-Type: application/json

Request:
{
  "model": "gpt-4",  // 可选，不指定则自动选择
  "messages": [...],
  "temperature": 0.7,
  "maxTokens": 1000
}

Response:
{
  "code": 200,
  "data": {
    "content": "模型回复",
    "model": "gpt-4",
    "usage": {
      "promptTokens": 100,
      "completionTokens": 200,
      "totalTokens": 300
    },
    "latency": 1500
  }
}
```

### 2.5 实现要点

1. **模型适配器**
   - 统一模型接口
   - 各厂商API适配

2. **Token计算**
   - 集成tiktoken等库
   - 缓存Token计算结果

3. **成本统计**
   - 实时统计Token消耗
   - 按维度聚合统计

4. **故障转移**
   - 健康检查
   - 自动切换备用模型

---

## 3. MCP网关

### 3.1 功能概述

MCP网关是MCP工具的统一管理入口，负责：
- MCP Server连接管理
- 工具调用路由
- 连接池管理
- 流量控制
- 工具调用统计

### 3.2 架构设计

```
Agent服务请求
  ↓
MCP网关
  ├─→ 工具路由 (根据工具ID)
  ├─→ 连接池管理
  ├─→ 流量控制
  ├─→ 调用MCP Server
  ├─→ 结果处理
  └─→ 返回结果
```

### 3.3 核心功能

#### 工具路由
```java
// 伪代码示例
class MCPGateway {
    // 根据工具ID路由到对应的MCP Server
    route(toolId, params) {
        tool = mcpService.getTool(toolId);
        server = tool.mcpServer;
        connection = connectionPool.get(server);
        return connection.call(tool, params);
    }
}
```

#### 连接池管理
- **连接池**: 维护MCP Server连接池
- **连接复用**: 复用长连接，减少开销
- **健康检查**: 定期检查连接健康状态

#### 流量控制
- **限流**: 按MCP Server、工具维度限流
- **超时控制**: 设置调用超时时间
- **重试机制**: 失败自动重试

#### 工具管理
- 工具注册和发现
- 工具参数验证
- 工具调用日志

### 3.4 API设计

#### 工具调用接口
```
POST /api/v1/mcp/tools/{toolId}/invoke
Content-Type: application/json

Request:
{
  "arguments": {
    "param1": "value1",
    "param2": "value2"
  }
}

Response:
{
  "code": 200,
  "data": {
    "result": "工具执行结果",
    "metadata": {
      "latency": 500,
      "server": "mcp-server-1"
    }
  }
}
```

#### 工具列表接口
```
GET /api/v1/mcp/tools

Response:
{
  "code": 200,
  "data": [
    {
      "id": "tool-1",
      "name": "数据库查询",
      "description": "...",
      "server": "mcp-server-1",
      "parameters": {...}
    }
  ]
}
```

### 3.5 实现要点

1. **MCP协议实现**
   - 实现MCP协议客户端
   - 支持HTTP/WebSocket等传输方式

2. **连接池**
   - 使用Apache Commons Pool或自定义
   - 支持连接复用和健康检查

3. **工具注册**
   - 工具自动发现
   - 工具元数据管理

4. **监控统计**
   - 工具调用统计
   - 连接池状态监控

---

## 4. 网关统一特性

### 4.1 认证鉴权
- 所有请求需要认证
- 基于JWT Token
- 支持API Key方式

### 4.2 限流策略
- **固定窗口**: 简单限流
- **滑动窗口**: 更平滑的限流
- **令牌桶**: 突发流量处理
- **漏桶**: 平滑流量

### 4.3 熔断降级
- **熔断**: 服务异常时自动熔断
- **降级**: 返回默认响应或缓存
- **恢复**: 自动恢复机制

### 4.4 监控告警
- 请求量监控
- 响应时间监控
- 错误率监控
- 自动告警

### 4.5 日志审计
- 请求日志记录
- 操作审计日志
- 日志查询和分析

---

## 5. 技术实现

### 5.1 网关实现方式

#### 方案1: Spring Cloud Gateway（推荐）
- 成熟的网关框架
- 支持路由、限流、熔断
- 性能优秀

#### 方案2: Spring Boot内嵌
- 简单场景使用
- 自定义过滤器
- 轻量级实现

### 5.2 负载均衡

- **Spring Cloud LoadBalancer**: 客户端负载均衡
- **Nginx**: 服务端负载均衡（可选）

### 5.3 限流实现

- **Redis + Lua**: 分布式限流
- **Guava RateLimiter**: 单机限流
- **Sentinel**: 阿里开源限流框架

### 5.4 监控集成

- **Micrometer**: 指标收集
- **Prometheus**: 指标存储
- **Grafana**: 可视化展示

---

## 6. 部署架构

```
┌─────────────┐
│  Nginx      │  (可选，反向代理)
└──────┬──────┘
       ↓
┌─────────────────────────┐
│  Agents网关 (多实例)    │
│  模型网关 (多实例)       │
│  MCP网关 (多实例)        │
└──────┬──────────────────┘
       ↓
┌─────────────────────────┐
│  业务服务 (多实例)       │
└─────────────────────────┘
```

---

## 📝 下一步

1. 详细设计各网关的API接口
2. 设计限流、熔断策略
3. 设计监控指标
4. 实现网关核心功能
5. 集成测试

