# 现代全栈项目配置管理最佳实践调研

## 📋 调研背景

### 调研目的
本文档通过分析成熟开源项目（AIHub、Pig、Sa-Token）的配置管理实践，总结现代全栈项目的配置管理最佳实践。

### 调研对象

| 项目 | 类型 | 技术栈 | 特点 |
|-----|------|--------|------|
| **AIHub** | 全栈 AI 平台 | Vue 3 + Spring Boot 3.2 + Python | 前后端分离 + 微服务架构 |
| **Pig** | 微服务企业平台 | Spring Cloud 3.5 + Vue 3 | 微服务架构 + 多服务协同 |
| **Sa-Token** | 权限框架 | Spring Boot + Redis | 轻量级配置，适合小型项目 |

---

## 🔧 前端配置方案

### 1. 环境变量 vs 配置文件

#### 方案对比

| 方式 | 优点 | 缺点 | 适用场景 |
|-----|------|------|----------|
| **环境变量** (.env) | • 构建时注入，安全性高<br>• 支持多环境<br>• 不需要修改代码 | • 需要重新构建才能生效<br>• 不能运行时修改<br>• 浏览器端可见敏感信息 | • 部署环境配置<br>• 不频繁变更的配置 |
| **配置文件** (config.json) | • 可运行时动态加载<br>• 无需重新构建<br>• 便于运维调整 | • 可能暴露敏感信息<br>• 配置与代码分离增加复杂度 | • UI 主题配置<br>• 运行时可变配置 |
| **硬编码** | • 简单直接 | • 不支持多环境<br>• 修改需要重新发布<br>• 不符合最佳实践 | ⚠️ **不推荐** |

#### AIHub 前端配置方案（推荐实践）

AIHub 采用了**混合配置方案**，结合了环境变量和动态配置文件：

```typescript
// 1. 环境变量（构建时配置）- .env
VITE_PORT=9527
VITE_API_BASE_URL=http://127.0.0.1:9528
VITE_PYTHON_API_BASE_URL=http://127.0.0.1:9529

// 2. 动态配置文件（运行时配置）- public/platform-config.json
{
  "Version": "6.3.0",
  "Title": "PureAdmin",
  "Theme": "default",
  "DarkMode": true,
  "MapConfigure": {
    "amapKey": "adc139d56406f3844c8f1cf1c6b65c41"
  }
}

// 3. Vite 配置 - vite.config.ts
const { VITE_PORT, VITE_API_BASE_URL } = wrapperEnv(loadEnv(mode, root));
export default {
  server: {
    port: VITE_PORT,
    proxy: {
      "/api": {
        target: VITE_API_BASE_URL || "http://127.0.0.1:9528",
        changeOrigin: true
      }
    }
  }
}

// 4. 动态配置加载工具 - src/config/index.ts
export const getPlatformConfig = async (app: App): Promise<undefined> => {
  return axios({
    method: "get",
    url: `${VITE_PUBLIC_PATH}platform-config.json`,
  })
    .then(({ data: config }) => {
      app.config.globalProperties.$config = Object.assign(
        app.config.globalProperties.$config, 
        config
      );
    });
};
```

**配置加载优先级**：
1. **环境变量**（最高优先级）→ 构建时注入
2. **动态配置文件**（运行时加载）→ 可动态修改
3. **代码默认值**（最低优先级）→ 降级方案

### 2. 前端读取后端服务地址

#### 方案对比

| 方式 | 优点 | 缺点 | 适用场景 |
|-----|------|------|----------|
| **环境变量** | • 构建时确定<br>• 无需运行时请求<br>• 安全性高 | • 需要为不同环境分别构建<br>• 修改需要重新发布 | ✅ **推荐** - 生产环境 |
| **Vite 代理** | • 开发环境方便<br>• 解决跨域问题<br>• 热更新 | • 仅限开发环境<br>• 不能用于生产环境 | ✅ **推荐** - 开发环境 |
| **动态配置接口** | • 运行时可变<br>• 无需重新构建<br>• 灵活性高 | • 需要额外的配置服务<br>• 增加网络请求 | 微服务架构 |

#### 推荐方案

