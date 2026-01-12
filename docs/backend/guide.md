# 后端开发文档

> 本文档是后端开发的完整指南，包含技术栈选型、项目结构、开发规范等内容。

## 📚 文档导航

- [数据库设计文档](./database.md) - 数据库选型、表结构设计、设计规范
- [系统初始化文档](./initialization.md) - 数据库初始化、超级管理员创建
- [API设计文档](./api-design.md) - RESTful API设计规范（待完善）

## 概述

后端采用Java作为主要开发语言，使用Spring Boot框架构建企业级应用。在特定场景（如AI模型调用、数据处理等）下使用Python作为补充。

## 技术栈

### 主要技术栈（Java）

- **框架**: **Spring Boot 3.x**
- **Java版本**: **Java 17+** (LTS版本)
- **构建工具**: **Maven** 或 **Gradle**
- **ORM**: **MyBatis-Plus** 或 **JPA/Hibernate**
- **数据库**: **MySQL 8.0+** (推荐，使用广泛)
- **缓存**: **Redis** (Spring Data Redis)
- **消息队列**: **RabbitMQ** 或 **Kafka** (可选)
- **认证授权**: **Spring Security** + **JWT**
- **API文档**: **Swagger/OpenAPI 3** (Knife4j)
- **日志**: **Logback** + **SLF4J**
- **监控**: **Spring Boot Actuator** + **Micrometer**

### 辅助技术栈（Python）

Python仅在以下场景使用：

1. **AI模型调用**
   - 调用OpenAI、Claude等API
   - 模型推理服务
   - 使用框架：`openai`, `anthropic`, `langchain`等

2. **数据处理与分析**
   - Token计算
   - 数据统计分析
   - 使用框架：`pandas`, `numpy`等

3. **特定算法实现**
   - 文本处理
   - 敏感词检测
   - 内容合规检测

### 为什么选择Java作为主技术栈？

- **企业级应用优势**:
  - Spring Boot生态成熟，组件丰富
  - 企业级应用开发经验丰富
  - 性能稳定，适合长期运行的服务

- **团队协作**:
  - Java开发规范成熟，易于团队协作
  - 类型安全，减少运行时错误
  - 丰富的企业级框架和工具

- **可维护性**:
  - 代码结构清晰，易于维护
  - 完善的测试框架支持
  - 丰富的监控和运维工具

### Java与Python的协作方式

#### 方案1: HTTP服务调用（推荐）
```
Java后端 <--HTTP--> Python服务
```
- Java通过HTTP调用Python服务
- Python服务独立部署，可水平扩展
- 适合异步任务、批量处理

#### 方案2: 消息队列
```
Java后端 --> 消息队列 --> Python消费者
```
- 通过RabbitMQ/Kafka解耦
- 适合异步处理、任务队列

#### 方案3: 直接集成（不推荐）
```
Java后端 (Jython/Py4J)
```
- 性能开销大，维护复杂
- 仅在特殊场景考虑

## 项目结构

```
backend/
├── aihub-api/              # API模块（主应用）
│   ├── src/main/java/
│   │   └── com/aihub/
│   │       ├── controller/    # 控制器层
│   │       ├── service/       # 业务逻辑层
│   │       ├── mapper/         # 数据访问层
│   │       ├── entity/        # 实体类
│   │       ├── dto/            # 数据传输对象
│   │       ├── vo/             # 视图对象
│   │       ├── config/         # 配置类
│   │       ├── exception/      # 异常处理
│   │       └── util/           # 工具类
│   └── src/main/resources/
│       ├── application.yml     # 配置文件
│       └── mapper/             # MyBatis XML
│
├── aihub-common/          # 公共模块
│   ├── common-core/       # 核心工具类
│   ├── common-security/   # 安全相关
│   └── common-web/        # Web相关
│
├── aihub-ai-service/      # AI服务模块（Python）
│   ├── model_service/     # 模型调用服务
│   ├── token_calculator/  # Token计算
│   └── compliance/        # 合规检测
│
└── pom.xml                # Maven父POM
```

## 核心模块规划

### 1. 模型管理模块 (Model Management)
- 模型配置管理
- 模型路由策略
- 模型健康检查
- 模型调用封装

### 2. Agent管理模块 (Agent Management)
- Agent工作流定义
- Agent执行引擎
- Agent状态管理

### 3. Prompt管理模块 (Prompt Management)
- Prompt CRUD
- Prompt版本管理
- Prompt变量替换

### 4. MCP管理模块 (MCP Management)
- MCP Server注册
- MCP工具调用
- MCP连接管理

### 5. 权限管理模块 (Permission Management)
- 用户管理
- 角色管理
- 资源权限控制
- API Key管理

### 6. 监控统计模块 (Monitoring)
- Token消耗统计
- 成本分析
- 使用趋势分析
- 告警管理

### 7. 系统配置模块 (System Settings)
- 系统参数配置
- 合规检测配置
- 流量防护配置

## 开发规范

> 📌 **详细开发规范请参考** [`.cursor/rules/`](../../.cursor/rules/) 目录下的规范文件：
> - [Java 代码规范](../../.cursor/rules/java-code-style.mdc) - Import规范、代码简洁性、抽象复用、实用主义
> - [日志规范](../../.cursor/rules/logging.mdc) - 日志等级、避免重复日志
> - [文档规范](../../.cursor/rules/documentation.mdc) - 文档对应要求、文档入口规范

### 包命名规范
```
com.aihub.{module}.{layer}
例如：
com.aihub.model.controller
com.aihub.model.service
com.aihub.model.mapper
```

### 接口设计规范
- RESTful API设计
- 统一响应格式
- 统一异常处理
- API版本控制

### 数据库规范
- 表名使用下划线命名
- 字段名使用下划线命名
- 必须有创建时间、更新时间字段
- 逻辑删除而非物理删除

## 技术选型说明

### Spring Boot vs Spring Cloud
- **MVP阶段**: 使用Spring Boot单体应用
- **后续扩展**: 如需微服务，可迁移到Spring Cloud

### MyBatis-Plus vs JPA
- **推荐MyBatis-Plus**: 
  - SQL可控性强
  - 性能优化灵活
  - 适合复杂查询场景

### 数据库连接池
- **HikariCP**: Spring Boot默认，性能优秀

### 缓存策略
- **本地缓存**: Caffeine (热点数据)
- **分布式缓存**: Redis (共享数据)

## 下一步

1. 搭建Spring Boot项目脚手架
2. 设计数据库表结构
3. 实现核心API接口
4. 集成Python AI服务
5. 完善监控和日志

## 相关文档

- [项目总览](../README.md) - 项目主文档入口
- [API设计文档](./api-design.md) - 查看API接口设计
- [数据库设计文档](./database.md) - 查看数据库设计
- [系统初始化文档](./initialization.md) - 查看系统初始化流程

