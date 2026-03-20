# Docker Compose 部署指南

> 本文档说明如何使用 Docker Compose 一键部署 AIHub 项目。

## 📚 相关文档

- [快速开始指南](../backend/quick-start.md) - 传统部署方式
- [系统初始化文档](../backend/initialization.md) - 系统初始化说明（包含初始化方案详解）

## 🎯 方案概述

Docker Compose 方案提供了一键启动所有服务的能力，包括：
- MySQL 8.0 数据库
- Redis 7 缓存
- 后端 API 服务
- 前端 Nginx 服务

## 📁 Docker 目录结构

Docker 相关文件都位于 `docker/` 目录下：

- **`docker-compose.yml`** - 生产环境 Docker Compose 配置（所有服务容器化）
- **`docker-compose.dev.yml`** - 开发环境 Docker Compose 配置（仅基础设施）
- **`env.example`** - 环境变量配置模板
- **`backend/Dockerfile`** - 后端服务 Docker 镜像构建文件
- **`frontend/Dockerfile`** - 前端服务 Docker 镜像构建文件

> **注意**：`frontend/` 目录下也有一个 Dockerfile（用于兼容性），但 Docker Compose 使用的是 `docker/frontend/Dockerfile`。

## 前置要求

- **Docker**: 20.10+
- **Docker Compose**: 2.0+

### 安装 Docker

#### macOS
```bash
# 使用 Homebrew
brew install --cask docker

# 或下载 Docker Desktop
# https://www.docker.com/products/docker-desktop
```

#### Linux
```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

## 快速开始

> **重要提示**：所有 Docker Compose 命令都需要在 `docker/` 目录下执行。

### 1. 配置环境变量

```bash
# 进入 docker 目录
cd docker

# 复制环境变量模板
cp env.example .env

# 编辑环境变量（生产环境必须修改密码和密钥）
vim .env
```

**重要配置项**：
- `MYSQL_ROOT_PASSWORD`: MySQL root 密码（生产环境必须修改）
- `MYSQL_PASSWORD`: MySQL 用户密码（生产环境必须修改）
- `JWT_SECRET`: JWT 密钥（生产环境必须修改为至少 32 位随机字符串）
- `REDIS_PASSWORD`: Redis 密码（生产环境必须修改）

### 2. 启动所有服务

```bash
# 进入 docker 目录
cd docker

# 构建并启动所有服务
docker compose up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f
```

### 3. 等待服务就绪

首次启动需要：
1. 下载 Docker 镜像（约 5-10 分钟，取决于网络）
2. 构建应用镜像（约 3-5 分钟）
3. 数据库初始化（约 1-2 分钟）

**检查服务状态**：
```bash
# 查看所有服务健康状态
docker compose ps

# 查看特定服务日志
docker compose logs backend
docker compose logs mysql
```

### 4. 访问系统

1. **访问前端**: http://localhost:3000
2. **访问后端 API**: http://localhost:8080

### 5. 系统初始化

系统启动后，访问前端会自动跳转到初始化页面，按照页面引导完成：
1. 数据库配置（Docker 环境已自动配置，可直接跳过或验证）
2. 数据库表结构初始化
3. 创建超级管理员

## 使用场景

### 场景一：生产环境部署（推荐）

使用 `docker-compose.yml`：

```bash
# 1. 进入 docker 目录
cd docker

# 2. 配置环境变量
cp env.example .env
vim .env  # 修改密码和密钥

# 3. 启动服务
docker compose up -d

# 4. 查看日志
docker compose logs -f
```

**特点**：
- 所有服务都在容器中运行
- 适合生产环境
- 环境一致性最好

### 场景二：本地开发（推荐）

使用 `docker-compose.dev.yml` 仅启动基础设施，应用本地运行：

```bash
# 1. 进入 docker 目录
cd docker

# 2. 启动基础设施（MySQL + Redis）
docker compose -f docker-compose.dev.yml up -d

# 3. 检查基础设施状态
docker compose -f docker-compose.dev.yml ps

# 4. 本地运行后端（支持热重载）
cd ../backend/aihub-api
mvn spring-boot:run

# 5. 本地运行前端（支持热重载，新终端窗口）
cd ../../frontend
pnpm install  # 首次需要安装依赖
pnpm dev
```

**数据库连接信息**（已配置在 `application-dev.yml` 中）：
- 主机: `localhost`
- 端口: `3306`
- 数据库名: `aihub`
- 用户名: `aihub`（或 `root`）
- 密码: `aihub123456`（或 `docker/.env` 中配置的密码）

> **注意**：系统启动时会自动连接数据库，无需手动配置。详细说明请参考 [数据库配置说明](../backend/config.md)

**特点**：
- ✅ 基础设施容器化，无需本地安装 MySQL/Redis
- ✅ 应用本地运行，支持热重载，开发效率高
- ✅ 适合日常开发
- ✅ 数据持久化，容器重启数据不丢失

**停止基础设施**：
```bash
cd docker
docker compose -f docker-compose.dev.yml stop
# 或完全停止并删除容器
docker compose -f docker-compose.dev.yml down
```

### 场景三：快速体验

使用 `docker-compose.yml` 一键启动：

```bash
# 1. 进入 docker 目录
cd docker

# 2. 使用默认配置快速启动
docker compose up -d