```typescript
// ✅ 推荐：环境变量 + Vite 代理
// .env.development
VITE_PORT=9527
VITE_API_BASE_URL=http://127.0.0.1:9528

// .env.production
VITE_API_BASE_URL=https://api.example.com

// vite.config.ts
server: {
  port: 9527,
  proxy: {
    "/api": {
      target: "http://127.0.0.1:9528",  // 开发环境代理
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

### 3. 前端直接读取后端配置文件

#### 推荐实现

```typescript
// ✅ 推荐：通过专门的配置接口
// 后端提供配置接口（需权限控制）
@GetMapping("/api/config")
@PreAuthorize("hasPermission('system:config:view')")
public Result<SystemConfig> getSystemConfig() {
    return Result.success(systemConfigService.getConfig());
}

// 前端请求配置
const fetchSystemConfig = async () => {
  try {
    const response = await axios.get('/api/config');
    updateConfig(response.data);
  } catch (error) {
    console.error('获取系统配置失败', error);
    useLocalConfig();
  }
};
```

---

## 🏗️ 后端配置方案

### 1. Spring Boot 多环境配置最佳实践

#### 配置文件层次结构

```
resources/
├── application.yml              # 主配置文件（所有环境通用）
├── application-dev.yml         # 开发环境配置
├── application-test.yml        # 测试环境配置
├── application-staging.yml     # 预发布环境配置
├── application-prod.yml       # 生产环境配置
├── application-local.yml      # 本地个性化配置（.gitignore）
└── application.yml.example   # 配置文件示例（提交到代码库）
```

#### AIHub 后端配置方案（推荐实践）

```yaml
# application.yml - 主配置文件
spring:
  profiles:
    active: dev  # 默认激活 dev 环境
  
  application:
    name: aihub-api
  
  # 支持环境变量覆盖配置
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:aihub}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}

server:
  port: 9528  # 固定端口，所有环境一致
```

```yaml
# application-dev.yml - 开发环境配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aihub
    username: aihub
    password: aihub123456
  
  data:
    redis:
      host: localhost
      port: 6379
      password: aihub123456

logging:
  level:
    com.aihub: debug
```

```yaml
# application-prod.yml - 生产环境配置（不提交到代码库）
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}/aihub
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}

logging:
  level:
    com.aihub: info
```

#### 配置优先级（Spring Boot 加载顺序）

```
1. 命令行参数（最高优先级）
   java -jar app.jar --server.port=8080

2. 环境变量
   export SERVER_PORT=8080
   spring.datasource.url=${DB_URL}

3. 外部配置文件
   config/application-prod.yml
   application-prod.yml

4. 内部配置文件
   classpath:/application-prod.yml
   classpath:/application.yml

5. 默认值
   ${DB_HOST:localhost}  # 如果未设置，使用 localhost
```

### 2. 后端端口配置

#### 推荐方案

```yaml
# ✅ 推荐：配置文件 + 环境变量 + 默认值
server:
  port: ${SERVER_PORT:9528}  # 支持环境变量覆盖，默认 9528

# 或（Docker 环境）
# docker-compose.yml
services:
  aihub-api:
    ports:
      - "${API_PORT:-9528}:9528"  # 支持环境变量，默认 9528
    environment:
      - SERVER_PORT=9528  # 也可通过环境变量设置
```

---

## 🔗 统一配置方案

### 1. 前后端分离项目的统一配置管理

#### 配置统一策略

```typescript
// ✅ 推荐：前后端共享配置文件
// config/shared-config.json（提交到代码库）
{
  "api": {
    "baseURL": "http://localhost:9528",
    "timeout": 30000
  },
  "features": {
    "enableAI": true,
    "enableRAG": false
  }
}
```

### 2. 配置文件中心化方案

#### 主流配置中心对比

| 配置中心 | 优点 | 缺点 | 适用场景 |
|---------|------|------|----------|
| **Nacos** | • 阿里云开源<br>• 功能完善<br>• 服务发现+配置管理 | • 需要额外部署<br>• 学习成本 | 微服务架构 |
| **Apollo** | • 携程开源<br>• 支持灰度发布<br>• 权限控制完善 | • 架构复杂<br>• 需要数据库 | 大型项目 |
| **Spring Cloud Config** | • Spring 官方<br>• 与 Spring Cloud 集成好 | • 功能相对简单<br>• 缺少图形界面 | Spring Cloud 项目 |
| **不使用配置中心** | • 架构简单<br>• 无额外依赖 | • 配置分散<br>• 不支持动态更新 | 小型项目 |

---

## 📊 实际案例对比

### 1. AIHub 配置方案

| 特点 | 说明 |
|-----|------|
| ✅ 前端使用环境变量 | 构建时配置，安全性高 |
| ✅ 前端使用动态配置 | 运行时可变，灵活方便 |
| ✅ 后端使用多环境配置 | 支持 dev/prod/staging 等 |
| ✅ 支持环境变量覆盖 | 容器化友好 |
| ✅ 配置文件分离 | 敏感信息不提交到代码库 |

### 2. Pig 微服务配置方案

| 特点 | 说明 |
|-----|------|
| ✅ 微服务架构 | 每个服务独立配置 |
| ✅ Docker Compose | 统一部署和管理 |
| ✅ 环境变量注入 | 容器化友好 |
| ✅ 固定端口 | 内网部署，避免端口冲突 |

### 3. Sa-Token 配置方案

| 特点 | 说明 |
|-----|------|
| ✅ 单一配置文件 | 简单直接，适合小型项目 |
| ✅ 配置清晰 | 所有关键配置一目了然 |
| ⚠️ 不支持多环境 | 需要修改配置文件 |

---

## ✅ 推荐方案

### 1. 前端配置方案

```typescript
// .env.development
VITE_PORT=9527
VITE_API_BASE_URL=http://127.0.0.1:9528

