# SQL 脚本管理

> 本文档说明 SQL 脚本的存放方式和管理规范。

## 📚 相关文档

- [数据库设计文档](../backend/database.md) - 数据库设计规范
- [后端开发指南](../backend/guide.md) - 后端开发文档
- [项目主文档](../../README.md) - 返回项目文档入口

---

## 📦 存放方式

### 采用方式：**全量脚本为主，增量脚本为辅（可选 Flyway）**

#### 全量脚本（主要方式）

- **位置**: `init/` 目录
- **命名规范**: `init_{version}.sql` 或 `schema_{version}.sql`
  - 示例: `init_v1.0.0.sql`, `schema_latest.sql`
- **用途**:
  - 新环境快速初始化（推荐）
  - 数据库结构参考
  - 开发环境快速搭建
  - 可以直接手动执行，不依赖任何工具

#### 增量脚本（可选，配合 Flyway 使用）

- **位置**: `migrations/` 目录
- **命名规范**: `V{version}__{description}.sql`
  - 示例: `V1.0.0__init_tables.sql`, `V1.0.1__add_user_index.sql`
- **用途**: 
  - 版本控制和数据库迁移（需要 Flyway 工具）
  - 记录每次数据库变更
  - 便于代码审查和变更追踪
- **注意**: 如果项目没有引入 Flyway，可以忽略此目录

---

## 📁 目录结构

```
docs/sql/
├── guide.md              # 本文件
├── migrations/            # 增量脚本（Flyway 迁移脚本）
│   ├── V1.0.0__init_tables.sql
│   ├── V1.0.1__add_user_index.sql
│   └── V1.0.2__add_agent_table.sql
├── init/                  # 全量初始化脚本
│   ├── init_v1.0.0.sql    # 版本化全量脚本
│   └── schema_latest.sql  # 最新全量脚本（自动生成）
└── data/                  # 测试数据脚本（可选）
    └── test_data.sql
```

---

## 🎯 脚本使用策略

### 全量脚本的优势（主要方式）

1. **快速初始化**
   - 新环境可以直接执行全量脚本
   - 无需按顺序执行所有增量脚本
   - 不依赖任何工具，直接执行即可

2. **结构参考**
   - 查看完整的数据库结构
   - 便于理解和文档化
   - 一目了然所有表结构

3. **备份恢复**
   - 数据库结构备份
   - 紧急恢复场景
   - 版本化保存，便于回滚

### 增量脚本的优势（配合 Flyway 使用）

1. **版本控制友好**
   - 每次变更都是独立的文件
   - 便于 Git 追踪和代码审查
   - 冲突少，易于合并

2. **变更历史清晰**
   - 每个脚本记录一次变更
   - 可以清楚看到数据库演进过程
   - 便于回滚和问题定位

3. **符合 Flyway 规范**（如果使用 Flyway）
   - Flyway 本身就是基于增量脚本的
   - 自动执行，无需手动管理
   - 如果项目没有 Flyway，可以手动执行增量脚本

4. **团队协作友好**
   - 多人开发时，各自创建增量脚本
   - 避免全量脚本的冲突
   - 便于并行开发

---

## 📝 使用规范

### 增量脚本规范

1. **命名规范**
   ```
   V{主版本}.{次版本}.{修订版本}__{描述}.sql
   ```
   - 版本号遵循语义化版本（Semantic Versioning）
   - 描述使用下划线分隔，简洁明了
   - 示例: `V1.0.0__init_tables.sql`, `V1.0.1__add_user_email_index.sql`

2. **脚本内容**
   - 每个脚本应该是幂等的（可以重复执行）
   - 使用 `IF NOT EXISTS` 等语句保证幂等性
   - 包含必要的注释说明变更原因
   - **重要**: 不要包含 `CREATE DATABASE` 和 `USE` 语句
     - Flyway 会在应用配置的数据库连接上执行
     - 数据库连接在 `application.yml` 中配置

3. **脚本顺序**
   - 按照版本号顺序执行
   - Flyway 会自动管理执行顺序

### 全量脚本规范

