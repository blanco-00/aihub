# AIHub 后端项目

> AI基础设施平台后端代码

## 📚 相关文档

- [项目主文档](../../README.md) - 项目文档入口
- [后端开发指南](../../docs/backend/guide.md) - 后端开发文档
- [系统初始化文档](../../docs/backend/initialization.md) - 系统初始化说明
- [数据库设计文档](../../docs/backend/database.md) - 数据库设计

## 🚀 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库初始化

执行数据库初始化脚本：

```bash
mysql -u root -p < ../../docs/sql/init/init_v1.0.0.sql
```

### 3. 配置数据库连接

编辑 `aihub-api/src/main/resources/application-dev.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aihub
    username: root
    password: your_password
```

### 4. 启动应用

```bash
cd aihub-api
mvn spring-boot:run
```

或者使用 IDE 直接运行 `AihubApplication.java`

### 5. 访问系统

- 应用地址: http://localhost:8080
- 如果系统未初始化，会自动跳转到 `/init` 页面
- 在初始化页面创建第一个超级管理员

## 📁 项目结构

```
backend/
├── aihub-api/              # 主应用模块
│   ├── src/main/java/com/aihub/
│   │   ├── AihubApplication.java    # 启动类
│   │   ├── controller/              # 控制器
│   │   ├── service/                 # 服务层
│   │   ├── mapper/                  # 数据访问层
│   │   ├── entity/                  # 实体类
│   │   ├── dto/                     # 数据传输对象
│   │   ├── enums/                   # 枚举类
│   │   ├── exception/               # 异常类
│   │   ├── config/                  # 配置类
│   │   └── interceptor/             # 拦截器
│   └── src/main/resources/
│       ├── application.yml          # 主配置文件
│       ├── application-dev.yml     # 开发环境配置
│       ├── application-prod.yml     # 生产环境配置
│       ├── mapper/                  # MyBatis XML
│       └── db/migration/            # Flyway 迁移脚本（可选）
└── pom.xml                          # 父POM
```

## 🔧 技术栈

- Spring Boot 3.2.0
- MyBatis Plus 3.5.5
- MySQL 8.0+
- Lombok
- Spring Security (密码加密)

## 📝 开发规范

请参考项目根目录下的 `.cursor/rules/` 目录中的开发规范：
- Java 代码规范
- 日志规范
- 文档规范

## 🔗 相关链接

- [系统初始化文档](../../docs/backend/initialization.md) - 查看初始化流程
- [SQL 脚本管理](../../docs/sql/guide.md) - SQL脚本管理规范
