# 通知公告模块设计

> 本文档是通知公告模块的完整设计文档，包含数据库表设计和功能设计。

## 📚 相关文档

- [后端开发指南](./guide.md) - 返回后端文档总览
- [功能清单](../features.md) - 功能规划与开发状态
- [数据库设计文档](./database.md) - 数据库设计规范
- [项目主文档](../../README.md) - 返回项目文档入口

## 功能概述

通知公告模块用于系统内消息的发布、查看和管理，支持通知分类、发布范围控制和已读状态跟踪。

### 核心功能

1. **通知分类管理**：通知分类的CRUD操作
2. **通知公告管理（管理员）**：通知的创建、编辑、发布、撤回、删除
3. **通知查看（用户端）**：用户查看自己收到的通知，标记已读
4. **通知提醒**：顶部导航栏显示未读数量角标

## 数据库设计

### 1. 通知公告表 (notice)

**表设计说明**：
- 遵循项目数据库设计规范（字段命名、索引、逻辑删除等）
- 支持通知分类、发布范围、已读状态等核心功能
- 包含发布人信息、状态管理、排序等字段

```sql
CREATE TABLE IF NOT EXISTS notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容（支持HTML）',
    category_id BIGINT NULL COMMENT '通知分类ID',
    type TINYINT NOT NULL DEFAULT 1 COMMENT '通知类型 1-普通通知 2-重要通知 3-紧急通知',
    publish_type TINYINT NOT NULL DEFAULT 1 COMMENT '发布范围类型 1-全部用户 2-指定部门 3-指定角色 4-指定用户',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    publisher_name VARCHAR(50) NOT NULL COMMENT '发布人姓名',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-草稿 1-已发布 2-已撤回',
    publish_time DATETIME NULL COMMENT '发布时间',
    expire_time DATETIME NULL COMMENT '过期时间（可选）',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序（数字越大越靠前）',
    view_count INT NOT NULL DEFAULT 0 COMMENT '查看次数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    INDEX idx_notice_category (category_id),
    INDEX idx_notice_type (type),
    INDEX idx_notice_status (status),
    INDEX idx_notice_publish_time (publish_time),
    INDEX idx_notice_is_deleted (is_deleted),
    INDEX idx_notice_status_publish_time (status, publish_time),
    INDEX idx_notice_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';
```

### 2. 通知分类表 (notice_category)

```sql
CREATE TABLE IF NOT EXISTS notice_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    code VARCHAR(50) NOT NULL COMMENT '分类代码（唯一）',
    description VARCHAR(200) NULL COMMENT '分类描述',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    UNIQUE KEY uk_notice_category_code (code),
    INDEX idx_notice_category_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知分类表';
```

### 3. 通知发布范围表 (notice_scope)

用于存储通知的发布范围（部门、角色、用户）

```sql
CREATE TABLE IF NOT EXISTS notice_scope (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    notice_id BIGINT NOT NULL COMMENT '通知ID',
    scope_type TINYINT NOT NULL COMMENT '范围类型 1-部门 2-角色 3-用户',
    scope_id BIGINT NOT NULL COMMENT '范围ID（部门ID/角色ID/用户ID）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_notice_scope_notice (notice_id),
    INDEX idx_notice_scope_type_id (scope_type, scope_id),
    FOREIGN KEY (notice_id) REFERENCES notice(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知发布范围表';
```

### 4. 用户通知关联表 (notice_user)

用于记录用户对通知的已读状态

```sql
CREATE TABLE IF NOT EXISTS notice_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    notice_id BIGINT NOT NULL COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
    read_time DATETIME NULL COMMENT '阅读时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_notice_user (notice_id, user_id),
    INDEX idx_notice_user_user (user_id),
    INDEX idx_notice_user_read (is_read),
    INDEX idx_notice_user_user_read (user_id, is_read),
    FOREIGN KEY (notice_id) REFERENCES notice(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知关联表';
```

## 功能设计

### 1. 通知分类管理

- **功能**：通知分类的CRUD操作
- **接口**：
  - `GET /api/notice-categories` - 获取分类列表
  - `POST /api/notice-categories` - 创建分类
  - `PUT /api/notice-categories/{id}` - 更新分类
  - `DELETE /api/notice-categories/{id}` - 删除分类

