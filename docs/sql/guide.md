# SQL 脚本管理

> 📚 **相关文档**: [项目主文档](../../README.md) | [数据库设计文档](../backend/database.md) | [系统初始化文档](../backend/initialization.md)

## Flyway 数据库迁移

### 迁移脚本位置

迁移脚本位于：`backend/aihub-api/src/main/resources/db/migration/`

### 迁移脚本列表

#### V1.0.0__init_tables.sql
- **说明**: 初始化数据库表结构
- **包含表**: user, model_config
- **注意**: 此脚本包含最新的表结构（包括 nickname 字段）

#### V1.0.1__add_nickname_to_user.sql
- **说明**: 为已有数据库添加 nickname 字段
- **用途**: 用于升级已有数据库（从 V1.0.0 升级到包含 nickname 的版本）
- **注意**: 新数据库不需要执行此脚本，因为 V1.0.0 已包含 nickname 字段

### 使用方式

#### 1. 启用 Flyway

在 `application.yml` 或 `application-dev.yml` 中启用 Flyway：

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 1.0.0
```

#### 2. 自动迁移

启动应用时，Flyway 会自动：
- **首次启动时自动创建 `flyway_schema_history` 表**（如果不存在）
- 检查数据库中的迁移历史表 `flyway_schema_history`
- 按版本号顺序执行未执行的迁移脚本
- 记录执行历史，避免重复执行

**注意**：
- 如果 Flyway 未启用（`enabled: false`），`flyway_schema_history` 表不会存在，这是正常的
- 只有启用 Flyway 并启动应用后，才会创建此表

#### 3. 新数据库

对于全新的数据库：
- Flyway 会执行 V1.0.0（包含完整的表结构，包括 nickname）
- 不会执行 V1.0.1（因为 V1.0.0 已包含 nickname）

#### 4. 已有数据库

对于已有数据库（已执行过 V1.0.0，但没有 nickname 字段）：
- Flyway 会检测到 V1.0.0 已执行
- 自动执行 V1.0.1，添加 nickname 字段
- 将现有用户的 nickname 设置为 username

### 命名规范

迁移脚本命名格式：`V{版本号}__{描述}.sql`

- 版本号：使用点号分隔，如 `1.0.0`, `1.0.1`, `1.1.0`
- 描述：使用下划线分隔的英文描述
- 示例：`V1.0.1__add_nickname_to_user.sql`

### 注意事项

1. **Flyway 历史表**：
   - `flyway_schema_history` 表是 Flyway 自动创建的，用于记录迁移脚本的执行历史
   - **只有在启用 Flyway 并首次启动应用时才会创建此表**
   - 如果 Flyway 未启用（`enabled: false`），此表不会存在，这是正常的
   - 表结构：包含 `installed_rank`、`version`、`description`、`type`、`script`、`checksum`、`installed_on` 等字段
2. **不要修改已执行的迁移脚本**：已执行的脚本会被记录在 `flyway_schema_history` 表中，修改会导致校验失败
3. **新字段添加到最新版本**：新字段应该添加到最新的迁移脚本中，并更新 V1.0.0（全量初始化脚本）
4. **向后兼容**：迁移脚本应该支持重复执行（使用 `IF NOT EXISTS` 等）
5. **数据迁移**：如果需要迁移数据，应该在迁移脚本中包含数据迁移逻辑

### 当前状态

- ✅ V1.0.0: 初始化表结构（包含 nickname 字段）
- ✅ V1.0.1: 为已有数据库添加 nickname 字段（支持重复执行）

---

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

### SQL 脚本实际位置（后端项目）

```
backend/aihub-api/src/main/resources/
└── db/
    └── migration/         # Flyway 迁移脚本（实际位置）
        ├── V1.0.0__init_tables.sql
        ├── V1.0.1__add_user_index.sql
        └── V1.0.2__add_agent_table.sql
```

### 文档目录（仅用于说明）

```
docs/sql/
├── guide.md              # 本文件（SQL 脚本管理规范）
├── init/                  # 全量初始化脚本（可选，用于手动执行）
│   └── init_v1.0.0.sql    # 全量脚本（包含 CREATE DATABASE）
└── data/                  # 测试数据脚本（可选）
    └── test_data.sql
```

**注意**: 
- 增量脚本（migrations）统一存放在后端项目的 `resources/db/migration/` 目录
- 文档目录中的 `init/` 目录可以保留全量脚本，用于手动执行和参考

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
   # 在后端项目的 resources 目录创建新的迁移脚本
   touch backend/aihub-api/src/main/resources/db/migration/V1.0.1__add_new_feature.sql
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

## 🛠️ Flyway 集成（推荐在开发环境启用）

**注意**: Flyway 是推荐的数据库迁移工具，可以自动管理数据库版本和增量迁移。

**重要原则**: Flyway 应该只在开发环境启用，测试和生产环境必须禁用，由运维人员手动执行数据库迁移。

### 为什么启用 Flyway？

1. **自动增量迁移**：
   - 添加新字段时，只需创建新的迁移脚本（如 `V1.0.2__add_new_field.sql`）
   - Flyway 会自动检测并执行未执行的迁移脚本
   - 无需手动执行 SQL，提高开发效率

2. **版本管理**：
   - 所有数据库变更都记录在迁移脚本中
   - 便于代码审查和版本追踪
   - 团队协作时，每个人拉取代码后自动同步数据库结构

3. **与 Docker 初始化脚本的配合**：
   - Docker 初始化脚本（`docker-entrypoint-initdb.d`）只在**首次创建数据库**时执行
   - 如果数据库已存在，Docker 初始化脚本不会执行
   - Flyway 的 `baseline-on-migrate: true` 会自动处理已有数据库的情况
   - 两者不会冲突：Docker 负责首次初始化，Flyway 负责后续增量迁移

4. **幂等性保证**：
   - Flyway 会记录已执行的脚本，避免重复执行
   - 即使重启应用，也不会重复执行已执行的脚本

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

**SQL 脚本统一存放在后端项目的资源目录**：

- **位置**: `backend/aihub-api/src/main/resources/db/migration/`
- **原因**: 
  - 单一数据源，避免重复维护
  - 运行时直接可用，无需复制
  - 版本控制清晰，SQL 脚本与代码版本对应
  - 符合 Spring Boot 资源管理规范

**文档目录说明**：
- `docs/sql/` 目录仅用于文档说明和参考
- 实际 SQL 脚本存放在后端项目的 `resources` 目录

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
