---
name: feature-development
description: Guides complete feature development workflow in AIHub. Use when developing new features from scratch. Covers database design, backend API development, frontend UI implementation, menu/permission configuration, and documentation.
---

# Feature Development Skill

## 我能帮助你做什么

### 完整的功能开发流程
从需求分析到部署上线的完整功能开发流程，包括：
- 数据库设计和迁移
- 后端 API 开发（Controller, Service, Mapper）
- 前端页面和组件开发
- 菜单和权限配置
- 文档更新

### 功能开发检查清单
确保新功能开发完成后，所有必要的步骤都已完成。

## 快速参考

### 功能开发流程

```
1. 需求分析 → 2. 数据库设计 → 3. 后端开发 → 4. 前端开发 → 5. 配置菜单 → 6. 测试验证 → 7. 文档更新
```

### 关键步骤

- **数据库**: 创建迁移脚本（Flyway）
- **后端**: Entity → Mapper → Service → Controller
- **前端**: API → 组件 → 页面 → 路由
- **配置**: 菜单数据、角色权限、国际化
- **测试**: 功能测试、性能测试、兼容性测试
- **文档**: 更新功能文档、API 文档

## 详细参考

完整的规则和示例，请查看：

- **[Development Flow](reference/development-flow.md)** - 完整的开发流程和步骤
- **[Checklist](reference/checklist.md)** - 功能开发检查清单
- **[Examples](examples/complete-feature-example.md)** - 完整功能开发示例

## 功能开发流程

### Step 1: 需求分析

1. **理解需求**
   - 功能目标和范围
   - 涉及的模块
   - 数据流转逻辑

2. **确定技术方案**
   - 需要新建哪些表
   - 需要哪些 API 接口
   - 需要哪些前端页面
   - 是否需要菜单和权限

3. **评估影响范围**
   - 是否影响现有功能
   - 是否需要数据迁移
   - 是否需要向后兼容

### Step 2: 数据库设计

参考：**[java-development](../java-development/)** 和 **[database-operations](../database-operations/)**

1. **设计表结构**
   - 表名（snake_case）
   - 字段（snake_case，避免保留关键字）
   - 主键和外键
   - 索引设计

2. **创建迁移脚本**
   - 版本号：`V1.0.X__description.sql`
   - 位置：`backend/aihub-admin/src/main/resources/db/migration/`

3. **添加菜单数据**（如需要）
   - 菜单 INSERT 语句
   - 角色菜单权限分配
   - 国际化支持

### Step 3: 后端开发

参考：**[java-development](../java-development/)**

1. **创建 Entity**
   ```java
   @Data
   @TableName("feature_table")
   public class Feature {
       @TableId(type = IdType.AUTO)
       private Long id;
       // ...
   }
   ```

2. **创建 Mapper**
   ```java
   @Mapper
   public interface FeatureMapper extends BaseMapper<Feature> {
       // 自定义查询方法
   }
   ```

3. **创建 Service 和 ServiceImpl**
   ```java
   public interface FeatureService {
       PageResult<Feature> getList(Request request);
       void create(CreateRequest request);
       // ...
   }

   @Service
   public class FeatureServiceImpl implements FeatureService {
       @Autowired
       private FeatureMapper featureMapper;

       // 实现方法
   }
   ```

4. **创建 Controller**
   ```java
   @RestController
   @RequestMapping("/api/feature")
   public class FeatureController {
       @Autowired
       private FeatureService featureService;

       @GetMapping
       public Result<PageResult<Feature>> getList(...) { }

       @PostMapping
       public Result<Void> create(@Valid @RequestBody CreateRequest request) { }
       // ...
   }
   ```

### Step 4: 前端开发

参考：**[frontend-development](../frontend-development/)**

1. **创建 API 函数**
   ```typescript
   export interface FeatureRequest { ... }
   export interface FeatureResponse { ... }

   export function getFeatureList(params: FeatureRequest): Promise<Result<PageResult<FeatureResponse>>> {
     return http.request("GET", "/api/feature", { params });
   }
   ```

2. **创建页面组件**
   ```vue
   <script setup lang="ts">
   defineOptions({ name: "FeaturePage" });

   import { ref, onMounted } from "vue";
   import { getFeatureList } from "@/api/feature";

   const dataList = ref([]);
   const loading = ref(false);

   const fetchData = async () => {
     loading.value = true;
     try {
       const res = await getFeatureList({ ... });
       dataList.value = res.data.records;
     } catch (error) {
       ElMessage.error("操作失败");
     } finally {
       loading.value = false;
     }
   };

   onMounted(() => {
     fetchData();
   });
   </script>
   ```