### 2. 通知公告管理（管理员）

- **功能**：通知的创建、编辑、发布、撤回、删除
- **接口**：
  - `GET /api/notices` - 获取通知列表（分页、搜索、筛选）
  - `GET /api/notices/{id}` - 获取通知详情
  - `POST /api/notices` - 创建通知（草稿）
  - `PUT /api/notices/{id}` - 更新通知
  - `POST /api/notices/{id}/publish` - 发布通知
  - `POST /api/notices/{id}/withdraw` - 撤回通知
  - `DELETE /api/notices/{id}` - 删除通知

### 3. 通知查看（用户端）

- **功能**：用户查看自己收到的通知，标记已读
- **接口**：
  - `GET /api/notices/my` - 获取我的通知列表（分页）
  - `GET /api/notices/my/unread-count` - 获取未读通知数量
  - `GET /api/notices/{id}/detail` - 查看通知详情（自动标记已读）
  - `PUT /api/notices/{id}/read` - 标记通知为已读
  - `PUT /api/notices/read-all` - 全部标记为已读

### 4. 发布范围设计

**发布范围类型**：
1. **全部用户**：所有用户都能看到
2. **指定部门**：通过 `notice_scope` 表关联部门ID
3. **指定角色**：通过 `notice_scope` 表关联角色ID
4. **指定用户**：通过 `notice_scope` 表关联用户ID

**实现逻辑**：
- 发布通知时，根据 `publish_type` 和 `notice_scope` 表，计算应该接收通知的用户列表
- 在 `notice_user` 表中为每个接收用户创建一条记录（初始状态为未读）
- 用户查看通知时，更新 `notice_user` 表的 `is_read` 和 `read_time`

## 后端实现

### 1. 包结构

遵循项目包结构规范（参考 [模块架构文档](./module-architecture.md)）：

```
com.aihub.admin/
├── entity/
│   ├── Notice.java
│   ├── NoticeCategory.java
│   ├── NoticeScope.java
│   └── NoticeUser.java
├── dto/
│   ├── request/
│   │   ├── NoticeListRequest.java
│   │   ├── CreateNoticeRequest.java
│   │   ├── UpdateNoticeRequest.java
│   │   ├── PublishNoticeRequest.java
│   │   ├── NoticeCategoryListRequest.java
│   │   ├── CreateNoticeCategoryRequest.java
│   │   └── UpdateNoticeCategoryRequest.java
│   └── response/
│       ├── NoticeResponse.java
│       ├── NoticeListResponse.java
│       ├── NoticeDetailResponse.java
│       ├── MyNoticeResponse.java
│       └── NoticeCategoryResponse.java
├── mapper/
│   ├── NoticeMapper.java
│   ├── NoticeCategoryMapper.java
│   ├── NoticeScopeMapper.java
│   └── NoticeUserMapper.java
├── service/
│   ├── NoticeService.java
│   ├── NoticeCategoryService.java
│   └── impl/
│       ├── NoticeServiceImpl.java
│       └── NoticeCategoryServiceImpl.java
└── controller/
    ├── NoticeController.java
    └── NoticeCategoryController.java
```

### 2. Entity 实体类

遵循现有实体类设计模式（参考 `DictType.java`、`User.java` 等）：

- `Notice.java` - 通知公告实体（使用 `@TableName("notice")`、`@TableField` 等注解）
- `NoticeCategory.java` - 通知分类实体
- `NoticeScope.java` - 通知发布范围实体
- `NoticeUser.java` - 用户通知关联实体

### 3. DTO 类

遵循现有 DTO 设计模式（参考 `CreateDictTypeRequest.java` 等）：

**Request DTO**：
- `NoticeListRequest.java` - 通知列表查询请求（分页、搜索、筛选参数）
- `CreateNoticeRequest.java` - 创建通知请求（使用 `@Valid`、`@NotBlank` 等验证注解）
- `UpdateNoticeRequest.java` - 更新通知请求
- `PublishNoticeRequest.java` - 发布通知请求（包含发布范围：部门IDs、角色IDs、用户IDs）
- `NoticeCategoryListRequest.java` - 通知分类列表查询请求
- `CreateNoticeCategoryRequest.java` - 创建通知分类请求
- `UpdateNoticeCategoryRequest.java` - 更新通知分类请求

