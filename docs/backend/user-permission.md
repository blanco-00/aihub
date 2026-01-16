# 用户与权限模块设计

> 本文档包含用户管理、角色管理、部门管理的数据库设计和功能设计。

## 📚 相关文档

- [功能清单](../features.md) - 查看功能规划与开发状态
- [后端开发指南](./guide.md) - 返回后端文档总览
- [数据库设计文档](./database.md) - 查看数据库设计规范
- [项目主文档](../../README.md) - 返回项目文档入口

## 核心关系设计

### 用户-角色-部门关系

- **用户 ↔ 角色**：多对多（`user_role` 关联表）
- **用户 ↔ 部门**：多对一（`user.department_id` 字段）
- **部门结构**：树形结构（`parent_id`）

## 数据库表设计

> 📝 **说明**：完整的SQL表结构定义请参考后端迁移脚本：
> - `backend/aihub-api/src/main/resources/db/migration/V1.0.0__init_tables.sql` - 基础表结构
> - `backend/aihub-api/src/main/resources/db/migration/V1.0.5__add_department_table.sql` - 部门表
> - `backend/aihub-api/src/main/resources/db/migration/V1.0.8__add_department_id_to_user.sql` - 用户表添加部门关联字段

### 用户表扩展

在现有 `user` 表基础上添加部门关联字段 `department_id`，用于关联部门表。

### 角色表 (role)

**主要字段**：
- `id`: 角色ID（主键）
- `code`: 角色代码（SUPER_ADMIN/ADMIN/USER），唯一
- `name`: 角色名称
- `description`: 角色描述
- `status`: 状态（0-禁用，1-启用）
- `is_deleted`: 是否删除（0-未删除，1-已删除）

### 用户角色关联表 (user_role)

**主要字段**：
- `id`: 关联ID（主键）
- `user_id`: 用户ID
- `role_id`: 角色ID
- `created_at`: 创建时间

**唯一约束**：`(user_id, role_id)` 唯一，防止重复关联

### 部门表 (department)

**主要字段**：
- `id`: 部门ID（主键）
- `name`: 部门名称
- `parent_id`: 父部门ID，0表示顶级部门
- `sort_order`: 排序（注意：使用 `sort_order` 而非 `rank`，避免保留关键字）
- `status`: 状态（0-禁用，1-启用）
- `is_deleted`: 是否删除（0-未删除，1-已删除）

## 功能设计

### 角色管理

- **角色CRUD**：创建、查询、更新、删除角色
- **用户角色分配**：为用户分配角色，支持多角色
- **角色权限配置**：通过 `role_menu` 表控制角色可见菜单

### 部门管理

- **部门CRUD**：支持树形结构的部门管理
- **部门人员管理**：查看部门下的用户列表
- **部门排序**：通过 `sort_order` 字段控制显示顺序

### 用户管理（已实现）

- **用户CRUD**：已完成，参考 [功能清单](../features.md#用户与权限)
- **用户角色**：当前支持单角色，多角色功能待实现
- **用户部门**：待实现部门关联

## 实现优先级

1. **角色管理**（P0）：角色CRUD，用户角色关联表
2. **部门管理**（P1）：部门树形结构，用户部门关联
3. **多角色支持**（P1）：用户可拥有多个角色
