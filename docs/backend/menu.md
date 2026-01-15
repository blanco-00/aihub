# 菜单管理模块设计

> 本文档包含菜单管理的数据库设计和功能设计。

## 📚 相关文档

- [功能清单](../features.md) - 查看功能规划与开发状态
- [后端开发指南](./guide.md) - 返回后端文档总览
- [数据库设计文档](./database.md) - 查看数据库设计规范
- [项目主文档](../../README.md) - 返回项目文档入口

## 数据库表设计

### 菜单表 (menu)

```sql
CREATE TABLE menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID，0表示顶级',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称（路由name）',
    path VARCHAR(200) NOT NULL COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径，父级为Layout',
    redirect VARCHAR(200) COMMENT '重定向路径',
    icon VARCHAR(50) COMMENT '图标',
    title VARCHAR(100) NOT NULL COMMENT '菜单标题（i18n key）',
    rank INT DEFAULT 0 COMMENT '排序',
    show_link TINYINT(1) DEFAULT 1 COMMENT '是否显示 0-隐藏 1-显示',
    keep_alive TINYINT(1) DEFAULT 0 COMMENT '是否缓存',
    status TINYINT(1) DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0,
    INDEX idx_parent_id (parent_id),
    INDEX idx_rank (rank),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';
```

### 角色菜单关联表 (role_menu)

```sql
CREATE TABLE role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';
```

## 关系设计

- **角色 ↔ 菜单**：多对多关系（一个角色可以访问多个菜单，一个菜单可以被多个角色访问）
- **菜单结构**：树形结构，通过 `parent_id` 实现多级菜单
- **动态路由**：后端 `/api/routes/async` 接口从数据库读取菜单，按角色过滤后返回

## 功能设计

### 菜单CRUD

- **菜单创建**：支持创建父级菜单和子菜单
- **菜单编辑**：修改菜单信息（路径、组件、图标等）
- **菜单删除**：逻辑删除，删除父菜单时检查子菜单
- **菜单排序**：通过 `rank` 字段控制显示顺序，支持拖拽排序

### 菜单权限

- **角色菜单关联**：通过 `role_menu` 表控制角色可见菜单
- **动态路由生成**：后端根据用户角色从数据库读取菜单，生成动态路由

### 菜单树结构

- **树形展示**：前端使用树形组件展示菜单层级
- **递归查询**：后端递归查询构建菜单树
- **拖拽排序**：支持拖拽调整菜单顺序

## 实现优先级

- **P0**：菜单CRUD、菜单树结构
- **P0**：角色菜单关联、动态路由生成（替换当前硬编码路由）