**Response DTO**：
- `NoticeResponse.java` - 通知响应（列表展示）
- `NoticeListResponse.java` - 通知列表响应（扩展字段）
- `NoticeDetailResponse.java` - 通知详情响应（包含完整内容）
- `MyNoticeResponse.java` - 我的通知响应（包含已读状态）
- `NoticeCategoryResponse.java` - 通知分类响应

### 4. Service 层

遵循现有 Service 设计模式（参考 `DictTypeServiceImpl.java`）：

**NoticeCategoryService**：
- `getNoticeCategoryList()` - 获取分类列表（分页、搜索）
- `getNoticeCategoryById()` - 根据ID获取分类详情
- `createNoticeCategory()` - 创建分类（使用 `@Transactional`）
- `updateNoticeCategory()` - 更新分类（使用 `@Transactional`）
- `deleteNoticeCategory()` - 删除分类（逻辑删除，使用 `@Transactional`）

**NoticeService**：
- `getNoticeList()` - 获取通知列表（分页、搜索、筛选，管理员使用）
- `getNoticeById()` - 根据ID获取通知详情
- `createNotice()` - 创建通知（草稿状态，使用 `@Transactional`）
- `updateNotice()` - 更新通知（使用 `@Transactional`）
- `publishNotice()` - 发布通知（计算接收用户并创建 notice_user 记录，使用 `@Transactional`）
- `withdrawNotice()` - 撤回通知（使用 `@Transactional`）
- `deleteNotice()` - 删除通知（逻辑删除，使用 `@Transactional`）
- `getMyNotices()` - 获取我的通知列表（分页，用户端使用）
- `getUnreadCount()` - 获取未读通知数量（用户端使用，可缓存到Redis）
- `markAsRead()` - 标记已读（使用 `@Transactional`）
- `markAllAsRead()` - 全部标记为已读（使用 `@Transactional`）

**日志规范**（参考 [日志规范](../../.cursor/rules/logging.mdc)）：
- 关键操作必须记录日志（创建、更新、删除、发布、撤回）
- 日志必须包含业务标识（noticeId、userId等）
- 使用参数化日志，避免字符串拼接

### 5. Controller 层

遵循现有 Controller 设计模式（参考 `DictTypeController.java`）：

**NoticeCategoryController**：
- `GET /api/notice-categories` - 获取分类列表（分页、搜索、筛选）
- `GET /api/notice-categories/{id}` - 根据ID获取分类详情
- `POST /api/notice-categories` - 创建分类（使用 `@OperationLog` 注解）
- `PUT /api/notice-categories/{id}` - 更新分类（使用 `@OperationLog` 注解）
- `DELETE /api/notice-categories/{id}` - 删除分类（使用 `@OperationLog` 注解）

**NoticeController**：
- `GET /api/notices` - 获取通知列表（分页、搜索、筛选，管理员使用）
- `GET /api/notices/{id}` - 获取通知详情（管理员使用）
- `POST /api/notices` - 创建通知（草稿，使用 `@OperationLog` 注解）
- `PUT /api/notices/{id}` - 更新通知（使用 `@OperationLog` 注解）
- `POST /api/notices/{id}/publish` - 发布通知（使用 `@OperationLog` 注解）
- `POST /api/notices/{id}/withdraw` - 撤回通知（使用 `@OperationLog` 注解）
- `DELETE /api/notices/{id}` - 删除通知（使用 `@OperationLog` 注解）
- `GET /api/notices/my` - 获取我的通知列表（分页，用户端使用）
- `GET /api/notices/my/unread-count` - 获取未读通知数量（用户端使用）
- `GET /api/notices/{id}/detail` - 查看通知详情（自动标记已读，用户端使用）
- `PUT /api/notices/{id}/read` - 标记通知为已读（用户端使用）
- `PUT /api/notices/read-all` - 全部标记为已读（用户端使用）

