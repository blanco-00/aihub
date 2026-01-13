# 数据库配置说明

> 本文档说明如何配置数据库连接信息。

## 📚 相关文档

- [后端开发指南](./guide.md) - 返回后端文档总览
- [快速开始指南](./quick-start.md) - 快速初始化项目
- [Docker Compose 部署指南](../deployment/docker-compose.md) - Docker 部署相关配置
- [项目主文档](../../README.md) - 返回项目文档入口

## 默认配置

**开发环境**：系统已配置好 Docker 开发环境的数据库连接（在 `application-dev.yml` 中）：
- 主机: `localhost`
- 端口: `3306`
- 数据库名: `aihub`
- 用户名: `aihub`
- 密码: `aihub123456`

**启动 Docker 开发环境**：
```bash
cd docker
docker compose -f docker-compose.dev.yml up -d
```

详细说明请参考 [Docker Compose 部署指南 - 本地开发](../deployment/docker-compose.md#场景二本地开发推荐)

## 配置方式

### 方式一：修改 application-dev.yml（开发环境通用配置）

编辑 `backend/aihub-api/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aihub?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: aihub
    password: aihub123456
```

**注意**：此文件会被提交到代码库，包含的是通用配置。

### 方式二：使用环境变量（推荐用于个性化配置）

在启动命令中设置环境变量：

```bash
DB_HOST=localhost DB_USERNAME=root DB_PASSWORD=your_password mvn spring-boot:run
```

或创建 `.env` 文件（不提交）：

```bash
DB_HOST=localhost
DB_PORT=3306
DB_NAME=aihub
DB_USERNAME=root
DB_PASSWORD=your_actual_password
```

然后启动：

```bash
# Linux/macOS
export $(cat .env | xargs) && mvn spring-boot:run

# 或使用 dotenv（需要安装 dotenv-cli）
dotenv -f .env -- mvn spring-boot:run
```

### 方式三：使用 application-local.yml（个人本地配置）

创建 `application-local.yml`（此文件在 `.gitignore` 中，不会被提交）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aihub?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password_here
```

然后在 `application.yml` 中激活本地配置：

```yaml
spring:
  profiles:
    active: local,dev  # 同时激活 local 和 dev profile
```

### 方式四：使用 IDE 环境变量

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

**注意**：方式三和方式四都使用 `application-local.yml`，区别在于：
- **方式三**：在 `application.yml` 中激活 `local` profile
- **方式四**：通过 IDE 环境变量或启动参数激活 `local` profile

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
- **开发环境配置**: `backend/aihub-api/src/main/resources/application-dev.yml`（提交到代码库）
- **生产环境配置**: `backend/aihub-api/src/main/resources/application-prod.yml`
- **本地配置**（不提交）: `backend/aihub-api/src/main/resources/application-local.yml` - 用于个性化配置
- **环境变量文件**（不提交）: `backend/aihub-api/.env` - 用于环境变量方式配置

## 注意事项

- ⚠️ **`.env` 文件已在 `.gitignore` 中，不会被提交**
- ⚠️ **`application-local.yml` 已在 `.gitignore` 中，不会被提交**
- ⚠️ **`application-dev.yml` 包含通用配置，会被提交到代码库**
- ⚠️ **如果需要个性化配置，使用环境变量或 `application-local.yml`**
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

### Q: 数据库连接失败怎么办？

A: 检查以下几点：
1. **Docker 容器是否运行**（如果使用 Docker 开发环境）：
   ```bash
   cd docker
   docker compose -f docker-compose.dev.yml ps
   ```
   - 如果容器未运行，执行 `docker compose -f docker-compose.dev.yml up -d`

2. **数据库配置是否正确**：
   - 检查 `application-dev.yml` 中的数据库配置
   - 确认主机、端口、数据库名、用户名、密码是否正确

3. **查看应用日志**：
   - 查看控制台输出的错误信息
   - 常见错误：
     - `Unknown database 'aihub'` - 数据库不存在，检查 Docker 容器日志
     - `Access denied` - 用户名或密码错误，检查配置
     - `Communications link failure` - 无法连接到 MySQL 服务器，检查容器是否运行

4. **检查 Docker 容器日志**（如果使用 Docker）：
   ```bash
   cd docker
   docker compose -f docker-compose.dev.yml logs mysql
   ```

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
