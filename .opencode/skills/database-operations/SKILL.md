---
name: database-operations
description: Guides database operations in AIHub. Use when writing SQL queries, MyBatis Mapper XML, designing database tables, or optimizing query performance. Covers SQL optimization, naming conventions, MyBatis patterns, and performance best practices.
---

# Database Operations Skill

## What I Help With

### SQL Query Optimization
- Avoiding `SELECT *` and full table scans
- Using indexed fields in WHERE conditions
- Avoiding N+1 query problems
- Using UNION ALL with application-level deduplication
- Proper pagination implementation

### Database Design
- Table naming conventions (snake_case, avoiding reserved keywords)
- Field naming and data types
- Primary key and index design
- Foreign key relationships

### MyBatis Mapper Patterns
- XML query structure and best practices
- Parameter binding and result mapping
- Complex query patterns (JOIN, subqueries)
- Batch operations

### Performance Optimization
- Query execution time analysis
- Index usage monitoring
- Slow query identification (>500ms)
- Application-level vs database-level deduplication

## Quick Reference

### SQL Performance Rules
- ❌ NEVER use `SELECT *` - query only needed fields
- ❌ NEVER use `selectAll()` or `findAll()` - use pagination
- ❌ NEVER query in loops (N+1 problem) - use batch queries or JOIN
- ✅ ALWAYS use indexed fields in WHERE conditions
- ✅ ALWAYS use pagination for large datasets
- ✅ Use `UNION ALL` + application deduplication for better performance

### Naming Conventions
- **Tables**: snake_case (`user_role`, `login_log`)
- **Fields**: snake_case (`user_name`, `created_at`)
- **Avoid reserved keywords**: `rank`, `order`, `group`, `status`, `type` → `sort_order`, `display_order`, `group_name`, `is_deleted`, `record_type`

### Performance Benchmarks
- **Slow query**: >500ms - log warning
- **Very slow query**: >1000ms - log error and investigate
- **Batch operations**: Consider using batch inserts/updates
- **UNION ALL vs UNION**: UNION ALL is 3-5x faster

## Detailed References

For complete rules and examples, see:

- **[SQL Optimization](reference/sql-optimization.md)** - Query performance, indexing, JOIN patterns
- **[Database Design](reference/database-design.md)** - Table structure, naming conventions, data types
- **[MyBatis Patterns](reference/mybatis-patterns.md)** - XML mapper patterns, parameter binding
- **[Migration Guide](reference/migration-guide.md)** - Flyway migration structure and best practices
- **[Examples](examples/mapper-example.md)** - Complete MyBatis mapper XML examples

## Common Patterns

### Selective Query Pattern
```java
// ✅ Correct: Select only needed fields
List<User> users = userMapper.selectList(
    new LambdaQueryWrapper<User>()
        .select(User::getId, User::getUsername, User::getEmail)
        .eq(User::getStatus, 1)
        .orderByDesc(User::getCreatedAt)
);
```

### Pagination Pattern
```java
// ✅ Correct: Use pagination
Page<User> page = new Page<>(current, size);
Page<User> result = userMapper.selectPage(page,
    new LambdaQueryWrapper<User>()
        .eq(User::getStatus, 1)
);
```

### Batch Query to Avoid N+1
```java
// ❌ Wrong: Query in loop
for (Long userId : userIds) {
    User user = userMapper.selectById(userId);  // N+1 problem
}

// ✅ Correct: Batch query
List<User> users = userMapper.selectBatchIds(userIds);  // One query
```

### JOIN to Avoid N+1
```xml
<!-- ✅ Correct: Use JOIN -->
<select id="selectUsersWithRoles" resultType="com.aihub.entity.User">
    SELECT
        u.id, u.username, u.email,
        r.id as role_id, r.name as role_name
    FROM user u
    INNER JOIN user_role ur ON u.id = ur.user_id
    INNER JOIN role r ON ur.role_id = r.id
    WHERE u.is_deleted = 0 AND r.is_deleted = 0
</select>
```

