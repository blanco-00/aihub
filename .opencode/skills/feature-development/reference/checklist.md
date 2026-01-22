# 功能开发检查清单

## 完整检查清单

在提交代码前，确保以下所有项目都已完成。

### 数据库（Database）

#### 表结构设计
- [ ] 表名使用 snake_case（小写下划线）
- [ ] 字段名使用 snake_case
- [ ] 字段名不使用保留关键字（如 `rank`, `order`, `group`）
- [ ] 主键设计正确（通常为 `id BIGINT AUTO_INCREMENT`）
- [ ] 外键关系设计合理
- [ ] 索引创建在常用查询字段上
- [ ] 必需字段已添加（`created_at`, `updated_at`, `is_deleted`）

#### 迁移脚本
- [ ] 迁移脚本文件名格式正确（`V1.0.X__description.sql`）
- [ ] 迁移脚本放置在正确位置（`backend/aihub-admin/src/main/resources/db/migration/`）
- [ ] 建表语句使用 `IF NOT EXISTS`
- [ ] 菜单 INSERT 语句包含在迁移脚本中
- [ ] 菜单使用 `ON DUPLICATE KEY UPDATE` 处理重复
- [ ] 角色菜单权限已分配（`INSERT INTO role_menu`）
- [ ] 菜单 name 使用驼峰命名（如 `UserManagement`）
- [ ] 菜单 title 使用 i18n key（如 `menus.pureUser`）

#### SQL 优化
- [ ] 没有使用 `SELECT *`
- [ ] 查询语句只列出需要的字段
- [ ] WHERE 条件使用了索引字段
- [ ] 避免 N+1 查询（使用 JOIN 或批量查询）

### 后端（Backend）

#### Entity
- [ ] `@TableName` 注解正确
- [ ] `@TableId` 注解正确
- [ ] 字段映射正确（`@TableField`）
- [ ] 逻辑删除字段使用 `@TableLogic`
- [ ] 字段类型与数据库类型匹配

#### Mapper
- [ ] `@Mapper` 注解存在
- [ ] XML 文件路径正确
- [ ] 查询方法命名清晰（`selectByXxx`, `countByXxx`）
- [ ] XML 查询语句优化（不使用 `SELECT *`）
- [ ] 参数绑定正确（`#{param}` 或 `${param}`）

#### Service
- [ ] `@Service` 注解存在
- [ ] Service 接口和实现类命名规范（`UserService`, `UserServiceImpl`）
- [ ] `@Autowired` 依赖注入正确
- [ ] 写操作使用 `@Transactional(rollbackFor = Exception.class)`
- [ ] 使用 `BusinessException` 抛出业务异常
- [ ] 异常消息清晰准确，使用中文
- [ ] 关键操作添加日志（`log.info`, `log.error`）
- [ ] 日志包含业务标识符（如 userId, menuId）
- [ ] 敏感信息不记录日志（密码、token）

#### Controller
- [ ] `@RestController` 注解存在
- [ ] `@RequestMapping` 或 `@GetMapping/@PostMapping` 正确
- [ ] 参数验证使用 `@Valid` 注解
- [ ] 返回类型统一使用 `Result<T>`
- [ ] `@OperationLog` 注解添加到关键操作
- [ ] 方法命名规范（`getUserList`, `createUser`, `updateUser`, `deleteUser`）

#### 代码质量
- [ ] 方法长度 < 50 行（特殊情况不超过 100）
- [ ] 嵌套层级 < 3 层
- [ ] 使用 Early Return 减少嵌套
- [ ] 代码简洁易懂，无过度设计
- [ ] 没有使用全限定名（所有类都已 import）
- [ ] 命名规范（类用 PascalCase，方法用 camelCase）

### 前端（Frontend）

#### API 函数
- [ ] API 函数放在正确位置（`frontend/src/api/`）
- [ ] TypeScript 类型定义正确（`interface` 或 `type`）
- [ ] 请求方法正确（GET/POST/PUT/DELETE）
- [ ] 参数类型定义清晰
- [ ] 返回类型定义清晰

#### 组件
- [ ] `<script setup lang="ts">` 正确
- [ ] `defineOptions({ name: "ComponentName" })` 存在
- [ ] imports 按分组顺序排列（third-party → framework → internal）
- [ ] 使用 Composition API（`ref`, `reactive`, `computed`）
- [ ] 使用 `onMounted`/`onUnmounted` 等生命周期钩子

#### 代码格式
- [ ] 2 spaces 缩进（不使用 tabs）
- [ ] 双引号（不使用单引号）
- [ ] 语句后有分号
- [ ] 行长度 < 120 字符
- [ ] 多行对象/数组有尾随逗号

