# 前端API对接文档

> 本文档定义了前端与后端API的对接规范、接口定义和错误处理方式。

## 📚 相关文档

- [前端开发指南](./guide.md) - 返回前端文档总览
- [页面规划文档](./pages.md) - 查看页面功能需求

## API设计原则

1. **RESTful风格**: 遵循REST规范
2. **统一响应格式**: 所有API返回统一格式
3. **错误处理**: 明确的错误码和错误信息
4. **版本控制**: API版本化
5. **认证授权**: Token-based认证

## API基础配置

### Base URL
```
开发环境: http://localhost:8080/api/v1
生产环境: https://api.yourdomain.com/api/v1
```

### 请求头
```typescript
headers: {
  'Content-Type': 'application/json',
  'Authorization': 'Bearer {token}',
  'X-Request-ID': '{uuid}' // 用于追踪请求
}
```

### 响应格式
```typescript
// 成功响应
{
  code: 200,
  message: 'success',
  data: { ... },
  timestamp: 1234567890
}

// 错误响应
{
  code: 400,
  message: 'error message',
  error: {
    type: 'VALIDATION_ERROR',
    details: { ... }
  },
  timestamp: 1234567890
}
```

## API模块划分

### 1. 认证模块 (Auth)

#### POST /auth/login
**功能**: 用户登录
**请求**:
```typescript
{
  username: string;
  password: string;
}
```
**响应**:
```typescript
{
  token: string;
  refreshToken: string;
  user: UserInfo;
}
```

#### POST /auth/logout
**功能**: 用户登出

#### POST /auth/refresh
**功能**: 刷新Token

---

### 2. 模型管理 (Models)

#### GET /models
**功能**: 获取模型列表
**查询参数**:
- page: number
- pageSize: number
- vendor?: string
- status?: 'active' | 'inactive'
- keyword?: string

**响应**:
```typescript
{
  list: Model[];
  total: number;
  page: number;
  pageSize: number;
}
```

#### GET /models/:id
**功能**: 获取模型详情

#### POST /models
**功能**: 创建模型
**请求**:
```typescript
{
  name: string;
  vendor: string;
  modelId: string;
  apiKey: string;
  baseUrl?: string;
  config: ModelConfig;
}
```

#### PUT /models/:id
**功能**: 更新模型

#### DELETE /models/:id
**功能**: 删除模型

#### POST /models/:id/test
**功能**: 测试模型
**请求**:
```typescript
{
  prompt: string;
  config?: ModelConfig;
}
```

---

### 3. Agent管理 (Agents)

#### GET /agents
**功能**: 获取Agent列表

#### GET /agents/:id
**功能**: 获取Agent详情

#### POST /agents
**功能**: 创建Agent
**请求**:
```typescript
{
  name: string;
  description?: string;
  workflow: WorkflowNode[];
  promptId?: string;
  config: AgentConfig;
}
```

#### PUT /agents/:id
**功能**: 更新Agent

#### DELETE /agents/:id
**功能**: 删除Agent

#### POST /agents/:id/execute
**功能**: 执行Agent
**请求**:
```typescript
{
  input: Record<string, any>;
  context?: Record<string, any>;
}
```

#### GET /agents/:id/logs
**功能**: 获取执行日志

---

### 4. Prompt管理 (Prompts)

#### GET /prompts
**功能**: 获取Prompt列表

#### GET /prompts/:id
**功能**: 获取Prompt详情（含版本信息）

#### POST /prompts
**功能**: 创建Prompt
**请求**:
```typescript
{
  title: string;
  content: string;
  description?: string;
  tags?: string[];
  category?: string;
}
```

#### PUT /prompts/:id
**功能**: 更新Prompt（创建新版本）

#### DELETE /prompts/:id
**功能**: 删除Prompt

#### GET /prompts/:id/versions
**功能**: 获取版本历史

#### POST /prompts/:id/render
**功能**: 渲染Prompt（测试变量替换）
**请求**:
```typescript
{
  variables: Record<string, any>;
  versionId?: string;
}
```

---

### 5. MCP管理 (MCP)

#### GET /mcp/servers
**功能**: 获取MCP Server列表

#### GET /mcp/servers/:id
**功能**: 获取Server详情

#### POST /mcp/servers
**功能**: 创建Server

#### PUT /mcp/servers/:id
**功能**: 更新Server

#### DELETE /mcp/servers/:id
**功能**: 删除Server

