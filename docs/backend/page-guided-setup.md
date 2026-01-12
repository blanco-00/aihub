# 页面引导式初始化方案设计

> 本文档描述通过页面引导用户完成数据库配置和初始化的设计方案。

## 方案概述

通过前端页面引导用户逐步完成：
1. 数据库连接配置
2. 数据库创建（可选，需要用户手动创建）
3. 数据库表结构初始化
4. 超级管理员创建

## 架构设计

### 前端流程

```
1. 配置页面 (/setup)
   ├─ 数据库连接信息表单
   ├─ 测试连接按钮
   └─ 保存配置

2. 初始化页面 (/init)
   ├─ 检查数据库状态
   ├─ 初始化表结构
   └─ 创建超级管理员
```

### 后端设计

#### 方案 A：动态数据源配置（推荐）

**优点**：
- 配置存储在本地文件，不提交到 Git
- 支持运行时修改配置
- 用户体验好

**实现**：
1. 创建配置存储服务，将配置保存到 `~/.aihub/config.yml` 或 `application-local.yml`
2. 使用 Spring 的 `@ConditionalOnProperty` 动态创建数据源
3. 配置变更后重启数据源连接池

**代码结构**：
```java
@Configuration
public class DynamicDataSourceConfig {
    // 从配置文件或环境变量读取配置
    // 动态创建数据源
}
```

#### 方案 B：配置向导 API

**优点**：
- 实现简单
- 配置验证清晰

**实现**：
1. 提供配置测试接口 `/api/setup/test-connection`
2. 提供配置保存接口 `/api/setup/save-config`
3. 配置保存到本地文件，应用重启后生效

## 安全考虑

### 配置存储

1. **本地文件存储**：
   - 配置保存在 `~/.aihub/config.yml` 或项目目录下的 `application-local.yml`
   - 这些文件已在 `.gitignore` 中，不会被提交

2. **加密存储**（可选）：
   - 敏感信息（如密码）可以加密存储
   - 使用 AES 加密，密钥存储在环境变量中

3. **权限控制**：
   - 配置文件权限设置为 `600`（仅所有者可读写）

### 传输安全

1. **HTTPS**：生产环境必须使用 HTTPS
2. **密码字段**：前端使用 `type="password"`，不显示明文
3. **不在日志中记录密码**：确保日志不包含敏感信息

## 实现步骤

### 阶段一：配置测试功能

1. 创建配置测试接口
2. 前端添加配置表单
3. 实现连接测试功能

### 阶段二：配置保存功能

1. 实现配置保存到本地文件
2. 支持配置热重载（可选）
3. 配置验证和错误处理

### 阶段三：完整引导流程

1. 整合配置和初始化流程
2. 优化用户体验
3. 添加帮助文档和提示

## 用户体验设计

### 配置页面

```
┌─────────────────────────────────────┐
│  系统配置向导                        │
├─────────────────────────────────────┤
│                                     │
│  数据库连接配置                      │
│  ┌─────────────────────────────┐   │
│  │ 主机地址: [localhost    ]   │   │
│  │ 端口:     [3306         ]   │   │
│  │ 数据库名: [aihub        ]   │   │
│  │ 用户名:   [root         ]   │   │
│  │ 密码:     [••••••••     ]   │   │
│  └─────────────────────────────┘   │
│                                     │
│  [测试连接]  [保存配置]             │
│                                     │
└─────────────────────────────────────┘
```

### 初始化页面

```
┌─────────────────────────────────────┐
│  系统初始化                          │
├─────────────────────────────────────┤
│                                     │
│  ✓ 数据库连接正常                    │
│  ⚠ 数据库不存在，请先创建            │
│     CREATE DATABASE aihub ...       │
│                                     │
│  [初始化表结构]                      │
│                                     │
│  创建超级管理员                      │
│  ...                                │
└─────────────────────────────────────┘
```

## 技术实现细节

### 后端 API 设计

```java
@RestController
@RequestMapping("/api/setup")
public class SetupController {
    
    // 测试数据库连接
    @PostMapping("/test-connection")
    public Result<ConnectionTestResult> testConnection(@RequestBody DatabaseConfigDTO config);
    
    // 保存配置
    @PostMapping("/save-config")
    public Result<Void> saveConfig(@RequestBody DatabaseConfigDTO config);
    
    // 获取当前配置（不返回密码）
    @GetMapping("/config")
    public Result<DatabaseConfigDTO> getConfig();
}
```

### 前端实现

```typescript
// 配置服务
export const setupService = {
  testConnection: (config: DatabaseConfig) => Promise<ConnectionTestResult>,
  saveConfig: (config: DatabaseConfig) => Promise<void>,
  getConfig: () => Promise<DatabaseConfig>,
}
```

## 注意事项

1. **配置持久化**：配置必须保存到本地文件，应用重启后仍然有效
2. **配置验证**：保存前必须验证配置的有效性
3. **错误处理**：提供清晰的错误提示和解决建议
4. **向后兼容**：支持通过环境变量和配置文件的方式配置（现有方式）

## 后续优化

1. **配置导入/导出**：支持配置文件的导入导出
2. **多环境配置**：支持开发、测试、生产环境的配置切换
3. **配置中心集成**：支持 Nacos、Consul 等配置中心
4. **配置加密**：敏感信息加密存储