# 3. 访问系统
open http://localhost:3000
```

**特点**：
- 零配置启动
- 适合快速体验和演示
- 不适合生产环境（使用默认密码）

## 系统初始化

> **详细说明请参考** [系统初始化文档](../backend/initialization.md)

Docker Compose 部署采用统一的页面引导初始化方案，与本地开发环境保持一致。

**简要说明**：
- MySQL 容器启动时会自动创建数据库
- 表结构和超级管理员需要通过页面初始化（访问 `http://localhost:3000` 会自动跳转到 `/init` 页面）

## 常用命令

### 服务管理

> **注意**：以下命令需要在 `docker/` 目录下执行。

```bash
# 启动服务
docker compose up -d

# 停止服务
docker compose stop

# 停止并删除容器
docker compose down

# 停止并删除容器和数据卷（⚠️ 会删除数据）
docker compose down -v

# 重启服务
docker compose restart

# 重启特定服务
docker compose restart backend
```

### 日志查看

```bash
# 查看所有服务日志
docker compose logs -f

# 查看特定服务日志
docker compose logs -f backend
docker compose logs -f mysql

# 查看最近 100 行日志
docker compose logs --tail=100 backend
```

### 数据管理

```bash
# 备份数据库
docker compose exec mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} aihub > backup.sql

# 恢复数据库
docker compose exec -T mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} aihub < backup.sql

# 进入 MySQL 命令行
docker compose exec mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} aihub

# 进入 Redis 命令行
docker compose exec redis redis-cli -a ${REDIS_PASSWORD}
```

### 镜像管理

```bash
# 重新构建镜像
docker compose build

# 重新构建并启动
docker compose up -d --build
```

# 清理未使用的镜像
docker image prune -a
```

## 数据持久化

所有数据都存储在 Docker volumes 中：

```bash
# 查看 volumes
docker volume ls | grep aihub

# 备份 volume
docker run --rm -v aihub_mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_backup.tar.gz /data

# 恢复 volume
docker run --rm -v aihub_mysql_data:/data -v $(pwd):/backup alpine tar xzf /backup/mysql_backup.tar.gz -C /
```

## 故障排查

### 服务无法启动

```bash
# 1. 检查服务状态
docker compose ps

# 2. 查看错误日志
docker compose logs backend
docker compose logs mysql

# 3. 检查端口占用
lsof -i :8080
lsof -i :3306
lsof -i :3000

# 4. 检查 Docker 资源
docker system df
docker system prune  # 清理未使用的资源
```

### 数据库连接失败

```bash
# 1. 检查 MySQL 容器状态
docker compose ps mysql

# 2. 检查 MySQL 日志
docker compose logs mysql

# 3. 测试数据库连接
docker compose exec mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "SELECT 1;"
```

### 前端无法访问后端

```bash
# 1. 检查后端服务状态
docker compose ps backend
curl http://localhost:8080/actuator/health

# 2. 检查网络连接
docker compose exec frontend ping backend

# 3. 检查环境变量
docker compose exec backend env | grep DB_
```

## 生产环境建议

### 1. 安全配置

- ✅ 修改所有默认密码
- ✅ 使用强密码（至少 16 位，包含大小写字母、数字、特殊字符）
- ✅ 修改 JWT_SECRET（至少 32 位随机字符串）
- ✅ 使用 HTTPS（配置 Nginx 反向代理）
- ✅ 限制数据库和 Redis 端口仅内网访问

### 2. 性能优化

```yaml
# 增加 MySQL 连接池
environment:
  SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: 20

# 增加 Redis 内存限制
command: redis-server --maxmemory 512mb --maxmemory-policy allkeys-lru
```

### 3. 监控和日志

- 配置日志收集（如 ELK、Loki）
- 配置监控告警（如 Prometheus + Grafana）
- 定期备份数据库

### 4. 高可用部署

- 使用 Docker Swarm 或 Kubernetes
- 配置数据库主从复制
- 配置 Redis 集群

## 常见问题

### Q: 如何修改数据库密码？

A: 
1. 修改 `.env` 文件中的 `MYSQL_ROOT_PASSWORD` 和 `MYSQL_PASSWORD`
2. 修改后端环境变量 `DB_PASSWORD`
3. 重启服务：`docker compose restart`

### Q: 如何升级应用？

A:
```bash
# 1. 拉取最新代码
git pull

# 2. 重新构建镜像
docker compose build

# 3. 重启服务
docker compose up -d
```

### Q: 数据会丢失吗？

A: 不会。数据存储在 Docker volumes 中，即使删除容器，数据也不会丢失。只有执行 `docker compose down -v` 才会删除数据。

### Q: 如何查看应用日志？

A:
```bash
# 查看所有日志
docker compose logs -f

# 查看特定服务日志
docker compose logs -f backend
```

### Q: 如何进入容器调试？

A:
```bash
# 进入后端容器
docker compose exec backend sh

# 进入 MySQL 容器
docker compose exec mysql bash

# 进入前端容器
docker compose exec frontend sh
```

## 参考资源

- [Docker 官方文档](https://docs.docker.com/)
- [Docker Compose 官方文档](https://docs.docker.com/compose/)
- [Spring Boot Docker 最佳实践](https://spring.io/guides/gs/spring-boot-docker/)
