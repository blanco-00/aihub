# 数据库配置说明

> 本文档说明如何安全地配置数据库连接信息。

## 📚 相关文档

- [后端开发指南](./guide.md) - 返回后端文档总览
- [快速开始指南](./quick-start.md) - 快速初始化项目
- [项目主文档](../../README.md) - 返回项目文档入口

## 安全配置指南

为了安全，**永远不要**将包含真实密码的配置文件提交到 Git。

## 配置方式

### 方式一：页面引导配置（推荐，最简单）

**重要**: 系统提供了页面引导的数据库配置功能，无需手动编辑配置文件，更加安全和便捷。

#### 配置步骤

1. **启动后端服务**（即使数据库未配置，后端也能启动）：
   ```bash
   cd backend/aihub-api
   mvn spring-boot:run
   ```

2. **启动前端服务**：
   ```bash
   cd frontend
   pnpm dev
   ```

3. **访问数据库配置页面**：
   - 打开浏览器访问 `http://localhost:3000`
   - 系统会自动检测数据库配置状态
   - 如果未配置，会自动跳转到 `/setup` 页面

4. **填写数据库信息**：
   - 数据库主机：`localhost` 或 IP 地址
   - 数据库端口：`3306`（默认）
   - 数据库名称：`aihub`
   - 用户名：`root`（或你的 MySQL 用户名）
   - 密码：你的 MySQL 密码

5. **测试连接**：
   - 点击"测试连接"按钮
   - 系统会验证连接信息是否正确
   - 如果连接成功，会显示连接耗时和数据库是否存在

6. **保存配置**：
   - 连接测试成功后，点击"保存配置"按钮
   - 配置会同时保存到以下两个文件：
     - `application-local.yml` - Spring Boot 会自动加载（推荐使用）
     - `.env` - 环境变量文件（可用于环境变量方式启动）
   - 系统会自动激活 `local` profile

7. **重启后端服务**：
   - 保存配置后，需要重启后端服务才能生效
   - 重启后，后端会使用新的数据库配置

**配置说明**：
- 配置信息会同时保存到两个文件：
  - `backend/aihub-api/src/main/resources/application-local.yml` - Spring Boot 自动加载
  - `backend/aihub-api/.env` - 环境变量文件，可用于 `export $(cat .env | xargs) && mvn spring-boot:run`
- 这两个文件都在 `.gitignore` 中，不会被提交到 Git
- 支持空密码（如果 MySQL 用户没有密码）

### 方式二：使用环境变量

**注意**：如果已经通过页面引导配置（方式一），`.env` 文件会自动生成，无需手动创建。

1. **创建环境变量文件**（在 `backend/aihub-api/` 目录下）：

   ```bash
   cd backend/aihub-api
   # 如果已通过页面配置，.env 文件已自动生成，可直接使用
   # 否则手动创建 .env 文件
   ```

2. **编辑 `.env` 文件**，填写实际值（如果文件不存在或需要修改）：

   ```bash
   DB_HOST=localhost
   DB_PORT=3306
   DB_NAME=aihub
   DB_USERNAME=root
   DB_PASSWORD=your_actual_password
   ```

3. **启动应用**：

   ```bash
   # Linux/macOS
   export $(cat .env | xargs) && mvn spring-boot:run
   
   # 或使用 dotenv（需要安装 dotenv-cli）
   dotenv -f .env -- mvn spring-boot:run
   ```

**说明**：
- 如果使用页面引导配置（方式一），`.env` 文件会在保存配置时自动生成
- 如果手动创建 `.env` 文件，需要确保格式正确
- Spring Boot 不会自动加载 `.env` 文件，需要手动通过环境变量方式加载

### 方式三：使用 IDE 环境变量

在 IntelliJ IDEA 的 Run Configuration 中设置环境变量：

1. 打开 Run/Debug Configurations
2. 在 Environment variables 中添加：
   ```
   DB_HOST=localhost
   DB_PORT=3306
   DB_NAME=aihub
   DB_USERNAME=root
   DB_PASSWORD=your_password
   ```

### 方式四：使用外部配置文件（仅开发环境）

创建 `application-local.yml`（此文件不会被提交到 Git）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aihub?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_here
```

然后在 `application.yml` 中激活本地配置：

```yaml
spring:
  profiles:
    active: local
```

## 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_HOST` | 数据库主机地址 | `localhost` |
| `DB_PORT` | 数据库端口 | `3306` |
| `DB_NAME` | 数据库名称 | `aihub` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | 无（必须设置） |

### 配置文件位置

- **主配置文件**: `backend/aihub-api/src/main/resources/application.yml`
- **开发环境配置**: `backend/aihub-api/src/main/resources/application-dev.yml`
- **生产环境配置**: `backend/aihub-api/src/main/resources/application-prod.yml`
- **本地配置**（不提交）: `backend/aihub-api/src/main/resources/application-local.yml` - 页面配置时自动生成
- **环境变量文件**（不提交）: `backend/aihub-api/.env` - 页面配置时自动生成

## 注意事项

- ⚠️ **`.env` 文件已在 `.gitignore` 中，不会被提交**
- ⚠️ **`application-local.yml` 已在 `.gitignore` 中，不会被提交**
- ⚠️ **永远不要将包含真实密码的配置文件提交到 Git**
- ⚠️ **生产环境建议使用配置中心（如 Nacos、Consul）或 Kubernetes Secrets**

## 验证配置

配置完成后，可以通过以下方式验证：

1. **检查环境变量是否加载**：
   ```bash
   echo $DB_PASSWORD
   ```

2. **启动应用，查看日志**：
   - 如果配置正确，应用会正常启动
   - 如果配置错误，会看到数据库连接错误

3. **测试数据库连接**：
   ```bash
   mysql -h $DB_HOST -P $DB_PORT -u $DB_USERNAME -p$DB_PASSWORD $DB_NAME -e "SELECT 1;"
   ```

## 常见问题

### Q: 环境变量没有生效怎么办？

A: 检查以下几点：
1. 环境变量是否正确设置（使用 `echo $DB_PASSWORD` 检查）
2. 启动命令是否正确加载了环境变量
3. 如果使用 IDE，检查 Run Configuration 中的环境变量设置

### Q: 如何在不同环境使用不同配置？

A: 使用 Spring Profile：
1. 创建不同环境的配置文件（`application-dev.yml`, `application-prod.yml`）
2. 在启动时指定 profile：
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

### Q: 生产环境如何配置？

A: 推荐方式：
1. **使用环境变量**：在服务器上设置环境变量
2. **使用配置中心**：如 Nacos、Consul、Spring Cloud Config
3. **使用 Kubernetes Secrets**：如果使用 K8s 部署
4. **使用外部配置文件**：通过启动参数指定配置文件位置

## 相关文档

- [快速开始指南](./quick-start.md) - 查看完整的初始化流程
- [后端开发指南](./guide.md) - 查看后端开发文档
- [项目主文档](../../README.md) - 返回项目文档入口