### UNION ALL + Application Deduplication
```xml
<!-- SQL: Use UNION ALL (faster) -->
<select id="selectMenusByRoleCode" resultType="com.aihub.entity.Menu">
    SELECT id, name, parent_id FROM menu m
    INNER JOIN role_menu rm ON m.id = rm.menu_id
    WHERE rm.role_id = #{roleId}

    UNION ALL

    SELECT child.id, child.name, child.parent_id
    FROM menu child
    INNER JOIN (
        SELECT DISTINCT rm.menu_id
        FROM role_menu rm
        WHERE rm.role_id = #{roleId}
    ) parent_menus ON child.parent_id = parent_menus.menu_id
</select>
```

```java
// Java: Application-level deduplication
@Override
public List<MenuResponse> getMenuTreeByRoleCode(String roleCode) {
    List<Menu> menus = menuMapper.selectByRoleCode(roleCode);
    // Use LinkedHashSet to maintain order and deduplicate
    Set<Long> seenIds = new LinkedHashSet<>();
    List<Menu> distinctMenus = menus.stream()
        .filter(menu -> seenIds.add(menu.getId()))
        .collect(Collectors.toList());
    return buildMenuTree(distinctMenus, 0L);
}
```

## MyBatis Mapper XML Structure

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.aihub.admin.mapper.UserMapper">

    <select id="selectById" resultType="com.aihub.admin.entity.User">
        SELECT
            id, username, email, nickname, phone,
            status, role, department_id,
            avatar, description, remark,
            created_at, updated_at, is_deleted
        FROM user
        WHERE id = #{id}
          AND is_deleted = 0
    </select>

    <select id="selectUserList" resultType="com.aihub.admin.dto.response.UserListResponse">
        SELECT
            u.id, u.username, u.email, u.nickname, u.phone,
            u.role, u.status, d.name as department_name,
            u.created_at, u.updated_at
        FROM user u
        LEFT JOIN department d ON u.department_id = d.id
        WHERE u.is_deleted = 0
        <if test="keyword != null and keyword != ''">
            AND (u.username LIKE CONCAT('%', #{keyword}, '%')
                 OR u.email LIKE CONCAT('%', #{keyword}, '%')
                 OR u.nickname LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test="status != null">
            AND u.status = #{status}
        </if>
        <if test="departmentId != null">
            AND u.department_id = #{departmentId}
        </if>
        ORDER BY u.created_at DESC
        LIMIT #{size} OFFSET #{offset}
    </select>

</mapper>
```

## Flyway Migration Pattern

```sql
-- ============================================
-- Description: Add user role assignment feature
-- Version: V1.0.5
-- Author: Your Name
-- ============================================

-- Create user_role table
CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- Insert role_menu associations
INSERT INTO role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM role r
CROSS JOIN menu m
WHERE r.is_deleted = 0
  AND m.is_deleted = 0
  AND r.code = 'ADMIN'
  AND m.name = 'SystemDict'
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id), menu_id=VALUES(menu_id);
```

## Performance Optimization Checklist

Before deploying database changes, verify:

- [ ] No `SELECT *` queries
- [ ] WHERE conditions use indexed fields
- [ ] Pagination implemented for large datasets
- [ ] No N+1 query problems (use JOIN or batch queries)
- [ ] Table and field names avoid reserved keywords
- [ ] Appropriate indexes created on frequently queried columns
- [ ] UNION ALL used instead of UNION (with app-level deduplication)
- [ ] Query execution time logged and <500ms for normal operations
- [ ] Slow queries (>1000ms) identified and optimized
- [ ] Foreign key relationships properly defined

## Related Skills

- **[java-development](../java-development/)** - Java code that interacts with database
- **[feature-development](../feature-development/)** - Complete feature development including database migrations
