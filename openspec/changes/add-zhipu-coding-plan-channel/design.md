## Context

**背景**: 用户需要添加智谱AI的coding plan渠道来进行测试。当前系统状态:

- 后端已实现 `ZhipuAIProvider`，支持调用智谱API (`https://open.bigmodel.cn/api/paas/v4`)
- `ModelProviderFactory` 中已有 `zhipuai` 和 `zhipu` 的厂商别名映射
- 前端 `ModelConfigDialog.vue` 的厂商下拉框中缺少智谱选项，仅支持: OpenAI, Anthropic, Azure, 百度, 阿里, 腾讯

**约束**:
- 需保持与现有代码风格一致
- 不修改后端逻辑（ZhipuAIProvider已实现）
- 最小改动原则

## Goals / Non-Goals

**Goals:**
- 在前端厂商下拉框中添加"智谱"选项
- 验证智谱coding plan渠道可正常配置和测试

**Non-Goals:**
- 不新增数据库表
- 不修改后端代码（已有ZhipuAI支持）
- 不实现流式输出（当前ZhipuAIProvider不支持stream）

## Decisions

1. **前端方案**: 在 `vendorOptions` 数组中添加智谱选项
   - 方案A: 添加 `{ label: "智谱", value: "zhipuai" }`
   - 方案B: 添加 `{ label: "智谱", value: "zhipu" }`
   - **选择**: `zhipuai` - 与后端 `ModelProviderFactory` 中的别名一致

2. **模型ID**: 用户需手动输入智谱的模型ID
   - 常用: `glm-4`, `glm-4-flash`, `glm-4-plus` 等
   - coding plan可能使用: `glm-4` 或专用coding模型

3. **Base URL**: 使用智谱默认API地址
   - 默认: `https://open.bigmodel.cn/api/paas/v4`
   - 用户可自定义

## Risks / Trade-offs

- [风险] 前端厂商选项与后端Provider不完全匹配 → 已验证后端支持zhipuai
- [风险] 未知智谱coding plan具体模型ID → 用户需自行确认正确的modelId

## Open Questions

- 智谱coding plan对应的具体modelId是什么？（用户需要自行确认）
