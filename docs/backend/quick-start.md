# 快速开始指南

> 本文档说明如何快速初始化AIHub项目，包括环境准备、数据库创建、配置修改、应用启动和系统初始化。

## 📚 相关文档

- [后端开发指南](./guide.md) - 返回后端文档总览
- [数据库配置说明](./config.md) - 数据库连接配置和安全指南
- [系统初始化文档](./initialization.md) - 详细的初始化说明
- [数据库设计文档](./database.md) - 查看数据库设计

## 前置要求

在开始之前，请确保已安装以下软件：

### 后端环境
- **JDK 17+** - Java 开发环境
- **Maven 3.6+** - 项目构建工具
- **MySQL 8.0+** - 数据库服务器

### 前端环境
- **Node.js 20.19.0+ 或 22.13.0+** - Node.js 运行环境（LTS版本）
- **pnpm >= 9** - 包管理工具（推荐使用 pnpm，项目已配置）

### 其他工具
- **Git** - 版本控制工具（可选）

### 安装 pnpm（如果未安装）

```bash
# 使用 npm 安装
npm install -g pnpm

# 或使用 Homebrew (macOS)
brew install pnpm

# 验证安装
pnpm --version
```

## 快速开始步骤

### 1. 启动 Docker 开发环境（推荐）

**重要**: 使用 Docker Compose 启动 MySQL 和 Redis，无需本地安装。

```bash
# 进入 docker 目录
cd docker

# 启动基础设施（MySQL + Redis）
docker compose -f docker-compose.dev.yml up -d

# 查看服务状态
docker compose -f docker-compose.dev.yml ps
```

**数据库连接信息**（已配置在 `application-dev.yml` 中）：
- 主机: `localhost`
- 端口: `3306`
- 数据库名: `aihub`
- 用户名: `aihub`
- 密码: `aihub123456`

