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

### 用户表扩展

在现有 `user` 表基础上添加部门关联：

```sql
-- 添加部门关联字段
ALTER TABLE user ADD COLUMN department_id BIGINT COMMENT '部门ID';
ALTER TABLE user ADD INDEX idx_department_id (department_id);
```

### 角色表 (role)

```sql
CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代码 SUPER_ADMIN/ADMIN/USER',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    status TINYINT(1) DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0,
    INDEX idx_role_code (code),
    INDEX idx_role_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
```

### 用户角色关联表 (user_role)

```sql
CREATE TABLE user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
```

### 部门表 (department)

```sql
CREATE TABLE department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID，0表示顶级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT(1) DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0,
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';
```

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
