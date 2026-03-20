# 快速开始

本指南将帮助你快速启动 AIHub。

## 前置要求

- Docker 20.10+
- Docker Compose 2.0+

## 一键启动

```bash
# 1. 克隆项目
git clone https://github.com/aihub/aihub.git
cd aihub

# 2. 启动所有服务
cd docker
docker compose up -d

# 3. 访问系统
# 前端: http://localhost:3000
# 后端: http://localhost:8080
```

首次启动会自动初始化数据库。

## 默认账号

- 用户名: `admin`
- 密码: `admin123`

## 验证服务

```bash
# 检查服务状态
docker compose ps

# 查看日志
docker compose logs -f
```

## 创建第一个 Agent

1. 登录系统
2. 点击「创建 Agent」
3. 选择「通用模板」
4. 选择模型（如 GPT-3.5）
5. 选择 Prompt 模板
6. 点击「创建」

## 下一步

- [教程](tutorial.md) - 学习创建不同场景的 Agent
- [部署指南](deployment/docker-compose.md) - 生产环境部署
- [开发指南](../development/) - 本地开发环境