#### 命名规范
- [ ] Files 使用 kebab-case（`user.ts`）
- [ ] Variables/Functions 使用 camelCase（`getUserList`, `dataList`）
- [ ] Constants 使用 UPPER_SNAKE_CASE（`userKey`, `TokenKey`）
- [ ] Components 使用 PascalCase（`UserManagement`）
- [ ] Types 使用 PascalCase（`UserInfo`, `CreateUserRequest`）
- [ ] Handlers 使用 `handle` 前缀（`handleClick`, `handleDelete`）
- [ ] Booleans 使用 `is`/`has`/`can` 前缀（`isLoading`, `hasData`）

#### UI/UX
- [ ] 使用 Element Plus 组件（不自己实现）
- [ ] 使用 Element Plus 布局组件（`el-container`, `el-header`, `el-aside`, `el-main`）
- [ ] 配色使用 Element Plus 主题色（`type="primary"`, `type="success"`）
- [ ] 间距使用 4px 的倍数（8px, 12px, 16px, 24px, 32px）
- [ ] 异步操作有加载状态（`loading` 变量或 `v-loading`）
- [ ] 成功操作有提示（`ElMessage.success()`）
- [ ] 错误操作有提示（`ElMessage.error()`）
- [ ] 危险操作有二次确认（`ElMessageBox.confirm()`）

#### 错误处理
- [ ] 异步操作使用 try/catch
- [ ] 错误日志输出（`console.error`）
- [ ] 用户友好提示（`ElMessage.error("错误描述: " + error.message)`）

#### 国际化
- [ ] 菜单 title 使用 i18n key
- [ ] `zh-CN.yaml` 中添加中文翻译
- [ ] `en.yaml` 中添加英文翻译

### 配置（Configuration）

#### 路由配置
- [ ] 路由文件在正确位置（`frontend/src/router/modules/`）
- [ ] 路由 path 正确
- [ ] 路由 name 使用驼峰命名（`UserManagement`）
- [ ] meta 信息完整（`title`, `icon`, `keepAlive`, `roles`）

#### 菜单显示
- [ ] 菜单数据已在迁移脚本中添加
- [ ] 刷新页面后菜单显示正常
- [ ] 点击菜单后路由跳转正常

#### 权限控制
- [ ] 菜单权限正确分配给角色
- [ ] 权限控制生效（无权限用户看不到菜单/按钮）

### 测试（Testing）

#### 功能测试
- [ ] 创建操作正常
- [ ] 读取操作正常
- [ ] 更新操作正常
- [ ] 删除操作正常
- [ ] 搜索功能正常
- [ ] 分页功能正常
- [ ] 表单验证正常
- [ ] 错误处理正常（显示错误提示）

#### 性能测试
- [ ] 查询响应时间 <500ms
- [ ] 大数据量（>1000 条）分页正常
- [ ] 慢查询已优化（>500ms 有警告日志）

#### 兼容性测试
- [ ] Chrome 浏览器测试通过
- [ ] Firefox 浏览器测试通过
- [ ] Safari 浏览器测试通过（如适用）
- [ ] 1920x1080 分辨率测试通过
- [ ] 1366x768 分辨率测试通过
- [ ] 移动端响应式测试通过（如适用）

### 文档（Documentation）

#### 功能文档
- [ ] 功能说明已添加到 `docs/features.md`
- [ ] 或已创建独立功能文档
- [ ] 文档包含功能描述、使用方式、截图（如需要）

#### API 文档
- [ ] API 接口说明已更新（如项目有 API 文档）
- [ ] 接口路径、参数、返回值说明清晰

#### 开发规范
- [ ] 如有新的开发规范，已更新到 AGENTS.md
- [ ] 代码示例已更新到相关文档

### 代码清理（Code Cleanup）

- [ ] 删除未使用的代码（类、方法、变量）
- [ ] 删除注释代码
- [ ] 删除调试代码（`console.log`, `debugger`）
- [ ] 删除无用的 imports

## 快速检查清单（开发时使用）

在开发过程中，可以快速检查以下关键项：

### 开始编码前
- [ ] 功能需求已理解
- [ ] 技术方案已确定
- [ ] 表结构已设计

### 数据库开发后
- [ ] 迁移脚本已创建
- [ ] 菜单数据已添加
- [ ] SQL 已优化

### 后端开发后
- [ ] Entity 已创建
- [ ] Mapper 已创建
- [ ] Service 已创建
- [ ] Controller 已创建
- [ ] 日志已添加
- [ ] 异常处理完善

### 前端开发后
- [ ] API 函数已创建
- [ ] 页面组件已创建
- [ ] 路由已配置
- [ ] 国际化已添加

### 提交代码前
- [ ] 所有测试通过
- [ ] 代码符合规范
- [ ] 文档已更新
- [ ] 未使用的代码已删除
