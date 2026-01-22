# Frontend Code Style

## Imports Organization

### Import 分组顺序

1. **Third-party imports**: 第三方库导入
   ```typescript
   import dayjs from "dayjs";
   import Cookies from "js-cookie";
   import { ElMessage } from "element-plus";
   ```

2. **Framework imports**: 框架核心导入
   ```typescript
   import { ref, reactive, computed, onMounted } from "vue";
   import { useRouter } from "vue-router";
   import { defineStore } from "pinia";
   ```

3. **Internal imports with `@/` alias**: 内部导入，按来源分组
   ```typescript
   // @/api/* - API functions
   import { getUserList, createUser } from "@/api/user";

   // @/store/* - Store modules
   import { useUserStoreHook } from "@/store/modules/user";

   // @/utils/* - Utilities
   import { getToken, setToken } from "@/utils/auth";

   // @/components/* - Components
   import { ReIcon } from "@/components/ReIcon";
   ```

### Import 分组规则

- 每组 imports 之间要有**空行**
- 每组内部按**字母顺序**排列
- 使用**命名导入**（named imports）优先

### 正确示例
```typescript
// Third-party imports
import dayjs from "dayjs";
import Cookies from "js-cookie";
import { ElMessage } from "element-plus";

// Framework imports
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";

// Internal imports
import { getUserList } from "@/api/user";
import { useUserStoreHook } from "@/store/modules/user";
import { getToken, hasPerms } from "@/utils/auth";
import { ReIcon } from "@/components/ReIcon";
```

### 错误示例
```typescript
// ❌ 错误：没有分组，混乱的 import
import { ref } from "vue";
import dayjs from "dayjs";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { getUserList } from "@/api/user";
```

## Formatting 规则

### Indentation（缩进）
- **2 spaces**（2个空格）
- ❌ 不使用 tabs（制表符）

### Quotes（引号）
- **Double quotes**（双引号）
- 示例：`"string"`, `"path/to/file"`

### Semicolons（分号）
- **Required**（必需）
- 每个语句后必须加分号

### Trailing Commas（尾随逗号）
- **Yes**（使用）
- 多行对象/数组中，最后一项后面加逗号

### Line Length（行长度）
- **< 120 characters**（小于 120 个字符）
- 超长行应该在运算符后换行

### 正确示例
```typescript
// ✅ 正确：2 spaces, double quotes, semicolons
const userList = ref([]);
const loading = ref(false);

// ✅ 正确：trailing commas in multi-line objects
const user = {
  id: 1,
  username: "test",
  email: "test@example.com",
};

// ✅ 正确：long line break
const result = await api.getData()
  .then(data => {
    // ...
  })
  .catch(error => {
    // ...
  });
```

### 错误示例
```typescript
// ❌ 错误：使用 tabs
	const userList = ref([]);

// ❌ 错误：单引号
const username = 'test';

// ❌ 错误：缺少分号
const email = "test@example.com"

// ❌ 错误：多行对象缺少尾随逗号
const user = {
  id: 1,
  username: "test"
};

// ❌ 错误：行太长
const result = await api.getData().then(data => processData(data)).catch(error => handleError(error));
```

## 类型定义

### type vs interface

- **Prefer `type` over `interface`**（优先使用 `type`）
- `type` 适合：联合类型、交叉类型、基本类型别名
- `interface` 适合：对象类型、可扩展的类型

### Generic Types with Defaults

```typescript
// ✅ 正确：generic type with default
type Result<T = any> = {
  code: number;
  message: string;
  data: T;
};

type PageResult<T = any> = {
  records: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
};
```

### Export Types at Module Level

```typescript
// ✅ 正确：在模块级别导出类型
export interface UserInfo {
  id: number;
  username: string;
  email: string;
}

export type CreateUserRequest = {
  username: string;
  password: string;
  email: string;
};

export function createUser(data: CreateUserRequest): Promise<Result<UserInfo>> {
  // ...
}
```

### JSDoc Comments

```typescript
// ✅ 正确：使用 JSDoc 注释
/**
 * 用户信息接口
 */
export interface UserInfo {
  /** 用户ID */
  id: number;
  /** 用户名 */
  username: string;
  /** 邮箱 */
  email: string;
}
```

## 命名约定

### Files（文件）
- **kebab-case**（短横线命名）
- 示例：`user.ts`, `auth.ts`, `hook.tsx`, `index.vue`

### Variables/Functions（变量/函数）
- **camelCase**（小写驼峰命名）
- 示例：`getUserList`, `dataList`, `handleSizeChange`, `isLoading`

### Constants（常量）
- **UPPER_SNAKE_CASE**（全大写下划线）
- 示例：`userKey`, `TokenKey`, `MAX_PAGE_SIZE`, `DEFAULT_PAGE_SIZE`

### Components（组件）
- **PascalCase**（大写驼峰命名）
- 示例：`Guide`, `Auth`, `Perms`, `UserList`, `LoginForm`

### Types（类型）
- **PascalCase**（大写驼峰命名）
- 示例：`UserInfo`, `CreateUserRequest`, `PageResult`, `UserListResponse`

### Handlers（处理器）
- **`handle` prefix**（handle 前缀）
- 示例：`handleClick`, `handleSearch`, `handleSubmit`, `handleClose`

### Booleans（布尔值）
- **`is`, `has`, `can` prefix**（is/has/can 前缀）
- 示例：`isRemembered`, `hasPerms`, `canEdit`, `isLoading`, `hasData`

### Actions（Pinia Store Actions）
- **`SET_` prefix for mutations**（mutation 使用 SET_ 前缀）
- 示例：`SET_USERNAME`, `SET_ROLES`, `SET_PERMS`, `SET_AVATAR`

## 检查清单

提交代码前，检查：

- [ ] Imports 按分组顺序排列（third-party → framework → internal）
- [ ] 各组 imports 之间有空行
- [ ] 2 spaces indentation（不使用 tabs）
- [ ] Double quotes（不使用单引号）
- [ ] Semicolons after every statement
- [ ] Trailing commas in multi-line objects/arrays
- [ ] Line length < 120 characters
- [ ] `type` 优先于 `interface`（除非需要扩展）
- [ ] Types 在模块级别导出
- [ ] Files 使用 kebab-case
- [ ] Variables/Functions 使用 camelCase
- [ ] Constants 使用 UPPER_SNAKE_CASE
- [ ] Components 使用 PascalCase
- [ ] Types 使用 PascalCase
- [ ] Handlers 使用 `handle` 前缀
- [ ] Booleans 使用 `is`/`has`/`can` 前缀
