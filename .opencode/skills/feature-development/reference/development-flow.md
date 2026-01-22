# 开发流程

## 完整开发流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                    1. 需求分析                              │
│  - 理解功能需求                                            │
│  - 确定技术方案                                            │
│  - 评估影响范围                                            │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    2. 数据库设计                            │
│  - 设计表结构                                              │
│  - 创建迁移脚本 (Flyway)                                   │
│  - 添加菜单数据 (如需要)                                  │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    3. 后端开发                              │
│  - 创建 Entity                                             │
│  - 创建 Mapper                                             │
│  - 创建 Service/ServiceImpl                                  │
│  - 创建 Controller                                          │
│  - 添加日志和异常处理                                      │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    4. 前端开发                              │
│  - 创建 API 函数                                          │
│  - 创建页面组件 (Vue 3 + Composition API)                  │
│  - 配置路由                                               │
│  - 添加国际化翻译                                          │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    5. 配置菜单和权限                         │
│  - 确保菜单已在迁移脚本中添加                             │
│  - 验证菜单显示和路由跳转                                  │
│  - 验证权限控制                                            │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    6. 测试验证                               │
│  - 功能测试 (CRUD)                                        │
│  - 性能测试 (响应时间)                                      │
│  - 兼容性测试 (浏览器/分辨率)                                │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    7. 文档更新                               │
│  - 更新功能文档                                            │
│  - 更新 API 文档 (如需要)                                  │
│  - 更新开发规范 (如需要)                                    │
└─────────────────────────────────────────────────────────────────┘
```

## 需求分析

### 1.1 理解功能需求

- **功能目标**：这个功能要解决什么问题？
- **功能范围**：包含哪些子功能？不包含什么？
- **用户场景**：用户如何使用这个功能？

**示例：**
- 功能：用户管理
- 目标：管理系统用户，包括增删改查
- 场景：管理员可以添加新用户、编辑用户信息、禁用用户

### 1.2 确定技术方案

- **数据模型**：需要哪些表？表之间的关系？
- **API 设计**：需要哪些接口？接口路径和参数？
- **前端设计**：需要哪些页面？页面布局和交互？
- **配置需求**：是否需要菜单、权限、国际化？

**示例：**
- 数据模型：user 表、user_role 关联表
- API 设计：/api/users (GET), /api/users (POST), /api/users/{id} (PUT), /api/users/{id} (DELETE)
- 前端设计：用户列表页面、新增用户对话框
- 配置：用户管理菜单，用户管理权限

### 1.3 评估影响范围

- **现有功能影响**：是否会影响现有功能？
- **数据迁移需求**：是否需要迁移现有数据？
- **向后兼容性**：是否需要保持向后兼容？

## 数据库设计

### 2.1 设计表结构

#### 表名规范
- 使用 snake_case（小写下划线）
- 示例：`user`, `user_role`, `login_log`

#### 字段规范
- 使用 snake_case（小写下划线）
- 避免使用保留关键字
- 示例：`user_name`, `created_at`, `is_deleted`

#### 必需字段
```sql
CREATE TABLE `example_table` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识（0-未删除，1-已删除）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2.2 创建迁移脚本

#### 迁移脚本规范
- **文件名格式**：`V{major}.{minor}.{patch}__{description}.sql`
- **示例**：`V1.0.5__add_user_management.sql`

#### 迁移脚本结构
```sql
-- ============================================
-- Description: 添加用户管理功能
-- Version: V1.0.5
-- Author: Your Name
-- Date: 2026-01-20
-- ============================================

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `phone` VARCHAR(20) COMMENT '手机号',
    `role` VARCHAR(50) DEFAULT 'USER' COMMENT '主角色',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    `department_id` BIGINT COMMENT '部门ID',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识（0-未删除，1-已删除）',
    KEY `idx_username` (`username`),
    KEY `idx_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入菜单数据
INSERT INTO `menu` (`parent_id`, `name`, `path`, `component`, `icon`, `title`, `sort_order`, `show_link`, `keep_alive`, `status`)
VALUES (
    (SELECT id FROM (SELECT id FROM `menu` WHERE `name` = 'SystemManagement' LIMIT 1) AS tmp),
    'UserManagement',
    '/system/user/index',
    'system/user/index',
    'ri:user-line',
    'menus.pureUser',
    1,
    1,
    0,
    1
)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 为所有角色分配菜单权限
INSERT INTO `role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id
FROM `role` r
CROSS JOIN `menu` m
WHERE r.is_deleted = 0
  AND m.is_deleted = 0
  AND m.name = 'UserManagement'
ON DUPLICATE KEY UPDATE `role_id`=VALUES(`role_id`), `menu_id`=VALUES(`menu_id`);
```

## 后端开发

### 3.1 创建 Entity

```java
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String email;
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private String role;
    private Integer status;
    private Long departmentId;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
```

