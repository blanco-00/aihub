# 数据库设计文档

> 本文档包含数据库选型说明、表结构设计和数据库规范。

## 📚 相关文档

- [后端开发指南](./guide.md) - 返回后端文档总览
- [SQL 脚本管理](../sql/README.md) - SQL 脚本存放方式和管理规范
- [项目主文档](../../README.md) - 返回项目文档入口

## 数据库选型

### 已选型：MySQL 8.0+

**选择理由**：
- ✅ 使用广泛，团队熟悉度高
- ✅ 生态成熟，工具和文档丰富
- ✅ 性能优秀，适合大多数业务场景
- ✅ 社区活跃，问题解决容易
- ✅ 运维经验丰富，部署简单

### 数据库对比

#### MySQL vs PostgreSQL

| 特性 | MySQL | PostgreSQL |
|------|-------|------------|
| **使用场景** | Web应用、中小型系统 | 复杂查询、数据分析 |
| **性能** | 读写性能优秀 | 复杂查询性能优秀 |
| **SQL标准** | 部分支持 | 高度符合SQL标准 |
| **数据类型** | 基础类型丰富 | 类型系统更强大（JSON、数组等） |
| **事务支持** | 支持（InnoDB） | 完全支持，MVCC |
| **并发控制** | 行级锁 | 多版本并发控制（MVCC） |
| **学习曲线** | 平缓 | 相对陡峭 |
| **社区生态** | 非常活跃 | 活跃 |
| **适用项目** | 本项目 ✅ | 数据分析、GIS等 |

**本项目选择MySQL的原因**：
- 团队对MySQL更熟悉
- 项目主要是CRUD操作，MySQL性能足够
- 部署和维护更简单
- 生态工具更丰富

#### MySQL vs OceanBase

| 特性 | MySQL | OceanBase |
|------|-------|-----------|
| **定位** | 关系型数据库 | 分布式数据库 |
| **架构** | 单机/主从 | 分布式架构 |
| **扩展性** | 垂直扩展 | 水平扩展 |
| **一致性** | 最终一致性（主从） | 强一致性（分布式） |
| **适用规模** | 中小型应用 | 大型分布式系统 |
| **复杂度** | 简单 | 复杂 |
| **成本** | 开源免费 | 开源/商业版 |
| **使用场景** | 本项目 ✅ | 大规模分布式系统 |

**OceanBase特点**：
- 阿里开源的分布式数据库
- 支持MySQL协议，兼容性好
- 适合大规模、高并发的分布式场景
- 对于本项目来说过于复杂

**本项目选择MySQL的原因**：
- MVP阶段不需要分布式架构
- MySQL完全满足需求
- 后续如需扩展，可考虑迁移到OceanBase或分库分表

### 数据库版本

- **推荐版本**: MySQL 8.0+
- **最低版本**: MySQL 5.7+（不推荐，建议8.0+）

### MySQL 8.0+ 新特性优势

1. **窗口函数** - 支持复杂的分析查询
2. **CTE（公共表表达式）** - 提高SQL可读性
3. **JSON支持增强** - 更好的JSON操作
4. **角色管理** - 更细粒度的权限控制
5. **性能提升** - 查询优化器改进

## 数据库设计规范

### 命名规范

#### 表命名
- 使用小写字母和下划线
- 表名使用复数形式（可选）
- 示例：`user_info`, `model_config`, `agent_workflow`

#### 字段命名
- 使用小写字母和下划线
- 布尔字段使用 `is_` 前缀
- 时间字段使用 `_time` 或 `_at` 后缀
- 示例：`user_name`, `is_active`, `created_at`, `updated_at`

#### 索引命名
- 主键：`pk_{table_name}`
- 唯一索引：`uk_{table_name}_{column}`
- 普通索引：`idx_{table_name}_{column}`
- 示例：`pk_user`, `uk_user_email`, `idx_user_created_at`

### 字段规范

#### 必须字段
每个表都应该包含以下字段：
```sql
id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除'
```

#### 字段类型选择
- **ID**: `BIGINT` (自增主键)
- **字符串**: 
  - 短字符串（<255）: `VARCHAR(n)`
  - 长文本: `TEXT` 或 `LONGTEXT`
- **数字**: 
  - 整数: `INT` 或 `BIGINT`
  - 小数: `DECIMAL(m,n)`
- **布尔**: `TINYINT(1)` (0/1)
- **时间**: `DATETIME` 或 `TIMESTAMP`
- **JSON**: `JSON` (MySQL 5.7+)

