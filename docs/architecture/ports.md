# AIHub 服务端口与配置说明

## 📍 服务端口规划

| 服务 | 端口 | 说明 | 配置文件 |
|------|------|------|----------|
| **前端** | 9527 | Vue 3 开发服务器 | `frontend/.env` |
| **Java 后端** | 9528 | Spring Boot API | `aihub-java/aihub-api/src/main/resources/application.yml` |
| **Python AI** | 9529 | FastAPI AI 服务 | `aihub-python/.env` |

## 🚀 快速启动

```bash
# 启动所有服务
./startup.sh

# 查看服务状态
./startup.sh status

# 停止所有服务
./startup.sh stop

# 单独启动某个服务
./startup.sh java    # 只启动 Java 后端
./startup.sh python  # 只启动 Python AI
./startup.sh frontend # 只启动前端
```

## ⚙️ 配置方案

### 前端 (Vue 3 + Vite)

**配置文件**: `frontend/.env`

```bash
# 服务端口
VITE_PORT=9527

# Java 后端 API 地址
VITE_API_BASE_URL=http://127.0.0.1:9528

# Python AI 服务地址
VITE_PYTHON_API_BASE_URL=http://127.0.0.1:9529
```

### Java 后端 (Spring Boot)

**配置文件**: `aihub-java/aihub-api/src/main/resources/application.yml`

```yaml
server:
  port: 9528
```

**多环境配置**:
- `application-dev.yml` - 开发环境（连接 Docker 数据库）
- `application-prod.yml` - 生产环境
- `application-local.yml` - 本地个性化配置（不提交到 git）

### Python AI (FastAPI)

**配置文件**: `aihub-python/.env`

```bash
# 服务端口
AIHUB_PYTHON_PORT=9529

# Java 后端地址（用于内部通信）
AIHUB_JAVA_API_URL=http://127.0.0.1:9528

# 数据库配置
AIHUB_PYTHON_DB_HOST=127.0.0.1
AIHUB_PYTHON_DB_PORT=3306
AIHUB_PYTHON_DB_NAME=aihub
AIHUB_PYTHON_DB_USER=aihub
AIHUB_PYTHON_DB_PASSWORD=aihub123456

# Redis 配置
AIHUB_PYTHON_REDIS_HOST=127.0.0.1
AIHUB_PYTHON_REDIS_PORT=6379
AIHUB_PYTHON_REDIS_PASSWORD=aihub123456

# JWT 配置
AIHUB_PYTHON_JWT_SECRET=your-secret-key
AIHUB_PYTHON_JWT_ALGORITHM=HS512
```

## 🔧 配置优先级

```
命令行参数 > 环境变量 > 配置文件 > 默认值
```

### Java 配置优先级示例

```bash
# 最高优先级：命令行参数
java -jar app.jar --server.port=9999

# 环境变量覆盖
export SERVER_PORT=9999
java -jar app.jar

# 配置文件中的值
# application.yml: server.port=9528
```

## 📁 配置文件目录结构

```
AIHub/
├── frontend/
│   ├── .env                    # 环境变量（端口、API 地址）
│   ├── vite.config.ts          # Vite 配置
│   └── public/
│       └── platform-config.json # 动态配置（UI 主题等）
│
├── aihub-java/
│   └── aihub-api/src/main/resources/
│       ├── application.yml          # 主配置
│       ├── application-dev.yml      # 开发环境
│       ├── application-prod.yml     # 生产环境
│       └── application-local.yml    # 本地配置（.gitignore）
│
├── aihub-python/
│   ├── .env                    # 环境变量（端口、数据库、JWT）
│   └── src/aihub/config.py    # Pydantic 配置类
│
└── startup.sh                 # 统一启动脚本
```

## 🐳 Docker 部署配置

使用 Docker Compose 部署时，通过环境变量覆盖配置：

```yaml
services:
  aihub-api:
    ports:
      - "9528:9528"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}

  aihub-python:
    ports:
      - "9529:9529"
    environment:
      - AIHUB_PYTHON_DB_HOST=mysql
      - AIHUB_PYTHON_DB_PASSWORD=${DB_PASSWORD}
```

## 🔐 安全建议

1. **敏感配置**（密码、JWT Secret）不提交到代码库
2. 使用环境变量或 Docker Secrets 注入敏感信息
3. 生产环境使用 `.env.production` 或外部配置中心

## 📝 修改端口步骤

如需修改端口，请同步修改以下文件：

1. `frontend/.env` → `VITE_PORT` 和 API 地址
2. `aihub-java/.../application.yml` → `server.port`
3. `aihub-python/.env` → `AIHUB_PYTHON_PORT`
4. `startup.sh` → 顶部端口变量

## 🆘 常见问题

### Q: 端口被占用怎么办？

```bash
# 查看端口占用
lsof -i:9528

# 停止占用进程
kill -9 <PID>
```

### Q: 如何确认服务是否正常运行？

```bash
./startup.sh status
```

或手动检查：

```bash
curl http://localhost:9527    # 前端
curl http://localhost:9528    # Java API
curl http://localhost:9529/health  # Python AI
```

## 📚 相关文档

- [配置管理最佳实践](../docs/configuration-management-best-practices.md)
- [开发环境搭建](./getting-started/development.md)
- [Docker 部署](./deployment/docker.md)