// .env.production
VITE_API_BASE_URL=https://api.example.com

// vite.config.ts
const { VITE_PORT, VITE_API_BASE_URL } = wrapperEnv(loadEnv(mode, root));

export default {
  server: {
    port: VITE_PORT,
    proxy: {
      "/api": {
        target: VITE_API_BASE_URL || "http://127.0.0.1:9528",
        changeOrigin: true
      }
    }
  }
}
```

### 2. 后端配置方案

```yaml
# application.yml（主配置）
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:aihub}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}

server:
  port: ${SERVER_PORT:9528}
```

### 3. Docker 部署

```yaml
# docker-compose.yml
services:
  aihub-api:
    ports:
      - "9528:9528"
    environment:
      - SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
      - DB_HOST=mysql
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
```

```bash
# 启动命令
export DB_USERNAME=aihub
export DB_PASSWORD=aihub123456
docker compose up -d
```

---

## 📝 实施建议

### 1. 配置文件管理规范

```
配置文件命名：
- application.yml          主配置文件
- application-{env}.yml   环境配置文件（dev/test/staging/prod）
- application-local.yml   本地配置文件（.gitignore）
- application.yml.example 配置文件示例（提交到代码库）

环境变量命名：
- 前端：VITE_*（Vite 特定）
- 后端：SPRING_* 或业务前缀（如 DB_HOST, REDIS_HOST）
- 敏感信息：必须使用环境变量，不写入配置文件
```

### 2. 安全性建议

```yaml
# ❌ 不推荐：敏感信息写入配置文件
spring:
  datasource:
    password: my_password  # 容易泄露

# ✅ 推荐：使用环境变量
spring:
  datasource:
    password: ${DB_PASSWORD}  # 从环境变量读取
```

---

## 📚 参考资料

### 官方文档

- [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
- [Vite Environment Variables](https://vitejs.dev/guide/env-and-mode.html)
- [Nacos Config Management](https://nacos.io/docs/latest/concepts/nacos-config/)

### 开源项目

- [AIHub](https://github.com/aihub/aihub) - 全栈 AI 平台
- [Pig](https://gitee.com/log4j/pig) - 微服务企业平台
- [Sa-Token](https://github.com/dromara/sa-token) - 权限认证框架

---

## 🎯 总结

### 核心原则

1. **配置分离**：通用配置、环境配置、敏感配置分离
2. **环境变量优先**：敏感信息必须使用环境变量
3. **多环境支持**：支持 dev/test/staging/prod 等多环境
4. **安全第一**：敏感信息不提交到代码库
5. **灵活可变**：支持运行时配置更新

### 推荐方案

| 项目类型 | 前端配置 | 后端配置 | 配置中心 |
|---------|---------|---------|----------|
| **小型项目** | 环境变量 | 多环境配置 | ❌ 不需要 |
| **中型项目** | 环境变量 + 动态配置 | 多环境配置 + 环境变量 | 可选 |
| **微服务** | 环境变量 + 配置中心 | 多环境配置 + 配置中心 | ✅ 推荐 |

---

**文档版本**: 1.0  
**最后更新**: 2026-03-27  
**维护者**: AIHub Team
