# AGENTS.md - Coding Guidelines for AIHub

This file provides essential information for agentic coding agents working on the AIHub codebase.

---

## Build Commands

### Frontend (Vue 3 + TypeScript)
```bash
cd frontend

# Development (hot reload, port 3000)
pnpm dev

# Production build
pnpm build

# Staging build
pnpm build:staging

# Preview production build
pnpm preview

# Type checking
pnpm typecheck  # tsc + vue-tsc

# Linting
pnpm lint             # Run all linters (eslint + prettier + stylelint)
pnpm lint:eslint      # ESLint on {src,mock,build}/**/*.{vue,js,ts,tsx}
pnpm lint:prettier    # Prettier format
pnpm lint:stylelint   # CSS/SCSS linting
```

**Note**: No testing framework is configured. Use `pnpm typecheck` for type validation.

### Backend (Java 17 + Spring Boot)
```bash
cd backend

# Run main application (with hot reload)
cd aihub-api && mvn spring-boot:run

# Build all modules
mvn clean package

# Run tests
mvn test

# Skip tests during build
mvn clean package -DskipTests
```

---

## Code Style Guidelines

### Frontend (TypeScript/Vue)

#### Imports Organization
1. Third-party imports (`import dayjs from "dayjs"`)
2. Framework imports (`import { ref } from "vue"`)
3. Internal imports with `@/` alias, grouped by source:
   - `@/api/*` - API functions
   - `@/store/*` - Store modules
   - `@/utils/*` - Utilities
   - `@/components/*` - Components
4. Blank lines between groups

#### Formatting
- **Indentation**: 2 spaces (no tabs)
- **Quotes**: Double quotes (`"string"`)
- **Semicolons**: Required after every statement
- **Trailing commas**: Yes in multi-line objects/arrays
- **Line length**: < 120 characters

#### Types
- Prefer `type` over `interface`
- Generic types with defaults: `type Result<T = any>`
- Export types at module level
- JSDoc comments: `/** Type description */`

#### Naming Conventions
- **Files**: kebab-case (`user.ts`, `auth.ts`)
- **Variables/Functions**: camelCase (`getUserList`, `dataList`)
- **Constants**: UPPER_SNAKE_CASE (`userKey`, `TokenKey`)
- **Components**: PascalCase (`Guide`, `Auth`)
- **Types**: PascalCase (`UserInfo`, `CreateUserRequest`)
- **Handlers**: `handle` prefix (`handleClick`, `handleSearch`)
- **Booleans**: `is`, `has`, `can` prefix (`isRemembered`, `hasPerms`)

#### Error Handling
```typescript
try {
  const response = await api.getData();
  if (response.code === 200) {
    dataList.value = response.data;
  }
} catch (error: any) {
  console.error("Error description", error);
  message("Error message", { type: "error" });
}
```

#### Component Patterns
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
```

#### UI/UX Guidelines
- **Use Element Plus** components exclusively (no custom implementations)
- **Layout**: Use `el-container`, `el-header`, `el-aside`, `el-main`
- **Colors**: Use Element Plus type attributes (`type="primary"`, `type="success"`)
- **Spacing**: Multiples of 4px (8px, 12px, 16px, 24px, 32px)
- **Loading**: Always show loading states for async operations
- **Confirmation**: Use `ElMessageBox.confirm()` for dangerous actions
- **Tech aesthetic**: Reference vue-pure-admin style for modern design

---

### Backend (Java)

#### Import Rules
- **NEVER use fully qualified names** (e.g., `com.aihub.entity.User`)
- Always use `import` statements at the top of files

#### Code Style
- **Clean & readable**: Meaningful names, avoid abbreviations
- **Method length**: < 50 lines (max 100)
- **Nesting**: < 3 levels, use early return
- **Pragmatic**: Simple solutions, avoid over-engineering
- **Reuse**: Extract common code into utility methods/classes

#### Naming Conventions
- **Classes**: PascalCase (`UserService`, `UserServiceImpl`)
- **Methods**: camelCase with action verbs (`getUserById`, `createUser`)
- **Variables**: camelCase (`userName`, `totalCount`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`)
- **Packages**: lowercase (`com.aihub.admin.controller`)

#### Logging (SLF4J)
- **Levels**: DEBUG (dev), INFO (key operations), WARN (issues), ERROR (exceptions)
- **Structured**: Include business identifiers (userId, menuId, requestId)
- **No repetition**: Avoid same log in loops
- **Performance**: Log duration for slow operations (>500ms)

```java
// Correct
log.info("User login success: userId={}, username={}", userId, username);
log.error("Create user failed: userId={}, error={}", userId, e.getMessage(), e);
log.warn("Slow query: duration={}ms, count={}", duration, count);

// Incorrect
log.info("User login success");  // Missing context
for (User user : users) {
    log.info("Processing: {}", user);  // Duplicates in loop
}
```

#### Error Handling
- Use `BusinessException` for business errors
- Throw exceptions with clear messages
- Use `@Transactional(rollbackFor = Exception.class)` for write operations

```java
@Transactional(rollbackFor = Exception.class)
public void createUser(CreateUserRequest request) {
    User existing = userMapper.findByUsername(request.getUsername());
    if (existing != null && existing.getIsDeleted() == 0) {
        throw new BusinessException("用户名已存在");
    }
    // ... rest of implementation
}
```

#### Service Layer
- Interface in `com.aihub.admin.service`
- Implementation in `com.aihub.admin.service.impl`
- Use `@Autowired` for dependency injection
- Annotate with `@Service`

---

## Important Notes

### Testing
- **No test framework configured** for frontend
- Use `pnpm typecheck` to verify TypeScript types
- Backend has Spring Boot Test configured (`mvn test`)

### Code Cleanup
- **Delete unused code** (classes, methods, commented code)
- No long-term遗留废弃代码

### Documentation
- Code features should have corresponding docs in `docs/`
- README.md is the single documentation entry point
- Cursor rules in `.cursor/rules/` contain additional guidelines

### Technology Stack
- **Frontend**: Vue 3, TypeScript, Vite, Element Plus, Pinia
- **Backend**: Java 17, Spring Boot 3.2.0, MySQL 8.0, Redis, MyBatis Plus
- **Package Manager**: pnpm (frontend), Maven (backend)
- **Node.js**: 20.19.0+ or 22.13.0+
- **Java**: 17+