### 索引设计

#### 索引原则
1. 主键自动创建聚簇索引
2. 外键字段创建索引
3. 经常用于WHERE条件的字段创建索引
4. 经常用于ORDER BY的字段创建索引
5. 避免过多索引（影响写入性能）

#### 索引示例
```sql
-- 单列索引
CREATE INDEX idx_user_email ON user(email);

-- 复合索引
CREATE INDEX idx_user_status_created ON user(status, created_at);

-- 唯一索引
CREATE UNIQUE INDEX uk_user_username ON user(username);
```

### 表设计原则

1. **范式设计**: 至少满足第三范式
2. **适度冗余**: 在性能要求高的场景可适度冗余
3. **分表策略**: 单表数据量超过500万考虑分表
4. **字段注释**: 所有字段必须有COMMENT注释
5. **字符集**: 统一使用 `utf8mb4` 字符集
6. **存储引擎**: 使用 `InnoDB`（MySQL 5.5+默认）

## 核心表结构设计（待完善）

### 用户表 (user)
```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色 SUPER_ADMIN-超级管理员 ADMIN-管理员 USER-普通用户',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_email (email),
    INDEX idx_user_role (role),
    INDEX idx_user_status (status),
    INDEX idx_user_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

**角色说明**:
- `SUPER_ADMIN`: 超级管理员，拥有所有权限，至少保留一个
- `ADMIN`: 管理员，拥有大部分管理权限
- `USER`: 普通用户，基础使用权限

### 模型配置表 (model_config)
```sql
CREATE TABLE model_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模型ID',
    name VARCHAR(100) NOT NULL COMMENT '模型名称',
    vendor VARCHAR(50) NOT NULL COMMENT '厂商 OpenAI/Claude/DeepSeek等',
    model_id VARCHAR(100) NOT NULL COMMENT '模型ID',
    api_key VARCHAR(255) NOT NULL COMMENT 'API Key（加密存储）',
    base_url VARCHAR(255) COMMENT 'Base URL',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    config JSON COMMENT '模型配置参数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_model_vendor (vendor),
    INDEX idx_model_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型配置表';
```

### Agent表 (agent)
```sql
CREATE TABLE agent (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Agent ID',
    name VARCHAR(100) NOT NULL COMMENT 'Agent名称',
    description TEXT COMMENT '描述',
    workflow JSON NOT NULL COMMENT '工作流配置',
    prompt_id BIGINT COMMENT '关联的Prompt ID',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态',
    created_by BIGINT NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_agent_created_by (created_by),
    INDEX idx_agent_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent表';
```

### Prompt表 (prompt)
```sql
CREATE TABLE prompt (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Prompt ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT 'Prompt内容',
    description TEXT COMMENT '描述',
    tags JSON COMMENT '标签',
    category VARCHAR(50) COMMENT '分类',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号',
    parent_id BIGINT COMMENT '父版本ID',
    rating DECIMAL(3,2) COMMENT '评分',
    use_count INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    created_by BIGINT NOT NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_prompt_category (category),
    INDEX idx_prompt_created_by (created_by),
    INDEX idx_prompt_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt表';
```

> 更多表结构设计待完善...

## 数据库连接配置

### Spring Boot配置示例

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/aihub?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

## 数据库迁移

### SQL 脚本管理

- **SQL 脚本位置**: `backend/aihub-api/src/main/resources/db/migration/`
- **存放方式**: 增量方式为主，全量脚本为辅
- **详细说明**: 请参考 [SQL 脚本管理文档](../sql/guide.md)

### 使用Flyway（可选，仅开发环境）
- 版本化数据库迁移
- 自动执行SQL脚本
- 支持回滚
- SQL 脚本存放在 `backend/aihub-api/src/main/resources/db/migration/` 目录

### 使用Liquibase
- 另一种数据库迁移工具
- 支持XML/YAML格式

## 性能优化建议

1. **查询优化**
   - 避免SELECT *
   - 使用EXPLAIN分析查询
   - 合理使用索引

2. **连接池配置**
   - 使用HikariCP
   - 合理设置连接池大小

3. **读写分离**
   - 主从复制
   - 读写分离（后续扩展）

4. **分库分表**
   - 单表数据量大时考虑
   - 使用ShardingSphere等中间件

## 下一步

1. 完善所有表结构设计
2. 设计数据库索引策略
3. 配置数据库迁移工具
4. 编写数据库初始化脚本

