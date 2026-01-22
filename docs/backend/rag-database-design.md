# RAG 数据库表设计

> 本文档描述 RAG 功能的数据库表结构设计、索引策略和关系模型

## 📋 目录

- [设计原则](#设计原则)
- [数据库表设计](#数据库表设计)
- [ER 图](#er-图)
- [索引设计](#索引设计)
- [数据字典](#数据字典)
- [迁移脚本](#迁移脚本)

---

## 设计原则

### 1. 性能优先

- 读写分离：高频查询字段独立索引
- 分表策略：大表按时间或文档库分表
- 缓存友好：合理设计缓存键，便于 Redis 缓存

### 2. 扩展性优先

- 分区表：支持按文档库分区（未来扩展）
- 软删除：逻辑删除，保留数据追溯
- 版本控制：文档支持多版本

### 3. 一致性保证

- 外键约束：确保数据完整性
- 索引一致性：CRUD 后及时更新索引
- 事务支持：复杂操作使用事务

---

## 数据库表设计

### 1. RAG 文档库表 (rag_library)

**作用**: 管理文档库，支持多租户和多场景

```sql
CREATE TABLE `rag_library` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文档库 ID',
  `name` VARCHAR(255) NOT NULL COMMENT '文档库名称',
  `description` TEXT COMMENT '文档库描述',
  `type` VARCHAR(50) NOT NULL DEFAULT 'general' COMMENT '文档库类型: general/technical/legal/financial',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
  `is_public` TINYINT NOT NULL DEFAULT 0 COMMENT '是否公开: 1-是 0-否',
  `created_by` BIGINT UNSIGNED NOT NULL COMMENT '创建人 ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG 文档库表';
```

---

### 2. RAG 文档表 (rag_document)

**作用**: 存储文档元数据

```sql
CREATE TABLE `rag_document` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文档 ID',
  `library_id` BIGINT UNSIGNED NOT NULL COMMENT '文档库 ID',
  `title` VARCHAR(512) NOT NULL COMMENT '文档标题',
  `file_path` VARCHAR(1024) NOT NULL COMMENT '文件存储路径',
  `file_url` VARCHAR(1024) DEFAULT NULL COMMENT '文件访问 URL',
  `file_name` VARCHAR(512) NOT NULL COMMENT '原始文件名',
  `file_size` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `file_format` VARCHAR(50) NOT NULL COMMENT '文件格式: PDF/DOCX/TXT/MD/HTML',
  `page_count` INT UNSIGNED DEFAULT 0 COMMENT '页数（PDF）',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理 1-处理中 2-已索引 3-索引失败',
  `error_message` TEXT COMMENT '错误信息',
  `chunk_count` INT UNSIGNED DEFAULT 0 COMMENT '分块数量',
  `created_by` BIGINT UNSIGNED NOT NULL COMMENT '上传人 ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_library_id` (`library_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_document_library` FOREIGN KEY (`library_id`) REFERENCES `rag_library` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG 文档表';
```

---

### 3. RAG 文档分块表 (rag_document_chunk)

**作用**: 存储文档分块，每个分块对应一个向量

```sql
CREATE TABLE `rag_document_chunk` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分块 ID',
  `document_id` BIGINT UNSIGNED NOT NULL COMMENT '文档 ID',
  `chunk_index` INT UNSIGNED NOT NULL COMMENT '分块序号',
  `content` TEXT NOT NULL COMMENT '分块内容',
  `content_type` VARCHAR(50) DEFAULT 'text' COMMENT '内容类型: text/table/image',
  `parent_chunk_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父分块 ID（章节结构）',
  `level` TINYINT DEFAULT 1 COMMENT '层级: 1-段落 2-小节 3-子小节',
  `token_count` INT UNSIGNED DEFAULT 0 COMMENT 'Token 数量',
  `vector_id` VARCHAR(128) DEFAULT NULL COMMENT '向量 ID (在向量数据库中的 ID）',
  `is_embedded` TINYINT DEFAULT 0 COMMENT '是否已向量化: 0-否 1-是',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_chunk_index` (`document_id`, `chunk_index`),
  KEY `idx_vector_id` (`vector_id`),
  CONSTRAINT `fk_chunk_document` FOREIGN KEY (`document_id`) REFERENCES `rag_document` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG 文档分块表';
```

---

### 4. RAG 检索日志表 (rag_search_log)

**作用**: 记录检索日志，用于分析和优化

```sql
CREATE TABLE `rag_search_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志 ID',
  `library_id` BIGINT UNSIGNED NOT NULL COMMENT '文档库 ID',
  `query` VARCHAR(2048) NOT NULL COMMENT '查询内容',
  `query_type` VARCHAR(50) NOT NULL COMMENT '查询类型: hybrid/semantic/keyword',
  `top_k` INT UNSIGNED NOT NULL COMMENT '返回数量',
  `result_count` INT UNSIGNED NOT NULL COMMENT '实际结果数量',
  `semantic_count` INT UNSIGNED DEFAULT 0 COMMENT '语义检索结果数',
  `keyword_count` INT UNSIGNED DEFAULT 0 COMMENT '关键词检索结果数',
  `latency_ms` INT UNSIGNED NOT NULL COMMENT '响应时间（毫秒）',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户 ID',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT 'IP 地址',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_library_id` (`library_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG 检索日志表';
```

---

### 5. RAG 文档引用表 (rag_document_reference)

**作用**: 存储文档之间的引用关系（可选，阶段2）

```sql
CREATE TABLE `rag_document_reference` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '引用 ID',
  `source_document_id` BIGINT UNSIGNED NOT NULL COMMENT '源文档 ID',
  `source_chunk_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '源分块 ID',
  `target_document_id` BIGINT UNSIGNED NOT NULL COMMENT '目标文档 ID',
  `target_chunk_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '目标分块 ID',
  `reference_type` VARCHAR(50) DEFAULT 'explicit' COMMENT '引用类型: explicit/implicit',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_source_document` (`source_document_id`),
  KEY `idx_target_document` (`target_document_id`),
  CONSTRAINT `fk_ref_source_document` FOREIGN KEY (`source_document_id`) REFERENCES `rag_document` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ref_target_document` FOREIGN KEY (`target_document_id`) REFERENCES `rag_document` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG 文档引用表';
```

---

## ER 图

```
┌─────────────────┐
│  rag_library  │ 文档库
└───────┬───────┘
        │ 1:N
        │
        ▼
┌─────────────────┐
│ rag_document  │ 文档
└───────┬───────┘
        │ 1:N
        │
        ▼
┌─────────────────────┐
│rag_document_chunk│ 文档分块
└─────────────────────┘

┌─────────────────┐     ┌─────────────────────┐
│rag_search_log │     │rag_document_reference│ 文档引用关系
└─────────────────┘     └─────────────────────┘
```

---

## 索引设计

### rag_library

| 索引名 | 字段 | 类型 | 说明 |
|--------|------|------|------|
| PRIMARY | id | PRIMARY | 主键 |
| idx_type | type | INDEX | 按类型查询 |
| idx_status | status | INDEX | 按状态查询 |
| idx_created_by | created_by | INDEX | 按创建人查询 |

### rag_document

| 索引名 | 字段 | 类型 | 说明 |
|--------|------|------|------|
| PRIMARY | id | PRIMARY | 主键 |
| idx_library_id | library_id | INDEX | 按文档库查询 |
| idx_status | status | INDEX | 按状态查询 |
| idx_created_by | created_by | INDEX | 按创建人查询 |
| idx_created_at | created_at | INDEX | 按创建时间查询 |

### rag_document_chunk

| 索引名 | 字段 | 类型 | 说明 |
|--------|------|------|------|
| PRIMARY | id | PRIMARY | 主键 |
| idx_document_id | document_id | INDEX | 按文档查询分块 |
| idx_chunk_index | document_id, chunk_index | INDEX | 按文档和序号查询 |
| idx_vector_id | vector_id | INDEX | 通过向量 ID 快速查询 |

### rag_search_log

| 索引名 | 字段 | 类型 | 说明 |
|--------|------|------|------|
| PRIMARY | id | PRIMARY | 主键 |
| idx_library_id | library_id | INDEX | 按文档库查询日志 |
| idx_user_id | user_id | INDEX | 按用户查询日志 |
| idx_created_at | created_at | INDEX | 按创建时间查询 |

---

## 数据字典

### rag_library 字典

| 字段名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | 是 | AUTO | 主键 |
| name | VARCHAR(255) | 是 | - | 文档库名称 |
| description | TEXT | 否 | NULL | 文档库描述 |
| type | VARCHAR(50) | 是 | 'general' | 文档库类型 |
| status | TINYINT | 是 | 1 | 状态 |
| is_public | TINYINT | 是 | 0 | 是否公开 |
| created_by | BIGINT | 是 | - | 创建人 ID |
| created_at | DATETIME | 是 | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | 是 | CURRENT_TIMESTAMP | 更新时间 |
| deleted_at | DATETIME | 否 | NULL | 删除时间 |

### rag_document 字典

| 字段名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | 是 | AUTO | 主键 |
| library_id | BIGINT | 是 | - | 文档库 ID |
| title | VARCHAR(512) | 是 | - | 文档标题 |
| file_path | VARCHAR(1024) | 是 | - | 文件存储路径 |
| file_url | VARCHAR(1024) | 否 | NULL | 文件访问 URL |
| file_name | VARCHAR(512) | 是 | - | 原始文件名 |
| file_size | BIGINT | 是 | 0 | 文件大小 |
| file_format | VARCHAR(50) | 是 | - | 文件格式 |
| page_count | INT | 否 | 0 | 页数 |
| status | TINYINT | 是 | 0 | 状态 |
| error_message | TEXT | 否 | NULL | 错误信息 |
| chunk_count | INT | 否 | 0 | 分块数量 |
| created_by | BIGINT | 是 | - | 上传人 ID |
| created_at | DATETIME | 是 | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | 是 | CURRENT_TIMESTAMP | 更新时间 |
| deleted_at | DATETIME | 否 | NULL | 删除时间 |

---

## 迁移脚本

### Flyway 迁移脚本

**文件**: `backend/aihub-api/src/main/resources/db/migration/V2.0.0__add_rag_tables.sql`

```sql
-- ============================================================================
-- RAG 模块数据库表
-- 版本: V2.0.0
-- 日期: 2026-01-22
-- 说明: 创建 RAG 功能相关的数据库表
-- ============================================================================

-- 1. 创建 RAG 文档库表
CREATE TABLE `rag_library` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文档库 ID',
  `name` VARCHAR(255) NOT NULL COMMENT '文档库名称',
  `description` TEXT COMMENT '文档库描述',
  `type` VARCHAR(50) NOT NULL DEFAULT 'general' COMMENT '文档库类型',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `is_public` TINYINT NOT NULL DEFAULT 0 COMMENT '是否公开',
  `created_by` BIGINT UNSIGNED NOT NULL COMMENT '创建人 ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG 文档库表';

-- 2. 创建 RAG 文档表
CREATE TABLE `rag_document` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文档 ID',
  `library_id` BIGINT UNSIGNED NOT NULL COMMENT '文档库 ID',
  `title` VARCHAR(512) NOT NULL COMMENT '文档标题',
  `file_path` VARCHAR(1024) NOT NULL COMMENT '文件存储路径',
  `file_url` VARCHAR(1024) DEFAULT NULL COMMENT '文件访问 URL',
  `file_name` VARCHAR(512) NOT NULL COMMENT '原始文件名',
  `file_size` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `file_format` VARCHAR(50) NOT NULL COMMENT '文件格式',
  `page_count` INT UNSIGNED DEFAULT 0 COMMENT '页数',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理 1-处理中 2-已索引 3-索引失败',
  `error_message` TEXT COMMENT '错误信息',
  `chunk_count` INT UNSIGNED DEFAULT 0 COMMENT '分块数量',
  `created_by` BIGINT UNSIGNED NOT NULL COMMENT '上传人 ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_library_id` (`library_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_document_library` FOREIGN KEY (`library_id`) REFERENCES `rag_library` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG 文档表';

-- 3. 创建 RAG 文档分块表
CREATE TABLE `rag_document_chunk` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分块 ID',
  `document_id` BIGINT UNSIGNED NOT NULL COMMENT '文档 ID',
  `chunk_index` INT UNSIGNED NOT NULL COMMENT '分块序号',
  `content` TEXT NOT NULL COMMENT '分块内容',
  `content_type` VARCHAR(50) DEFAULT 'text' COMMENT '内容类型: text/table/image',
  `parent_chunk_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父分块 ID（章节结构）',
  `level` TINYINT DEFAULT 1 COMMENT '层级',
  `token_count` INT UNSIGNED DEFAULT 0 COMMENT 'Token 数量',
  `vector_id` VARCHAR(128) DEFAULT NULL COMMENT '向量 ID',
  `is_embedded` TINYINT DEFAULT 0 COMMENT '是否已向量化',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_chunk_index` (`document_id`, `chunk_index`),
  KEY `idx_vector_id` (`vector_id`),
  CONSTRAINT `fk_chunk_document` FOREIGN KEY (`document_id`) REFERENCES `rag_document` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG 文档分块表';

-- 4. 创建 RAG 检索日志表
CREATE TABLE `rag_search_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志 ID',
  `library_id` BIGINT UNSIGNED NOT NULL COMMENT '文档库 ID',
  `query` VARCHAR(2048) NOT NULL COMMENT '查询内容',
  `query_type` VARCHAR(50) NOT NULL COMMENT '查询类型',
  `top_k` INT UNSIGNED NOT NULL COMMENT '返回数量',
  `result_count` INT UNSIGNED NOT NULL COMMENT '实际结果数量',
  `semantic_count` INT UNSIGNED DEFAULT 0 COMMENT '语义检索结果数',
  `keyword_count` INT UNSIGNED DEFAULT 0 COMMENT '关键词检索结果数',
  `latency_ms` INT UNSIGNED NOT NULL COMMENT '响应时间（毫秒）',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户 ID',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT 'IP 地址',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_library_id` (`library_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG 检索日志表';

-- 5. 创建 RAG 文档引用表（可选，阶段2）
CREATE TABLE `rag_document_reference` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '引用 ID',
  `source_document_id` BIGINT UNSIGNED NOT NULL COMMENT '源文档 ID',
  `source_chunk_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '源分块 ID',
  `target_document_id` BIGINT UNSIGNED NOT NULL COMMENT '目标文档 ID',
  `target_chunk_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '目标分块 ID',
  `reference_type` VARCHAR(50) DEFAULT 'explicit' COMMENT '引用类型',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_source_document` (`source_document_id`),
  KEY `idx_target_document` (`target_document_id`),
  CONSTRAINT `fk_ref_source_document` FOREIGN KEY (`source_document_id`) REFERENCES `rag_document` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ref_target_document` FOREIGN KEY (`target_document_id`) REFERENCES `rag_document` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG 文档引用表';
```

---

## 初始化数据

### 插入默认文档库

```sql
-- 插入默认文档库
INSERT INTO `rag_library` (`name`, `description`, `type`, `created_by`)
VALUES
  ('通用知识库', '系统默认的通用知识库，用于存储通用文档', 'general', 1);
```

---

## 相关文档

- [RAG 数据入库指南](rag-data-ingestion-guide.md) - 数据入库和文档处理
- [RAG 检索架构设计](../architecture/rag-retrieval.md) - RAG 检索架构设计
- [实施路线图](../roadmap/implementation-roadmap.md) - RAG 功能实施计划
- [模块架构规划](../backend/module-architecture.md) - 后端模块划分方案
- [数据库设计文档](../backend/database.md) - 总体数据库设计
- [功能清单](../features.md) - RAG 相关功能状态跟踪

---

> 本文档会随着开发进度持续更新，建议定期同步最新版本。
