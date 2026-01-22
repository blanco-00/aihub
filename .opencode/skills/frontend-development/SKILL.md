---
name: frontend-development
description: Guides Vue 3 + TypeScript frontend development in AIHub. Use when creating or modifying Vue components, pages, API calls, or UI elements. Covers code style, Element Plus usage, UI/UX design patterns, and interaction best practices.
---

# Frontend Development Skill

## What I Help With

### Vue 3 Component Development
- Creating Vue components with Composition API
- TypeScript type definitions and interfaces
- Component structure and organization
- Reactive state management with ref/reactive

### UI/UX Design
- Element Plus component usage
- Layout implementation with el-container/el-header/el-aside/el-main
- Color system usage (primary, success, warning, error)
- Spacing system (multiples of 4px: 8px, 12px, 16px, 24px)
- Loading states and user feedback

### API Integration
- API function creation and type definitions
- Error handling patterns
- Loading states and user notifications
- Token management and authentication

### Code Style
- Import organization (third-party → framework → internal)
- Formatting: 2-space indent, double quotes, semicolons
- Naming conventions (camelCase, PascalCase, UPPER_SNAKE_CASE)
- Type definitions (type vs interface)

## Quick Reference

### Naming Conventions
- **Files**: kebab-case (`user.ts`, `auth.ts`)
- **Variables/Functions**: camelCase (`getUserList`, `dataList`)
- **Constants**: UPPER_SNAKE_CASE (`userKey`, `TokenKey`)
- **Components**: PascalCase (`Guide`, `Auth`)
- **Types**: PascalCase (`UserInfo`, `CreateUserRequest`)
- **Handlers**: `handle` prefix (`handleClick`, `handleSearch`)
- **Booleans**: `is`, `has`, `can` prefix (`isRemembered`, `hasPerms`)

### Import Order
1. Third-party imports: `import dayjs from "dayjs"`
2. Framework imports: `import { ref } from "vue"`
3. Internal imports with `@/` alias:
   - `@/api/*` - API functions
   - `@/store/*` - Store modules
   - `@/utils/*` - Utilities
   - `@/components/*` - Components

### Formatting Rules
- **Indentation**: 2 spaces (no tabs)
- **Quotes**: Double quotes (`"string"`)
- **Semicolons**: Required after every statement
- **Line length**: < 120 characters

## Detailed References

For complete rules and examples, see:

- **[Code Style](reference/code-style.md)** - Import organization, naming conventions, formatting
- **[UI/UX Design](reference/ui-design.md)** - Layout, colors, spacing, Element Plus usage
- **[Component Patterns](reference/component-patterns.md)** - Composition API patterns, hooks, lifecycle
- **[Examples](examples/vue-component-example.md)** - Complete Vue component with API integration

## Common Patterns

### Vue Component Template
```vue
<script setup lang="ts">
defineOptions({ name: "ComponentName" });

import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";

const loading = ref(false);
const data = ref([]);

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await api.getData();
    data.value = res.data;
    ElMessage.success("操作成功");
  } catch (error: any) {
    ElMessage.error("操作失败: " + error.message);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <el-card>
    <el-button :loading="loading" @click="fetchData">加载数据</el-button>
    <div v-loading="loading">{{ data }}</div>
  </el-card>
</template>
```

### API Function Pattern
```typescript
export interface UserInfo {
  id: number;
  username: string;
  email: string;
}

export interface UserListResponse {
  records: UserInfo[];
  total: number;
}

export function getUserList(
  current: number,
  size: number
): Promise<Result<UserListResponse>> {
  return http.request<Result<UserListResponse>>("GET", "/api/users", {
    params: { current, size }
  });
}
```

### Pinia Store Pattern
```typescript
import { defineStore } from "pinia";

export const useUserStore = defineStore("pure-user", {
  state: () => ({
    username: "",
    roles: [],
    permissions: []
  }),
  actions: {
    SET_USERNAME(username: string) {
      this.username = username;
    },
    SET_ROLES(roles: Array<string>) {
      this.roles = roles;
    }
  }
});
```

## Element Plus Components

### Commonly Used
- **Layout**: `el-container`, `el-header`, `el-aside`, `el-main`
- **Form**: `el-form`, `el-input`, `el-select`, `el-button`
- **Data Display**: `el-table`, `el-card`
- **Feedback**: `el-message`, `el-notification`, `el-alert`
- **Navigation**: `el-menu`, `el-breadcrumb`
- **Dialog**: `el-dialog`, `el-drawer`

### Loading States
- Page-level: `v-loading` directive
- Button: `el-button` `loading` attribute
- Table: `el-table` `v-loading` directive
- Skeleton: `el-skeleton` component

## Error Handling Pattern
```typescript
try {
  const response = await api.getData();
  if (response.code === 200) {
    dataList.value = response.data;
    ElMessage.success("操作成功");
  }
} catch (error: any) {
  console.error("Error description", error);
  ElMessage.error("操作失败: " + error.message);
}
```

## Checklist

Before committing frontend code, verify:

- [ ] Imports organized correctly (third-party → framework → internal)
- [ ] 2-space indentation, double quotes, semicolons
- [ ] Component name defined with `defineOptions()`
- [ ] Loading states for all async operations
- [ ] Error handling with try/catch and user notifications
- [ ] Element Plus components used (no custom implementations)
- [ ] Spacing in multiples of 4px
- [ ] Type definitions exported at module level
- [ ] Boolean variables use `is`/`has`/`can` prefix
- [ ] Handlers use `handle` prefix

## Related Skills

- **[java-development](../java-development/)** - Backend API integration
- **[feature-development](../feature-development/)** - Complete feature development workflow