#### POST /mcp/servers/:id/test
**功能**: 测试连接

#### GET /mcp/market
**功能**: 获取MCP市场列表

#### POST /mcp/market/:id/install
**功能**: 安装市场中的Server

---

### 6. 权限管理 (Permissions)

#### GET /users
**功能**: 获取用户列表

#### POST /users
**功能**: 创建用户

#### PUT /users/:id
**功能**: 更新用户

#### DELETE /users/:id
**功能**: 删除用户

#### GET /roles
**功能**: 获取角色列表

#### POST /roles
**功能**: 创建角色

#### PUT /roles/:id
**功能**: 更新角色（权限配置）

#### GET /api-keys
**功能**: 获取API Key列表

#### POST /api-keys
**功能**: 生成新Key

#### DELETE /api-keys/:id
**功能**: 撤销Key

---

### 7. 监控统计 (Monitoring)

#### GET /monitoring/token-usage
**功能**: 获取Token消耗统计
**查询参数**:
- startDate: string
- endDate: string
- dimension?: 'department' | 'project' | 'user' | 'model'
- groupBy?: string

**响应**:
```typescript
{
  summary: {
    total: number;
    cost: number;
    trend: number; // 百分比
  };
  details: Array<{
    dimension: string;
    value: number;
    cost: number;
  }>;
  timeline: Array<{
    date: string;
    value: number;
  }>;
}
```

#### GET /monitoring/cost-analysis
**功能**: 成本分析

#### GET /monitoring/alerts
**功能**: 获取告警列表

#### POST /monitoring/alerts
**功能**: 创建告警规则

---

### 8. 系统设置 (Settings)

#### GET /settings
**功能**: 获取系统配置

#### PUT /settings
**功能**: 更新系统配置

#### GET /settings/audit-logs
**功能**: 获取审计日志

---

## 前端API封装

### API Client封装
```typescript
// services/apiClient.ts
import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_APP_API_BASE_URL,
  timeout: 10000,
});

// 请求拦截器
apiClient.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    // 统一错误处理
    handleError(error);
    return Promise.reject(error);
  }
);

export default apiClient;
```

### Service层封装
```typescript
// services/modelService.ts
import apiClient from './apiClient';

export const modelService = {
  getList: (params: ModelListParams) => 
    apiClient.get('/models', { params }),
  
  getById: (id: string) => 
    apiClient.get(`/models/${id}`),
  
  create: (data: CreateModelRequest) => 
    apiClient.post('/models', data),
  
  update: (id: string, data: UpdateModelRequest) => 
    apiClient.put(`/models/${id}`, data),
  
  delete: (id: string) => 
    apiClient.delete(`/models/${id}`),
  
  test: (id: string, data: TestModelRequest) => 
    apiClient.post(`/models/${id}/test`, data),
};
```

### Vue Query集成（可选）
```typescript
// composables/useModels.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query';
import { modelService } from '../services/modelService';

export const useModels = (params: ModelListParams) => {
  return useQuery({
    queryKey: ['models', params],
    queryFn: () => modelService.getList(params),
  });
};

export const useCreateModel = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: modelService.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['models'] });
    },
  });
};
```

**注意**: 也可以直接使用 Pinia 进行状态管理，配合 Axios 实现数据获取和缓存。

---

## 错误处理

### 错误码定义
```typescript
enum ErrorCode {
  // 4xx
  BAD_REQUEST = 400,
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
  NOT_FOUND = 404,
  VALIDATION_ERROR = 422,
  
  // 5xx
  INTERNAL_ERROR = 500,
  SERVICE_UNAVAILABLE = 503,
}
```

### 错误处理策略
1. **网络错误**: 显示网络错误提示，支持重试
2. **认证错误**: 跳转登录页
3. **权限错误**: 显示无权限提示
4. **业务错误**: 显示具体错误信息
5. **未知错误**: 显示通用错误提示

---

## 请求优化

### 1. 请求去重
使用 Vue Query 的请求去重功能，或通过 Pinia store 实现请求去重

### 2. 请求缓存
- 静态数据：长期缓存
- 动态数据：短期缓存（5分钟）
- 实时数据：不缓存

### 3. 请求合并
批量请求合并为单个请求

### 4. 请求取消
组件卸载时取消未完成的请求

---

## Mock数据

开发阶段使用Mock数据：
- Mock Service Worker (MSW)
- JSON Server
- 本地Mock文件

