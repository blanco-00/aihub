# 后端模块架构规划文档

> 本文档记录 AIHub 后端模块架构规划方案和迁移计划

## 📋 目录

- [架构概述](#架构概述)
- [模块划分方案](#模块划分方案)
- [详细模块职责](#详细模块职责)
- [模块依赖关系](#模块依赖关系)
- [迁移计划](#迁移计划)
- [包结构规范](#包结构规范)

## 架构概述

### 设计目标

1. **通用后台管理基础**：将后台管理功能独立成通用模块，可独立使用
2. **AI基础设施支撑**：提供AI能力的底层支撑，不涉及具体业务场景
3. **AI应用可扩展**：AI具体应用模块可插拔，便于扩展和维护
4. **清晰的分层架构**：基础层 → 管理层 → 基础设施层 → 应用层

### 架构原则

- **单一职责**：每个模块职责清晰，边界明确
- **依赖倒置**：上层模块依赖下层模块，下层不依赖上层
- **可复用性**：通用功能提取到公共模块
- **可扩展性**：新功能以模块形式添加，不影响现有功能
- **渐进式迁移**：分阶段迁移，保证系统稳定运行

## 模块划分方案

### 整体架构

```
aihub-backend/
├── aihub-common/              # 通用基础模块（所有模块共享）
│   ├── common-core/          # 核心工具类、常量、枚举
│   ├── common-web/           # Web通用组件（Result、异常处理等）
│   ├── common-security/      # 安全相关（JWT、加密等）
│   └── common-redis/         # Redis工具
│
├── aihub-admin/               # 通用后台管理模块（可独立使用）
│   ├── admin-core/          # 核心实体、DTO
│   ├── admin-api/           # API接口（Controller）
│   ├── admin-service/       # 业务逻辑（Service）
│   └── admin-mapper/        # 数据访问（Mapper）
│
├── aihub-ai-infrastructure/   # AI基础设施模块
│   ├── ai-core/             # AI核心能力（模型管理、推理引擎等）
│   ├── ai-api/              # AI基础设施API
│   ├── ai-service/          # AI基础设施服务
│   └── ai-mapper/           # AI基础设施数据访问
│
├── aihub-ai-applications/     # AI具体应用模块（可扩展）
│   ├── application-llm/     # 大语言模型应用
│   ├── application-vision/   # 视觉AI应用
│   ├── application-audio/    # 音频AI应用
│   └── application-rag/      # RAG应用
│
└── aihub-api/                 # API聚合模块（启动入口）
    └── 整合所有模块，提供统一API入口
```

## 详细模块职责

### 1. aihub-common（通用基础模块）

**职责**：
- 工具类（JWT、Redis、日期、字符串等）
- 统一响应格式（Result、PageResult）
- 全局异常处理（GlobalExceptionHandler）
- 通用配置（MyBatis、Redis、Security等）
- 通用实体基类（BaseEntity）
- 通用枚举、常量

**特点**：
- ✅ 无业务逻辑，纯工具类
- ✅ 可被所有模块依赖
- ✅ 不依赖任何业务模块

**包含内容**：
```
common-core/
  - util/          # 工具类（JwtUtil、RedisUtil、DateUtil等）
  - constant/      # 常量定义
  - enums/         # 通用枚举
  - exception/     # 基础异常类

common-web/
  - dto/           # Result、PageResult等
  - exception/     # GlobalExceptionHandler
  - filter/        # 通用Filter
  - interceptor/   # 通用Interceptor

common-security/
  - jwt/           # JWT工具
  - encrypt/       # 加密工具
  - auth/          # 认证相关工具

common-redis/
  - RedisUtil      # Redis工具类
```

### 2. aihub-admin（通用后台管理模块）

**职责**：
- 用户管理（User）
- 角色权限（Role、Menu、Permission）
- 组织架构（Department）
- 日志审计（LoginLog、OperationLog）
- 系统监控（SystemMonitor）
- 认证授权（Auth）

**特点**：
- ✅ 通用后台管理功能
- ✅ 可独立使用，也可作为其他系统的基础
- ✅ 不包含任何AI相关代码
- ✅ 依赖 aihub-common

**包含内容**：
```
admin-core/
  - entity/        # User、Role、Menu、Department等
  - dto/           # 请求和响应DTO
  - enums/         # 业务枚举

admin-api/
  - controller/    # UserController、RoleController等

admin-service/
  - service/       # Service接口
  - impl/          # Service实现

admin-mapper/
  - mapper/        # Mapper接口
  - xml/           # MyBatis XML
```

### 3. aihub-ai-infrastructure（AI基础设施模块）

**职责**：
- **模型管理**：模型注册、版本管理、模型元数据
- **推理引擎**：统一推理接口、多模型适配、负载均衡
- **向量存储**：向量数据库管理、索引管理
- **Prompt管理**：Prompt模板、版本控制、A/B测试
- **Token管理**：使用量统计、配额管理
- **模型路由**：智能路由、降级策略
- **AI网关**：统一调用入口、限流、熔断

**特点**：
- ✅ 提供AI能力的底层支撑
- ✅ 不涉及具体业务场景
- ✅ 可被多个AI应用复用
- ✅ 依赖 aihub-common，可选依赖 aihub-admin（用于权限控制）

**包含内容**：
```
ai-core/
  - model/         # 模型实体、DTO
  - prompt/        # Prompt实体、DTO
  - vector/        # 向量存储实体、DTO

ai-api/
  - controller/    # ModelController、InferenceController等

ai-service/
  - model/         # 模型管理服务
  - inference/     # 推理引擎服务
  - prompt/        # Prompt管理服务
  - vector/        # 向量存储服务
  - gateway/       # AI网关服务

ai-mapper/
  - mapper/        # 数据访问层
```

### 4. aihub-ai-applications（AI具体应用模块）

**职责**：
- 具体AI应用场景的实现
- 每个应用模块独立，可插拔
- 依赖基础设施模块

**特点**：
- ✅ 具体业务场景实现
- ✅ 模块独立，可插拔
- ✅ 依赖 aihub-ai-infrastructure
- ✅ 可选依赖 aihub-admin（用于权限控制）

**示例应用模块**：

#### application-llm（大语言模型应用）
- 多轮对话
- 上下文管理
- 流式输出
- 对话历史

#### application-vision（视觉AI应用）
- 文生图
- 图生图
- 图像编辑
- 图像识别

#### application-audio（音频AI应用）
- ASR（语音识别）
- TTS（文本转语音）
- 音频处理

#### application-rag（RAG应用）
- 文档索引
- 向量检索
- 检索增强生成
- 知识库管理

### 5. aihub-api（API聚合模块）

**职责**：
- 整合所有模块
- 提供统一API入口
- 启动类、配置文件
- 跨模块的API编排

**特点**：
- ✅ 应用启动入口
- ✅ 依赖所有业务模块
- ✅ 统一配置管理
- ✅ API路由和编排

**包含内容**：
```
- AihubApplication.java    # 启动类
- config/                  # 应用级配置
- resources/
  - application.yml        # 配置文件
  - db/migration/         # 数据库迁移脚本（整合所有模块）
```

## 模块依赖关系

### 依赖图

```
aihub-api
  ├── aihub-admin
  ├── aihub-ai-infrastructure
  └── aihub-ai-applications
        │
        └── 都依赖 aihub-common
```

### 依赖规则

1. **aihub-common**：不依赖任何业务模块
2. **aihub-admin**：只依赖 aihub-common
3. **aihub-ai-infrastructure**：依赖 aihub-common，可选依赖 aihub-admin
4. **aihub-ai-applications**：依赖 aihub-ai-infrastructure 和 aihub-common，可选依赖 aihub-admin
5. **aihub-api**：依赖所有模块

## 迁移计划

### 第一阶段：拆分 aihub-common 和 aihub-admin

**目标**：
- 将通用工具类提取到 `aihub-common`
- 将后台管理功能迁移到 `aihub-admin`
- 保持现有功能正常运行

**具体步骤**：

1. **创建 aihub-common 模块**
   - 创建 Maven 模块
   - 提取工具类：`JwtUtil`、`RedisUtil`
   - 提取通用DTO：`Result`、`PageResult`
   - 提取异常处理：`BusinessException`、`GlobalExceptionHandler`
   - 提取通用配置：`MyBatisPlusConfig`、`RedisConfig`、`SecurityConfig`

2. **创建 aihub-admin 模块**
   - 创建 Maven 模块
   - 迁移实体类：`User`、`Role`、`Menu`、`Department`、`LoginLog`、`OperationLog`
   - 迁移Controller：`UserController`、`RoleController`、`MenuController`、`DepartmentController`、`AuthController`、`LoginLogController`、`OperationLogController`
   - 迁移Service：对应的Service和ServiceImpl
   - 迁移Mapper：对应的Mapper和XML
   - 迁移DTO：所有admin相关的DTO

3. **更新 aihub-api 模块**
   - 添加对 `aihub-common` 和 `aihub-admin` 的依赖
   - 移除已迁移的代码
   - 保留启动类和配置文件
   - 测试验证功能正常

**验收标准**：
- ✅ 所有现有功能正常运行
- ✅ 代码结构清晰，模块职责明确
- ✅ 无编译错误和运行时错误

### 第二阶段：创建 aihub-ai-infrastructure

**目标**：
- 搭建AI基础设施框架
- 实现模型管理、推理引擎等核心能力

**具体步骤**：

1. **创建 aihub-ai-infrastructure 模块**
   - 创建 Maven 模块
   - 设计数据库表结构（模型表、Prompt表等）
   - 实现模型管理功能
   - 实现推理引擎接口
   - 实现Prompt管理功能
   - 实现Token统计功能

2. **集成到 aihub-api**
   - 添加依赖
   - 配置相关Bean
   - 测试验证

**验收标准**：
- ✅ AI基础设施框架搭建完成
- ✅ 核心功能可用
- ✅ 接口设计合理，便于扩展

### 第三阶段：开发 aihub-ai-applications

**目标**：
- 根据业务需求逐步添加应用模块

**具体步骤**：

1. **创建第一个应用模块**（如 application-llm）
   - 创建 Maven 模块
   - 实现具体业务逻辑
   - 依赖 aihub-ai-infrastructure

2. **逐步添加其他应用模块**
   - 按需创建新模块
   - 保持模块独立性

**验收标准**：
- ✅ 应用模块功能完整
- ✅ 模块间解耦良好
- ✅ 可独立开发和部署

### 第四阶段：优化 aihub-api

**目标**：
- 整合所有模块
- 优化API设计
- 完善配置管理

**具体步骤**：

1. **API整合优化**
   - 统一API路由
   - 优化跨模块调用
   - 完善错误处理

2. **配置管理优化**
   - 统一配置文件管理
   - 环境配置分离
   - 配置验证

3. **性能优化**
   - 接口性能优化
   - 数据库查询优化
   - 缓存策略优化

**验收标准**：
- ✅ 所有模块整合完成
- ✅ API设计统一规范
- ✅ 性能满足要求

## 包结构规范

### 每个模块的标准包结构

```
com.aihub.{module}/
├── controller/     # API层（Controller）
│   └── {entity}Controller.java
├── service/        # 业务层（Service接口和实现）
│   ├── {entity}Service.java
│   └── impl/
│       └── {entity}ServiceImpl.java
├── mapper/         # 数据访问层（Mapper接口）
│   └── {entity}Mapper.java
├── entity/         # 实体类（对应数据库表）
│   └── {Entity}.java
├── dto/            # 数据传输对象
│   ├── request/    # 请求DTO
│   │   ├── Create{Entity}Request.java
│   │   ├── Update{Entity}Request.java
│   │   └── {Entity}ListRequest.java
│   └── response/   # 响应DTO
│       ├── {Entity}Response.java
│       └── {Entity}ListResponse.java
├── config/         # 配置类（模块特定配置）
│   └── {Module}Config.java
├── enums/          # 枚举类（模块特定枚举）
│   └── {Enum}.java
└── util/           # 工具类（模块内工具类，通用工具放在common）
    └── {Util}.java
```

### 包命名示例

```
# common 模块
com.aihub.common.core.util
com.aihub.common.web.dto
com.aihub.common.security.jwt

# admin 模块
com.aihub.admin.entity
com.aihub.admin.controller
com.aihub.admin.service

# ai-infrastructure 模块
com.aihub.ai.model.entity
com.aihub.ai.inference.service
com.aihub.ai.prompt.controller

# ai-applications 模块
com.aihub.applications.llm.controller
com.aihub.applications.vision.service
```

## 数据库设计

### 数据库划分策略

**方案一：共享数据库（推荐初期使用）**
- 所有模块共享同一个数据库
- 通过表名前缀区分模块（如：`admin_user`、`ai_model`）
- 优点：简单，便于关联查询
- 缺点：模块耦合度高

**方案二：分库分表（后期扩展）**
- 每个模块独立数据库
- 优点：模块完全解耦，可独立部署
- 缺点：跨模块查询复杂，需要分布式事务

**建议**：
- 第一阶段和第二阶段：使用共享数据库
- 第三阶段和第四阶段：根据实际情况考虑分库

### 表命名规范

```
# admin 模块表
admin_user
admin_role
admin_menu
admin_department
admin_login_log
admin_operation_log

# ai-infrastructure 模块表
ai_model
ai_model_version
ai_prompt
ai_prompt_version
ai_vector_store
ai_token_usage

# ai-applications 模块表
app_llm_conversation
app_llm_message
app_vision_image
app_rag_document
```
