## 1. 架构文档整理

- [x] 1.1 创建系统架构文档 (docs/architecture/system-architecture.md)
- [ ] 1.2 更新部署文档，添加Higress配置
- [x] 1.3 创建技术选型说明文档

## 2. Higress配置

- [ ] 2.1 创建Higress路由规则配置
- [ ] 2.2 配置服务间鉴权插件
- [ ] 2.3 配置限流规则 (按用户/按模型)

## 3. 服务架构定义

- [x] 3.1 定义平台服务职责和API
- [x] 3.2 定义Agent服务职责和API
- [x] 3.3 定义模型网关职责和API
- [x] 3.4 定义RAG服务职责和API
- [x] 3.5 定义MCP服务职责和API

## 4. 目录结构重组

- [x] 4.1 将 `backend/` 目录重命名为 `aihub-java/`
- [x] 4.2 创建 `aihub-python/` 目录结构
- [x] 4.3 更新根目录的 .gitignore 和配置文件引用

## 5. Python 基础服务搭建

- [x] 5.1 创建 `pyproject.toml` 并配置依赖
- [x] 5.2 创建 `Dockerfile` 用于 Python 服务
- [x] 5.3 创建 FastAPI 入口 `main.py`
- [x] 5.4 配置日志和健康检查端点

## 6. 数据库和缓存连接

- [x] 6.1 配置 MySQL 连接（SQLAlchemy）
- [x] 6.2 配置 Redis 连接（redis-py）
- [x] 6.3 读取 model_config 表的实现

## 7. 认证中间件

- [x] 7.1 实现 JWT 验证中间件
- [x] 7.2 实现用户信息解析
- [x] 7.3 配置需要认证的路由

## 8. AI 对话功能（第一阶段）

- [x] 8.1 创建 LangChain Agent 基础结构
- [x] 8.2 实现模型网关封装
- [x] 8.3 实现 `/api/agent/chat` API
- [x] 8.4 实现流式对话支持

## 9. Docker Compose 配置

- [x] 9.1 更新 docker-compose 添加 Python 服务
- [x] 9.2 配置服务间网络连接
- [x] 9.3 添加环境变量配置

## 12. Agent 核心功能

- [x] 12.1 创建 LangChain Agent 基础结构
- [x] 12.2 实现 Agent 状态管理
- [x] 12.3 实现 Agent 工具注册机制
- [x] 12.4 实现 Agent 会话管理

## 13. MCP Server 实现

- [x] 13.1 创建 MCP Server 基础结构
- [x] 13.2 实现 MCP 协议处理
- [x] 13.3 实现 MCP Tools 注册机制
- [x] 13.4 实现 `/api/mcp/tools` 工具列表 API
- [x] 13.5 实现 `/api/mcp/execute` 工具调用 API

## 14. Skills 技能系统

- [x] 14.1 设计 Skills 数据模型
- [x] 14.2 实现 Skills 注册机制
- [x] 14.3 实现 Skills 与 Agent 集成
- [x] 14.4 实现 Java Skills API 调用（如需要）

## 15. 文档体系优化

- [x] 15.1 设计 AIHub Logo 和品牌元素（使用占位符）
- [x] 15.2 简化 README.md（保留核心，链接详细文档）
- [x] 15.3 添加徽章（Star、License、Docker、Tech Stack）
- [ ] 15.4 制作演示 GIF/视频（非人类任务）
- [x] 15.5 重组 docs/ 目录结构
- [x] 15.6 创建 quickstart.md 快速开始文档
- [x] 15.7 创建 tutorial.md 场景化教程
- [x] 15.8 创建 examples/ 示例文档
- [ ] 15.9 添加 Sponsor 区（长期规划）
- [ ] 15.10 添加用户案例/名人背书（长期规划）