**详细文档**: [Docker Compose 部署指南 - 本地开发](../deployment/docker-compose.md#场景二本地开发推荐)

### 2. 创建数据库（如果使用本地 MySQL）

**重要**: 如果使用本地 MySQL 而非 Docker，需要手动创建数据库。

#### 方式一：使用 MySQL 命令行（推荐）

```bash
# 登录 MySQL
mysql -u root -p

# 在 MySQL 命令行中执行
CREATE DATABASE IF NOT EXISTS aihub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 验证数据库是否创建成功
SHOW DATABASES LIKE 'aihub';

# 退出 MySQL
EXIT;
```

#### 方式二：直接执行 SQL 命令

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS aihub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

#### 方式三：使用全量初始化脚本（会自动创建数据库和表）

```bash
mysql -u root -p < docs/sql/init/init_v1.0.0.sql
```

**注意**: 
- 数据库名称必须是 `aihub`（或修改配置文件中的数据库名称）
- 字符集必须使用 `utf8mb4`，排序规则使用 `utf8mb4_unicode_ci`
- 确保 MySQL 用户有创建数据库的权限

### 3. 安装前端依赖

```bash
cd frontend
pnpm install
```

**注意**: 
- 首次安装可能需要一些时间
- 如果安装失败，检查网络连接或配置 npm/pnpm 镜像源

### 4. 启动前后端服务

#### 启动后端服务

**重要**: 后端启动时会自动连接数据库（配置在 `application-dev.yml` 中）。

```bash
cd backend/aihub-api
mvn spring-boot:run
```

**启动成功标志**：
- 看到类似 `Started AihubApplication in X.XXX seconds` 的日志
- 应用监听在 `http://localhost:8080`
- 如果数据库连接成功，会看到数据库连接日志

#### 启动前端服务（新终端窗口）

```bash
cd frontend
pnpm dev
```

**启动成功标志**：
- 看到类似 `Local: http://localhost:3000/` 的日志
- 前端服务监听在 `http://localhost:3000`

**注意**: 
- 前端默认端口是 `3000`（可在 `vite.config.ts` 中修改）
- 前端会自动代理 `/api` 请求到后端 `http://localhost:8080`

### 5. 系统初始化

**重要**: 系统采用统一的页面引导初始化方案，所有环境（开发、测试、生产）使用相同的流程。

> **详细步骤请参考** [系统初始化文档](./initialization.md)

**简要流程**：
1. 访问 `http://localhost:3000`，系统会自动跳转到 `/init` 页面
2. 如果数据库表未初始化，点击"初始化数据库表结构"按钮
3. 填写超级管理员信息并创建，完成后自动跳转到首页

## 完整流程示例

### 终端 1：启动 Docker 开发环境

```bash
# 进入 docker 目录
cd docker

# 启动基础设施（MySQL + Redis）
docker compose -f docker-compose.dev.yml up -d
```

### 终端 2：后端服务

```bash
# 启动后端服务
cd backend/aihub-api
mvn spring-boot:run
```

### 终端 3：前端服务

```bash
# 1. 安装前端依赖（首次需要）
cd frontend
pnpm install

# 2. 启动前端服务
pnpm dev
```

### 浏览器操作

1. **访问系统**：打开浏览器访问 `http://localhost:3000`
2. **系统初始化**：按照页面引导完成初始化（详细步骤请参考 [系统初始化文档](./initialization.md)）

## 验证安装

完成以上步骤后，可以通过以下方式验证安装是否成功：

1. **检查后端服务**：
   - 访问 `http://localhost:8080/api/init/status`
   - 应该返回 `{"code":200,"data":true,"message":"操作成功"}`（如果已初始化）

2. **检查数据库表**：
   ```bash
   mysql -u root -p aihub -e "SHOW TABLES;"
   ```
   - 应该看到 `user`、`model_config` 等表

3. **检查前端页面**：
   - 访问 `http://localhost:3000`
   - 应该能正常显示页面，无错误提示

## 常见问题

### Q: 数据库连接失败怎么办？

A: 详细说明请参考 [数据库配置说明 - 常见问题](./config.md#常见问题)

### Q: 如何修改数据库连接配置？

A: 详细说明请参考 [数据库配置说明](./config.md)

### Q: 初始化相关问题

A: 详细说明请参考 [系统初始化文档 - 常见问题](./initialization.md#常见问题)

### Q: 如何修改服务器端口？

A: 

**后端端口**：编辑 `backend/aihub-api/src/main/resources/application.yml`：

```yaml
server:
  port: 8080  # 修改为你想要的端口
```

**前端端口**：编辑 `frontend/vite.config.ts`：

```typescript
server: {
  port: 3000,  // 修改为你想要的端口
  // ...
}
```

**注意**：如果修改了后端端口，需要同步修改前端的代理配置（`vite.config.ts` 中的 `proxy.target`）。

修改后重启应用即可。

### Q: 如何查看应用日志？

A: 
- **控制台输出**：应用启动时会在控制台输出日志
- **日志文件**：如果配置了日志文件，查看日志文件位置
- **日志级别**：在 `application.yml` 中配置 `logging.level` 可以调整日志级别

## 快速检查清单

在开始之前，请确认：

- [ ] 已安装 JDK 17+、Maven 3.6+、Docker Desktop
- [ ] 已安装 Node.js 20.19.0+（或 22.13.0+）和 pnpm >= 9
- [ ] Docker 开发环境已启动（MySQL + Redis）
- [ ] 后端服务已启动（`http://localhost:8080`）
- [ ] 前端服务已启动（`http://localhost:3000`）
- [ ] 已初始化数据库表结构
- [ ] 已创建超级管理员账号

## 下一步

初始化完成后，可以：

1. 配置模型（添加 OpenAI、Claude 等模型配置）
2. 创建其他用户和角色
3. 配置系统参数
4. 开始使用系统功能

## 相关文档

- [系统初始化文档](./initialization.md) - 详细的初始化说明和代码示例
- [数据库设计文档](./database.md) - 查看数据库表结构
- [SQL 脚本管理](../sql/guide.md) - SQL脚本管理规范