3. **配置路由**
   ```typescript
   // frontend/src/router/modules/feature.ts
   export default {
     path: "/feature",
     name: "Feature",
     component: () => import("@/views/feature/index.vue"),
     meta: {
       title: "menus.feature",
       icon: "ri:feature-line",
       keepAlive: true
     }
   };
   ```

4. **添加国际化**
   ```yaml
   # frontend/locales/zh-CN.yaml
   menus:
     feature: "功能管理"

   # frontend/locales/en.yaml
   menus:
     feature: "Feature Management"
   ```

### Step 5: 配置菜单和权限

1. **确保菜单已在迁移脚本中添加**
   - 检查 Flyway 迁移脚本是否有菜单 INSERT
   - 检查角色权限是否已分配

2. **验证菜单显示**
   - 刷新页面，检查菜单是否显示
   - 检查菜单路由是否正确
   - 检查权限控制是否生效

### Step 6: 测试验证

1. **功能测试**
   - [ ] CRUD 操作正常（创建、读取、更新、删除）
   - [ ] 表单验证正常
   - [ ] 分页、搜索、筛选正常
   - [ ] 权限控制正常

2. **性能测试**
   - [ ] 查询响应时间 <500ms
   - [ ] 大数据量分页正常
   - [ ] 慢查询已优化

3. **兼容性测试**
   - [ ] 不同浏览器测试（Chrome, Firefox, Safari）
   - [ ] 不同分辨率测试（1920x1080, 1366x768）
   - [ ] 移动端响应式测试

### Step 7: 文档更新

参考：**[Documentation Reference](reference/documentation.md)**

1. **更新功能文档**
   - 在 `docs/features.md` 中添加功能说明
   - 更新相关模块文档

2. **更新 API 文档**
   - 如有 API 文档，更新接口说明

3. **更新 AGENTS.md**（如需要）
   - 如果有新的开发规范，更新 AGENTS.md

## 功能开发检查清单

提交代码前，检查：

### 数据库
- [ ] 表设计符合规范（snake_case，避免保留关键字）
- [ ] 迁移脚本创建完成（版本号正确）
- [ ] 菜单数据已添加到迁移脚本
- [ ] 角色菜单权限已分配
- [ ] 国际化 key 已添加

### 后端
- [ ] Entity 创建完成（@TableName, @TableId）
- [ ] Mapper 创建完成（XML 查询优化）
- [ ] Service/ServiceImpl 创建完成（@Service, @Transactional）
- [ ] Controller 创建完成（@RestController, 验证）
- [ ] 日志添加完成（关键操作记录）
- [ ] 异常处理完善（BusinessException）
- [ ] 代码符合规范（import, 命名, 长度）

### 前端
- [ ] API 函数创建完成（类型定义）
- [ ] 页面组件创建完成（Composition API）
- [ ] 路由配置完成（meta 信息）
- [ ] 国际化翻译已添加
- [ ] Element Plus 组件使用正确
- [ ] 加载状态、错误处理完善
- [ ] 代码符合规范（import, 命名, 格式）

### 配置
- [ ] 菜单显示正常
- [ ] 路由跳转正常
- [ ] 权限控制生效

### 测试
- [ ] 功能测试通过
- [ ] 性能测试通过
- [ ] 兼容性测试通过

### 文档
- [ ] 功能文档已更新
- [ ] API 文档已更新（如需要）

## 常见问题

### Q1: 如何确定菜单是否需要添加？
**A**: 如果新功能需要在前端菜单中显示，必须添加菜单项。菜单数据必须在迁移脚本中插入，而不是手动添加。

### Q2: 如何处理国际化？
**A**: 菜单标题使用 i18n key（如 `menus.feature`），在 `frontend/locales/` 目录中添加对应的翻译。

### Q3: 如何确保事务回滚？
**A**: 所有写操作使用 `@Transactional(rollbackFor = Exception.class)` 注解。

### Q4: 如何避免 N+1 查询？
**A**: 使用 JOIN 或批量查询（`selectBatchIds`），避免在循环中查询数据库。

## Related Skills

- **[java-development](../java-development/)** - Java 代码规范和最佳实践
- **[frontend-development](../frontend-development/)** - 前端开发和 UI/UX 设计
- **[database-operations](../database-operations/)** - 数据库操作和 SQL 优化
