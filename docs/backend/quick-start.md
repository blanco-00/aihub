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
- **Node.js 18+** - Node.js 运行环境
- **pnpm** - 包管理工具（推荐使用 pnpm，项目已配置）

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

### 1. 创建数据库

**重要**: 应用无法通过页面创建数据库，需要手动创建。

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

### 2. 安装前端依赖

在配置数据库之前，需要先安装前端依赖：

```bash
cd frontend
pnpm install
```

**注意**: 
- 首次安装可能需要一些时间
- 如果安装失败，检查网络连接或配置 npm/pnpm 镜像源

### 3. 启动前后端服务

#### 启动后端服务

**重要**: 即使数据库未配置，后端也能启动（用于页面引导配置）。

```bash
cd backend/aihub-api
mvn spring-boot:run
```

**启动成功标志**：
- 看到类似 `Started AihubApplication in X.XXX seconds` 的日志
- 应用监听在 `http://localhost:8080`

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

### 4. 配置数据库连接（页面引导，推荐）

**重要**: 系统提供了页面引导的数据库配置功能，无需手动编辑配置文件，更加安全和便捷。

#### 页面引导配置步骤

1. **访问数据库配置页面**：
   - 打开浏览器访问 `http://localhost:3000`
   - 系统会自动检测数据库配置状态
   - 如果未配置，会自动跳转到 `/setup` 页面

4. **填写数据库信息**：
   - 数据库主机：`localhost` 或 IP 地址
   - 数据库端口：`3306`（默认）
   - 数据库名称：`aihub`（必须与步骤 1 中创建的数据库名称一致）
   - 用户名：`root`（或你的 MySQL 用户名）
   - 密码：你的 MySQL 密码

5. **测试连接**：
   - 点击"测试连接"按钮
   - 系统会验证连接信息是否正确
   - 如果连接成功，会显示连接耗时和数据库是否存在

6. **保存配置**：
   - 连接测试成功后，点击"保存配置"按钮
   - 配置会保存到 `application-local.yml` 文件
   - 系统会自动激活 `local` profile

7. **重启后端服务**：
   - 保存配置后，需要重启后端服务才能生效
   - 重启后，后端会使用新的数据库配置

**配置说明**：
- 配置信息保存在 `backend/aihub-api/src/main/resources/application-local.yml`
- 该文件已在 `.gitignore` 中，不会被提交到 Git
- 支持空密码（如果 MySQL 用户没有密码）

**安全提示**：
- ⚠️ **配置信息保存在本地文件，不会被提交到 Git**
- ⚠️ **生产环境建议使用环境变量或配置中心（如 Nacos、Consul）**

#### 其他配置方式（可选）

如果需要使用其他方式配置，请参考 [数据库配置说明](./config.md) 文档。

### 5. 初始化数据库表结构（通过页面）

**重要**: 数据库表结构可以通过页面初始化，无需手动执行 SQL 脚本。

1. **完成数据库配置**（参考步骤 2）

2. **重启后端服务**，确保配置生效

3. **访问初始化页面**：
   - 打开浏览器访问 `http://localhost:3000`
   - 系统会自动检测数据库状态
   - 如果数据库连接正常但表未初始化，会显示"初始化数据库表结构"按钮

4. **初始化数据库表结构**：
   - 点击"初始化数据库表结构"按钮
   - 系统会自动执行 SQL 脚本，创建所有表结构
   - 等待初始化完成（通常几秒钟）
   - 初始化完成后，会显示成功提示

**注意**: 
- 如果数据库不存在或连接失败，页面会显示错误提示
- 请先完成步骤 1（创建数据库）和步骤 2（配置数据库连接）

### 6. 创建超级管理员

在初始化页面填写超级管理员信息：

- **用户名**: 3-50个字符，不能为空，建议使用有意义的名称（如 `admin`）
- **邮箱**: 有效的邮箱地址，用于接收系统通知（如 `admin@example.com`）
- **密码**: 8-50个字符，建议包含大小写字母、数字和特殊字符
- **确认密码**: 与密码一致

点击"创建超级管理员"按钮，系统会创建第一个超级管理员账号。

**安全提示**：
- ⚠️ 请妥善保管超级管理员账号信息
- ⚠️ 建议使用强密码
- ⚠️ 不要将账号信息泄露给他人

### 7. 登录系统

创建成功后，系统会自动跳转到首页（或登录页面），使用刚才创建的超级管理员账号登录。

## 完整流程示例

### 终端 1：后端服务

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS aihub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 启动后端服务
cd backend/aihub-api
mvn spring-boot:run
```

### 终端 2：前端服务

```bash
# 1. 安装前端依赖（首次需要）
cd frontend
pnpm install

# 2. 启动前端服务
pnpm dev
```

### 浏览器操作

1. **访问系统**：打开浏览器访问 `http://localhost:3000`
2. **配置数据库**：
   - 系统会自动跳转到 `/setup` 页面
   - 填写数据库信息并测试连接
   - 保存配置后，**重启后端服务**（在终端 1 中按 `Ctrl+C` 停止，然后重新运行 `mvn spring-boot:run`）
3. **初始化数据库表结构**：
   - 重启后端后，刷新浏览器页面
   - 系统会自动跳转到 `/init` 页面
   - 点击"初始化数据库表结构"按钮
4. **创建超级管理员**：
   - 填写超级管理员信息
   - 点击"创建超级管理员"按钮
5. **登录系统**：
   - 创建成功后，系统会自动跳转到首页
   - 使用刚才创建的超级管理员账号登录