**操作日志**：
- 所有修改操作（创建、更新、删除、发布、撤回）必须添加 `@OperationLog` 注解
- 参考现有 Controller 的 `@OperationLog` 使用方式

## 前端实现

### 1. 页面结构
- **通知分类管理**：`/system/notice/category`（参考字典类型管理页面）
- **通知公告管理（管理员）**：`/system/notice`（列表、创建/编辑、发布/撤回）
- **我的通知（用户端）**：`/account/notices` 或集成到个人中心
- **通知提醒**：顶部导航栏铃铛图标（对接真实API）

### 2. API 接口定义
在 `frontend/src/api/system.ts` 或新建 `frontend/src/api/notice.ts` 中定义所有接口。

### 3. 国际化支持
在 `frontend/locales/zh-CN.yaml` 和 `frontend/locales/en.yaml` 中添加菜单标题、页面标题等翻译。

## 技术要点

### 1. 发布范围计算

发布通知时，根据发布范围类型计算接收用户：
- **全部用户**：查询所有启用且未删除的用户
- **指定部门**：从 `notice_scope` 表获取部门IDs，查询这些部门的用户
- **指定角色**：从 `notice_scope` 表获取角色IDs，通过 `user_role` 表查询用户
- **指定用户**：直接从 `notice_scope` 表获取用户IDs

计算完成后，批量在 `notice_user` 表中为每个接收用户创建记录（初始状态为未读）。

### 2. 已读状态管理

- 用户首次查看通知详情时，自动标记为已读
- 支持批量标记已读
- 支持统计未读数量（用于顶部角标）

### 3. 性能优化

- 通知列表查询时，使用分页
- 未读数量可以缓存到 Redis（用户ID -> 未读数量）
- 发布通知时，批量插入 `notice_user` 记录

## 扩展功能（可选）

1. **通知推送**：集成 WebSocket 实现实时推送
2. **邮件通知**：重要通知可发送邮件提醒
3. **通知模板**：支持通知模板，快速创建
4. **通知统计**：查看通知的阅读率、阅读时间等统计信息
5. **定时发布**：支持定时发布通知

## 开发检查清单

参考 [功能开发规范](../../.cursor/rules/feature-development.mdc)：

### 1. 数据库迁移脚本

- [ ] 创建数据库迁移脚本（如 `V1.0.16__add_notice_tables.sql`）
- [ ] 包含所有表的创建语句
- [ ] 包含菜单数据的插入语句
- [ ] 包含角色菜单权限的分配语句

### 2. 菜单管理

- [ ] 在迁移脚本中添加"通知公告"菜单（系统管理下）
- [ ] 在迁移脚本中添加"通知分类"菜单（通知公告下）
- [ ] 为所有角色分配菜单权限
- [ ] 菜单标题使用 i18n key（如 `menus.systemNotice`）

### 3. 国际化

- [ ] 在 `frontend/locales/zh-CN.yaml` 中添加中文翻译
- [ ] 在 `frontend/locales/en.yaml` 中添加英文翻译
- [ ] 包含菜单标题、页面标题、按钮文本等

### 4. 操作日志

- [ ] 所有修改操作添加 `@OperationLog` 注解
- [ ] 记录关键业务标识（noticeId、categoryId等）
- [ ] 遵循日志规范（参考 [日志规范](../../.cursor/rules/logging.mdc)）

### 5. 代码规范

- [ ] 遵循 Java 代码规范（参考 [Java 代码规范](../../.cursor/rules/java-code-style.mdc)）
- [ ] 使用 import 语句，禁止全限定名
- [ ] 方法长度控制在 50 行以内
- [ ] 使用 `@Transactional` 注解保证事务一致性

## 开发优先级

1. **P0（核心功能）**：
   - 通知公告的CRUD
   - 发布范围控制（全部用户）
   - 已读状态管理
   - 用户查看通知
   - 菜单和权限配置

2. **P1（重要功能）**：
   - 通知分类管理
   - 发布范围（指定部门/角色/用户）
   - 顶部通知提醒
   - 国际化支持

3. **P2（扩展功能）**：
   - 富文本编辑器
   - 通知统计
   - 定时发布
   - Redis 缓存未读数量