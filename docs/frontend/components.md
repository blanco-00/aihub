# 前端组件设计文档

> 本文档定义了前端组件的分类、设计和开发规范。

## 📚 相关文档

- [前端开发指南](./guide.md) - 返回前端文档总览
- [UI/UX设计文档](./design.md) - 查看设计规范
- [页面规划文档](./pages.md) - 查看页面规划

## 组件分类

### 1. 布局组件 (Layout Components)

#### 1.1 AppLayout
**用途**: 应用主布局
**Props**:
```typescript
interface AppLayoutProps {
  // 使用 Vue 3 的插槽
  // 通过 <slot> 传递内容
}
```

#### 1.2 Sidebar
**用途**: 侧边栏导航
**功能**:
- 菜单项配置
- 折叠/展开
- 当前路由高亮
- 权限控制

#### 1.3 Header
**用途**: 顶部导航栏
**功能**:
- Logo
- 面包屑导航
- 用户信息
- 通知中心
- 搜索框（全局）

---

### 2. 业务组件 (Business Components)

#### 2.1 ModelCard
**用途**: 模型卡片展示
**Props**:
```typescript
interface ModelCardProps {
  model: Model;
  onEdit?: () => void;
  onTest?: () => void;
  onDelete?: () => void;
}
```

#### 2.2 AgentWorkflowEditor
**用途**: Agent工作流可视化编辑器
**技术**: Vue Flow (@vue-flow/core) / X6
**功能**:
- 拖拽创建节点
- 连接节点
- 节点配置面板
- 工作流验证
- 导出/导入

#### 2.3 PromptEditor
**用途**: Prompt编辑器
**技术**: Monaco Editor
**功能**:
- 代码编辑
- 变量高亮
- 自动补全
- 实时预览

#### 2.4 TokenChart
**用途**: Token消耗图表
**技术**: ECharts / Recharts
**功能**:
- 多维度数据展示
- 时间范围选择
- 数据钻取
- 导出图片

#### 2.5 PermissionMatrix
**用途**: 权限矩阵配置
**功能**:
- 角色-资源权限表格
- 批量设置
- 权限继承展示

---

### 3. 通用组件 (Common Components)

#### 3.1 DataTable
**用途**: 数据表格（通用）
**功能**:
- 排序
- 筛选
- 分页
- 选择
- 操作列

#### 3.2 SearchBar
**用途**: 搜索栏
**功能**:
- 关键词搜索
- 高级筛选
- 搜索历史
- 快速筛选标签

#### 3.3 StatusBadge
**用途**: 状态标签
**类型**: success / warning / error / info

#### 3.4 ConfirmModal
**用途**: 确认对话框
**功能**:
- 危险操作确认
- 自定义提示信息
- 确认/取消回调

#### 3.5 FormField
**用途**: 表单字段（封装）
**功能**:
- 标签
- 必填标识
- 错误提示
- 帮助文本

---

### 4. 数据展示组件

#### 4.1 StatCard
**用途**: 统计卡片
**Props**:
```typescript
interface StatCardProps {
  title: string;
  value: string | number;
  trend?: 'up' | 'down' | 'stable';
  trendValue?: string;
  // icon 通过插槽传递
  onClick?: () => void;
}
```

#### 4.2 TrendChart
**用途**: 趋势图表
**功能**:
- 折线图/柱状图切换
- 时间范围选择
- 数据点提示
- 对比模式

#### 4.3 UsageHeatmap
**用途**: 使用热力图
**功能**:
- 时间维度展示
- 颜色映射
- 交互提示

---

### 5. 表单组件

#### 5.1 ModelForm
**用途**: 模型配置表单
**字段**:
- 基本信息
- API配置
- 参数配置
- 路由策略

#### 5.2 AgentForm
**用途**: Agent配置表单
**字段**:
- 基本信息
- 工作流配置（嵌入工作流编辑器）
- Prompt选择
- 测试配置

#### 5.3 PromptForm
**用途**: Prompt编辑表单
**字段**:
- Prompt内容（嵌入编辑器）
- 元信息
- 标签
- 版本说明

---

### 6. 反馈组件

#### 6.1 Toast
**用途**: 消息提示
**类型**: success / error / warning / info

#### 6.2 Loading
**用途**: 加载状态
**变体**:
- 全屏加载
- 局部加载
- 骨架屏
- 进度条

#### 6.3 EmptyState
**用途**: 空状态
**场景**:
- 无数据
- 加载失败
- 无权限

---

## 组件开发规范

### 命名规范
- 组件名：PascalCase
- Props接口：`ComponentNameProps`
- 文件名：kebab-case

### 代码结构
```vue
<!-- ComponentName.vue -->
<script setup lang="ts">
interface ComponentNameProps {
  // props定义
  title: string;
  value?: number;
}

const props = withDefaults(defineProps<ComponentNameProps>(), {
  value: 0,
});

// 组件逻辑
const count = ref(0);
</script>

<template>
  <div class="component-name">
    <h3>{{ props.title }}</h3>
    <p>{{ props.value }}</p>
  </div>
</template>

<style scoped>
.component-name {
  /* 样式 */
}
</style>
```

### Props设计原则
1. 保持Props接口简洁
2. 使用联合类型而非any
3. 提供合理的默认值
4. 文档化所有Props

### 状态管理
- 组件内部状态：`ref`、`reactive`（Vue 3 Composition API）
- 共享状态：Pinia（Vue 官方推荐）
- 服务端状态：Vue Query（可选）或 Pinia + Axios

### 样式方案
- Scoped CSS（Vue 3 推荐）
- CSS Modules
- Tailwind CSS（可选）

---

## 组件复用策略

### 1. 基础组件库
使用 Element Plus 作为基础UI组件库

### 2. 业务组件封装
在基础组件上封装业务逻辑，形成业务组件

### 3. 组件文档
使用 Storybook 维护组件文档和示例

---

## 组件测试

### 测试策略
- 单元测试：Vitest + Vue Test Utils
- 视觉回归测试：Chromatic（可选）
- E2E测试：Playwright / Cypress

### 测试覆盖
- 核心业务组件：> 80%
- 通用组件：> 90%

---

## 组件清单（MVP阶段）

### 必须组件
- [ ] AppLayout
- [ ] Sidebar
- [ ] Header
- [ ] DataTable
- [ ] ModelCard / ModelForm
- [ ] AgentWorkflowEditor（基础版）
- [ ] PromptEditor
- [ ] StatCard
- [ ] TokenChart

### 重要组件
- [ ] SearchBar
- [ ] PermissionMatrix
- [ ] ConfirmModal
- [ ] Loading / Skeleton

### 增强组件
- [ ] TrendChart
- [ ] UsageHeatmap
- [ ] EmptyState

