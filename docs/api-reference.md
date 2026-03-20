# API 接口文档

## 认证接口

### 登录

```
POST /api/auth/login
```

**请求体**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### 登出

```
POST /api/auth/logout
```

**请求头**
```
Authorization: Bearer <token>
```

---

## 用户管理

### 获取用户列表

```
GET /api/users
```

**查询参数**
| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码，默认 1 |
| size | int | 每页数量，默认 10 |
| keyword | string | 搜索关键词 |

---

## Agent 接口

### 创建 Agent

```
POST /api/agent/create
```

**请求体**
```json
{
  "name": "客服助手",
  "template": "客服",
  "model": "gpt-4",
  "prompt": "你是一个专业客服...",
  "ragEnabled": true,
  "mcpTools": ["order-query", "user-info"]
}
```

### 对话

```
POST /api/agent/chat
```

**请求体**
```json
{
  "agentId": "123",
  "message": "你好",
  "stream": false
}
```

**流式响应**
```
POST /api/agent/chat
```

响应为 Server-Sent Events (SSE)

---

## RAG 接口

### 上传文档

```
POST /api/rag/documents
```

**请求体** (multipart/form-data)
| 参数 | 类型 | 说明 |
|------|------|------|
| file | File | 文档文件 |
| knowledgeBaseId | string | 知识库 ID |

### 检索

```
POST /api/rag/search
```

**请求体**
```json
{
  "knowledgeBaseId": "kb-123",
  "query": "产品特点是什么？",
  "topK": 5
}
```

---

## MCP 接口

### 获取工具列表

```
GET /api/mcp/tools
```

**响应**
```json
{
  "code": 200,
  "data": [
    {
      "id": "order-query",
      "name": "订单查询",
      "description": "查询用户订单状态",
      "parameters": []
    }
  ]
}
```

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
