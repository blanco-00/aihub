## Context

AIHub 后端已实现模型管理功能（通过 model-management 变更），包括：
- `ModelGateway` - 统一模型调用接口
- `ModelProvider` - 多模型 Provider 适配器
- 在线模型测试能力

前端使用 Vue 3 + TypeScript + Element Plus 技术栈，已有基础的用户管理、部门管理等页面框架。需要在此框架基础上补充模型管理 UI。

当前前端缺乏模型管理相关页面，用户无法通过 Web 界面配置和管理 AI 模型，导致核心 AI 能力无法正常使用。

## Goals / Non-Goals

**Goals:**
- 提供完整的模型管理前端页面，支持模型的增删改查操作
- 提供在线模型测试页面，方便用户验证模型配置
- 与后端 ModelGateway、ModelProvider API 完全对接
- 遵循 AIHub 前端现有设计规范（Element Plus 组件、代码风格）

**Non-Goals:**
- 修改后端 API 或数据结构
- 实现新的模型 Provider（后端已支持）
- 模型训练或微调功能
- 复杂的模型性能分析和监控

## Decisions

### UI 框架和组件选择
- **决策**: 使用 Element Plus 组件库，遵循项目现有 UI 规范
- **理由**: 项目其他页面已使用 Element Plus，保持一致性和开发效率
- **替代方案**: 使用自定义组件（开发成本高，风格不统一）

### 页面结构
- **决策**: 模型管理采用主页面 + 弹窗表单的交互模式
- **理由**: 符合 Element Plus 设计规范，用户操作流畅
- **替代方案**: 独立的添加/编辑页面（用户体验较差，需要多次跳转）

### API 通信
- **决策**: 使用项目现有的 axios 实例和 API 封装方式
- **理由**: 保持代码一致性，复用请求拦截、错误处理等逻辑
- **替代方案**: 直接使用原生 fetch（缺乏统一的错误处理和配置）

### 状态管理
- **决策**: 使用 Pinia 进行页面级状态管理（如模型列表、筛选条件）
- **理由**: 项目已集成 Pinia，适合处理复杂交互状态
- **替代方案**: 仅使用组件内部响应式数据（组件间共享困难）

### 模型测试界面
- **决策**: 采用对话式测试界面，类似 ChatGPT 的交互方式
- **理由**: 直观易用，符合用户对模型测试的预期
- **替代方案**: 填写 JSON 格式的 prompt 和参数（技术门槛高，不友好）

## Menu Configuration

**关键发现**:
- 前端模型管理页面已存在: `frontend/src/views/system/model/index.vue`
- 前端 API 已存在: `frontend/src/api/modelConfig.ts`
- SQL migration 脚本已存在: `backend/.../db/migration/V1.0.18__add_model_management_menu.sql`
- **问题**: 数据库中没有模型管理菜单项（ModelManagement, ModelConfig），导致用户无法访问

**菜单配置方案**:

1. **执行 SQL Migration**:
   - 直接在数据库中执行 `V1.0.18__add_model_management_menu.sql`
   - 插入父菜单 `ModelManagement`（path: `/model`, title: `menus.pureModelManagement`）
   - 插入子菜单 `ModelConfig`（path: `/model/config/index`, title: `menus.pureModelConfig`）
   - 为所有角色分配这两个菜单的访问权限

2. **后端动态路由**:
   - 菜单数据通过 `/api/menus/tree` API 获取
   - 前端路由通过 `/api/routes/async` API 获取
   - 前端 `router/modules/system.ts` 为空，完全依赖后端动态路由

3. **菜单层级结构**:
   ```
   系统管理 (SystemManagement)
   ├── 用户管理
   ├── 角色管理
   ├── 菜单管理
   ├── 部门管理
   └── 模型管理 (ModelManagement) - 新增
       └── 模型配置 (ModelConfig) - 新增
   ```

4. **权限控制**:
   - 通过 `role_menu` 表控制菜单访问权限
   - 默认为所有角色分配模型管理菜单

## Risks / Trade-offs

### Risk1: 菜单迁移未执行
- **风险**: SQL migration 文件存在但未在数据库中执行，导致菜单缺失
- **缓解**: 手动执行 migration SQL；验证数据库中菜单数据正确插入
- **已确认**: 当前数据库中**没有** ModelManagement 和 ModelConfig 菜单项

### Risk2: 后端 API 可能尚未完全实现
- **风险**: 前端开发时发现后端 API 缺失或不完整
- **缓解**: 开发前先测试后端 API，与后端开发人员确认接口契约；必要时补充 API Mock 数据

### Risk 3: API Key 安全性
- **风险**: API Key 在前端传输和显示过程中可能泄露
- **缓解**: API Key 传输使用 HTTPS；显示时脱敏显示（如 `sk-****1234`）；敏感信息不存储在前端状态

### Risk 4: 多模型 Provider 参数差异
- **风险**: 不同 Provider（OpenAI、智谱、通义等）的配置参数差异大，表单设计复杂
- **缓解**: 使用动态表单配置，根据选择的 Provider 类型动态显示对应参数字段；参考后端 ModelProvider 的参数定义

### Trade-off: 开发速度 vs 代码复用
- **权衡**: 复用现有 CRUD 页面模板可能限制定制化
- **决策**: 复用基础结构和组件，但针对模型管理的特殊需求进行定制（如在线测试、Provider 参数配置）