### 3.2 创建 Mapper

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询用户列表（分页、搜索、筛选）
     */
    List<UserListResponse> selectUserList(
        @Param("keyword") String keyword,
        @Param("phone") String phone,
        @Param("role") String role,
        @Param("status") Integer status,
        @Param("departmentId") Long departmentId,
        @Param("offset") Long offset,
        @Param("size") Integer size
    );

    /**
     * 统计用户总数
     */
    Long countUserList(
        @Param("keyword") String keyword,
        @Param("phone") String phone,
        @Param("role") String role,
        @Param("status") Integer status,
        @Param("departmentId") Long departmentId
    );

    /**
     * 根据用户名查询
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询
     */
    User findByEmail(@Param("email") String email);

    /**
     * 统计指定角色的用户数量
     */
    Long countByRoleAndNotDeleted(@Param("role") String role);
}
```

### 3.3 创建 Service 和 ServiceImpl

```java
// Service 接口
public interface UserService {
    PageResult<UserListResponse> getUserList(UserListRequest request);
    UserListResponse getUserById(Long id);
    void createUser(CreateUserRequest request);
    void updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
    void toggleUserStatus(Long id, Integer status);
    List<Long> getRoleIdsByUserId(Long userId);
    void assignUserRoles(Long userId, List<Long> roleIds);
}

// ServiceImpl 实现
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(CreateUserRequest request) {
        // 验证
        User existing = userMapper.findByUsername(request.getUsername());
        if (existing != null && existing.getIsDeleted() == 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // ...

        userMapper.insert(user);
        log.info("用户创建成功: username={}", user.getUsername());
    }
}
```

### 3.4 创建 Controller

```java
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<PageResult<UserListResponse>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword
    ) {
        UserListRequest request = new UserListRequest();
        request.setCurrent(current);
        request.setSize(size);
        request.setKeyword(keyword);

        PageResult<UserListResponse> result = userService.getUserList(request);
        return Result.success(result);
    }

    @OperationLog(module = "用户管理", operation = "创建用户", recordParams = true)
    @PostMapping
    public Result<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        userService.createUser(request);
        return Result.success();
    }
}
```

## 前端开发

### 4.1 创建 API 函数

```typescript
// frontend/src/api/user.ts
import { http } from "@/utils/http";

export interface UserInfo {
  id: number;
  username: string;
  email: string;
  nickname?: string;
  phone?: string;
  role: string;
  status: number;
  departmentId: number;
  createdAt: string;
  updatedAt: string;
}

export interface UserListRequest {
  current: number;
  size: number;
  keyword?: string;
  phone?: string;
  role?: string;
  status?: number;
  departmentId?: number;
}

export interface UserListResponse extends UserInfo {
  roleName?: string;
  departmentName?: string;
}

export function getUserList(params: UserListRequest): Promise<Result<PageResult<UserListResponse>>> {
  return http.request<Result<PageResult<UserListResponse>>>("GET", "/api/users", { params });
}

export function createUser(data: CreateUserRequest): Promise<Result<void>> {
  return http.request<Result<void>>("POST", "/api/users", { data });
}

export function updateUser(id: number, data: UpdateUserRequest): Promise<Result<void>> {
  return http.request<Result<void>>("PUT", `/api/users/${id}`, { data });
}

export function deleteUser(id: number): Promise<Result<void>> {
  return http.request<Result<void>>("DELETE", `/api/users/${id}`);
}
```

### 4.2 创建页面组件

```vue
<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>用户管理</span>
        <el-button type="primary" size="small" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>
    </template>

    <el-table v-loading="loading" :data="dataList" border>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="roleName" label="角色" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="form.current"
      v-model:page-size="form.size"
      :total="total"
      @current-change="fetchData"
      @size-change="fetchData"
    />
  </el-card>
</template>

<script setup lang="ts">
defineOptions({ name: "UserManagement" });

import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { getUserList, deleteUser } from "@/api/user";

const loading = ref(false);
const dataList = ref([]);
const total = ref(0);

const form = reactive({
  current: 1,
  size: 10,
  keyword: ""
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getUserList(form);
    dataList.value = res.data.records;
    total.value = res.data.total;
  } catch (error: any) {
    ElMessage.error("获取用户列表失败：" + error.message);
  } finally {
    loading.value = false;
  }
};

const handleAdd = () => {
  // 打开新增对话框
};

const handleEdit = (row: any) => {
  // 打开编辑对话框
};

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm("确定要删除该用户吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    });

    await deleteUser(row.id);
    ElMessage.success("删除成功");
    fetchData();
  } catch (error: any) {
    if (error !== "cancel") {
      ElMessage.error("删除失败：" + error.message);
    }
  }
};

onMounted(() => {
  fetchData();
});
</script>
```

### 4.3 配置路由

```typescript
// frontend/src/router/modules/user.ts
export default {
  path: "/system/user",
  name: "UserManagement",
  component: () => import("@/views/system/user/index.vue"),
  meta: {
    title: "menus.pureUser",
    icon: "ri:user-line",
    keepAlive: true,
    roles: ["ADMIN", "SUPER_ADMIN"]
  }
};
```

### 4.4 添加国际化

```yaml
# frontend/locales/zh-CN.yaml
menus:
  pureUser: "用户管理"

# frontend/locales/en.yaml
menus:
  pureUser: "User Management"
```