```

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

A: 检查以下几点：
1. **MySQL 服务是否启动**：
   ```bash
   # macOS/Linux
   brew services list | grep mysql
   # 或
   systemctl status mysql
   ```

2. **数据库是否已创建**：
   ```bash
   mysql -u root -p -e "SHOW DATABASES LIKE 'aihub';"
   ```

3. **配置信息是否正确**：
   - 检查 `application.yml` 中的 `url`、`username`、`password`
   - 确认数据库名称、主机地址、端口号是否正确
   - 确认用户名和密码是否正确

4. **网络连接是否正常**：
   - 如果 MySQL 运行在远程服务器，检查网络连接
   - 检查防火墙设置，确保端口 3306 可访问

5. **查看应用日志**：
   - 查看控制台输出的错误信息
   - 常见错误：
     - `Unknown database 'aihub'` - 数据库不存在，需要先创建
     - `Access denied` - 用户名或密码错误
     - `Communications link failure` - 无法连接到 MySQL 服务器

### Q: 如何修改数据库连接配置？

A: **推荐使用环境变量**（安全且灵活）：

1. **修改 `.env` 文件**（推荐）：
   ```bash
   # 编辑 backend/aihub-api/.env
   DB_HOST=your_host
   DB_PORT=3306
   DB_NAME=aihub
   DB_USERNAME=your_username
   DB_PASSWORD=your_password
   ```

2. **或在启动命令中设置环境变量**：
   ```bash
   DB_HOST=localhost DB_USERNAME=root DB_PASSWORD=your_password mvn spring-boot:run
   ```

3. **或创建 `application-local.yml`**（不推荐，仅用于开发）：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://your_host:3306/aihub?...
       username: your_username
       password: your_password
   ```

修改后需要**重启应用**才能生效。

**注意**: 不要将包含真实密码的配置文件提交到 Git！

### Q: 可以在不同环境使用不同配置吗？

A: 可以。Spring Boot 支持多环境配置：

1. 创建环境配置文件：
   - `application-dev.yml` - 开发环境
   - `application-prod.yml` - 生产环境

2. 在 `application.yml` 中指定激活的环境：
   ```yaml
   spring:
     profiles:
       active: dev  # 或 prod
   ```

3. 或启动时指定：
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

### Q: 初始化页面无法访问怎么办？

A: 检查以下几点：
1. **后端服务是否启动**：
   - 检查控制台是否有错误
   - 确认服务监听在 `http://localhost:8080`

2. **前端服务是否启动**：
   - 检查前端服务是否运行
   - 确认访问地址是否正确（默认 `http://localhost:3000`）
   - 检查终端是否有错误信息

3. **数据库连接是否正常**：
   - 查看后端日志，检查是否有数据库连接错误
   - 参考"数据库连接失败怎么办"部分

4. **查看应用日志**：
   - 查看控制台输出的错误信息
   - 检查是否有异常堆栈信息

5. **确认路由配置**：
   - 如果直接访问后端 API，确认路径是否正确
   - 前端路由是否正确配置

### Q: 页面显示"数据库不存在"怎么办？

A: 这说明数据库 `aihub` 还没有创建。请按照步骤 1 创建数据库：

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS aihub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

创建完成后，刷新页面即可。

### Q: 页面显示"数据库连接被拒绝"怎么办？

A: 检查以下几点：
1. **MySQL 服务是否启动**
2. **配置信息是否正确**：
   - 检查 `application.yml` 中的数据库配置
   - 确认用户名和密码是否正确
3. **网络连接**：
   - 如果 MySQL 运行在远程服务器，检查网络连接
   - 检查防火墙设置

### Q: 初始化数据库表结构失败怎么办？

A: 检查以下几点：
1. **数据库连接是否正常**（参考上面的问题）
2. **数据库用户权限**：
   - 确保数据库用户有 `CREATE TABLE`、`ALTER TABLE` 等权限
   - 可以执行：`GRANT ALL PRIVILEGES ON aihub.* TO 'your_user'@'localhost';`
3. **表是否已存在**：
   - 如果表已存在，初始化会跳过（使用 `CREATE TABLE IF NOT EXISTS`）
   - 如果表结构有问题，可能需要先删除表
4. **查看应用日志**：
   - 查看后端日志中的具体错误信息
   - 检查 SQL 执行是否有错误

### Q: 如何创建新的超级管理员？

A: 使用现有的超级管理员账号登录，在用户管理页面创建新用户并设置为超级管理员角色。

### Q: 忘记超级管理员密码怎么办？

A: 可以通过以下方式重置：
1. **如果有其他超级管理员**：请其他管理员在用户管理页面重置密码
2. **如果没有其他管理员**：需要直接在数据库中重置（需要数据库访问权限）：
   ```sql
   -- 注意：密码需要使用 BCrypt 加密，不能直接设置明文密码
   -- 建议通过应用的重置密码功能，或创建新的超级管理员
   ```

### Q: 为什么不能删除最后一个超级管理员？

A: 系统要求至少保留一个超级管理员，以确保系统始终有管理员可以管理。这是安全保护机制。

### Q: 可以跳过初始化页面吗？

A: 不可以。系统要求必须通过初始化页面创建第一个超级管理员，这是安全设计，确保管理员信息由用户自己设置。

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

- [ ] 已安装 JDK 17+、Maven 3.6+、MySQL 8.0+
- [ ] 已安装 Node.js 18+ 和 pnpm
- [ ] 已创建数据库 `aihub`
- [ ] 后端服务已启动（`http://localhost:8080`）
- [ ] 前端服务已启动（`http://localhost:3000`）
- [ ] 已通过页面配置数据库连接
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
