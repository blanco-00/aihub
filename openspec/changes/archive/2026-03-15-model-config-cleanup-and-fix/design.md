## Context

当前AIHub系统有4个需要改进的问题：

### 当前状态

1. **模型配置**：
   - 后端 `ZhipuAIProvider` 已有默认baseUrl (`https://open.bigmodel.cn/api/paas`)，但前端仍要求用户填写
   - 前端 `ModelConfigDialog.vue` 中baseUrl是可选的但有正则校验，用户体验不佳

2. **测试文件**：
   - 6个测试产物文件被提交到git：`chat-final.png`, `chat-working.png`, `page-screenshot.png`, `menu-screenshot.png`, `chat-page.png`, `console.txt`
   - `.gitignore` 缺少对这些文件的忽略规则

3. **UI交互**：
   - 厂商配置：需要填API Key和可选的baseUrl
   - 模型选择：在配置对话框中填写，可获取模型列表
   - 两者混在一起，模型切换不够便捷

4. **对话Bug**：
   - 后端返回 `Result.success(response)` → 前端收到 `{code: 200, data: "actual response"}`
   - 前端错误处理：`response.data.response || response.data`（应为 `response.data`）
   - 模型状态：健康检查结果未存入响应式变量

### 约束

- 后端使用Spring Boot 3.2.0
- 前端使用Vue 3 + Element Plus
- 不能破坏现有功能

## Goals / Non-Goals

**Goals:**
1. 选择厂商时自动填充默认baseUrl，用户可选覆盖
2. 清理测试文件并更新.gitignore防止再次提交
3. 改进厂商/模型配置的UI交互设计
4. 修复模型对话响应处理和状态显示Bug

**Non-Goals:**
- 不修改后端API接口
- 不改变数据库结构
- 不重构模型供应商的底层实现

## Decisions

### 1. 厂商默认URL配置方式

**选项A**: 在前端硬编码默认URL
- 优点：简单快速
- 缺点：维护成本高，厂商URL变更需要代码更新

**选项B**: 后端提供厂商元数据API
- 优点：配置集中管理，易于维护
- 缺点：需要新增API

**选择A**：在前端维护厂商默认URL映射表
- 原因：当前厂商数量有限，变动不频繁，简单方案更实用
- 后续可扩展为后端API

### 2. 测试文件清理方式

**选项A**: git rm --cached + .gitignore
- 优点：保留文件在本地
- 缺点：需要commit两次

**选择B**: 直接删除文件 + 更新.gitignore
- 原因：这些是测试产物，应该删除
- 执行：`git rm` + 更新`.gitignore`

### 3. 厂商/模型UI交互设计

**选项A**: 保持现有设计，仅优化
- 优点：改动小
- 缺点：交互本质未改善

**选项B**: 分离厂商管理和模型管理
- 厂商管理：只需配置API Key和可选URL
- 模型管理：关联厂商，快速选择/切换模型

**选择B**：分离为两个管理模块
- 更好的关注点分离
- 模型切换更便捷

### 4. 对话Bug修复

**问题1**: response.data.response || response.data
- 修复：直接使用 `response.data`

**问题2**: 健康检查未存储结果
- 修复：在 reactive 变量中存储健康检查结果

**问题3**: 模型列表加载时机
- 修复：在页面加载时获取启用的模型列表

## Risks / Trade-offs

- [风险] 修改前端表单可能影响现有用户填写习惯 → 保持必填字段不变，只改变可选行为
- [风险] 删除git跟踪的文件需要commit → 明确告知用户需要commit

## Migration Plan

1. 更新前端代码（厂商默认URL、Bug修复）
2. 更新.gitignore
3. 删除测试文件并commit
4. 测试验证功能正常