1. **脚本内容**
   - 可以包含 `CREATE DATABASE` 和 `USE` 语句
   - 用于新环境快速初始化，可以直接执行
   - 包含完整的数据库结构

2. **更新时机**
   - 每次发布新版本时更新
   - 保持与最新增量脚本一致

2. **生成方式**
   - 可以从数据库导出
   - 也可以从增量脚本合并生成

3. **维护**
   - 保留历史版本（版本化）
   - 保留最新版本（`schema_latest.sql`）

---

## 🔄 工作流程

### 开发新功能时

1. **创建增量脚本**
   ```bash
   # 创建新的迁移脚本
   touch docs/sql/migrations/V1.0.1__add_new_feature.sql
   ```

2. **编写 SQL**
   ```sql
   -- V1.0.1: 添加新功能相关表
   CREATE TABLE IF NOT EXISTS new_feature (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       -- ...
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
   ```

3. **提交代码**
   - 增量脚本随代码一起提交
   - Flyway 会自动执行

### 发布新版本时

1. **更新全量脚本**
   - 从数据库导出最新结构
   - 保存为 `init_v{version}.sql`

2. **更新最新脚本**
   - 更新 `schema_latest.sql`

---

## 🛠️ Flyway 集成（可选，仅开发环境）

**注意**: Flyway 是可选的数据库迁移工具。如果项目没有引入 Flyway，可以直接使用全量脚本手动初始化数据库。

**重要原则**: Flyway 应该只在开发环境启用，测试和生产环境必须禁用，由运维人员手动执行数据库迁移。

### 使用 Flyway 的前提条件

1. 在 `pom.xml` 中添加 Flyway 依赖
2. 在配置文件中按环境配置 Flyway

### 多环境配置示例

**开发环境** (`application-dev.yml`):
```yaml
spring:
  flyway:
    enabled: true                    # ✅ 开发环境启用
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 1.0.0
```

**测试环境** (`application-test.yml`):
```yaml
spring:
  flyway:
    enabled: false                   # ❌ 测试环境禁用
```

**生产环境** (`application-prod.yml`):
```yaml
spring:
  flyway:
    enabled: false                   # ❌ 生产环境禁用（必须）
```

### 脚本位置

- 开发时：`docs/sql/migrations/`（文档目录）
- 运行时：`src/main/resources/db/migration/`（资源目录）
- 部署时：将 `docs/sql/migrations/` 中的脚本复制到资源目录

### 环境配置说明

| 环境 | Flyway 状态 | 数据库迁移方式 | 原因 |
|------|------------|--------------|------|
| **开发环境** | ✅ 启用 | 自动执行 | 方便开发，快速迭代 |
| **测试环境** | ❌ 禁用 | 手动执行 | 需要测试验证，可控变更 |
| **生产环境** | ❌ 禁用 | 手动执行 | 安全第一，避免自动变更风险 |

### 生产环境数据库迁移流程

1. **开发阶段**: 在开发环境使用 Flyway 自动执行，验证脚本正确性
2. **测试阶段**: 在测试环境手动执行脚本，进行充分测试
3. **生产发布**: 
   - 运维人员审核 SQL 脚本
   - 在维护窗口期手动执行
   - 验证执行结果
   - 如有问题，执行回滚脚本

### 不使用 Flyway 的方式

如果项目不使用 Flyway，推荐使用全量脚本手动初始化：

```bash
# 直接执行全量脚本
mysql -u root -p < docs/sql/init/init_v1.0.0.sql
```

后续数据库变更时，可以：
1. 手动执行新的 SQL 语句
2. 或者更新全量脚本，重新执行

---

## 📋 检查清单

创建 SQL 脚本时，请检查：

- [ ] 脚本命名符合规范（V{version}__{description}.sql）
- [ ] 脚本内容是幂等的（可重复执行）
- [ ] 包含必要的注释说明
- [ ] 已测试脚本可以正常执行
- [ ] 已更新相关文档（如 database.md）

---

## 🔗 相关文档

- [数据库设计文档](../backend/database.md) - 查看数据库设计规范
- [后端开发指南](../backend/guide.md) - 查看后端开发文档
- [项目主文档](../../README.md) - 返回项目文档入口
