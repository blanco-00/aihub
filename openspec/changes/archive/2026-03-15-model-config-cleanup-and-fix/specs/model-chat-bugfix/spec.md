## ADDED Requirements

### Requirement: Model chat response correctly handled
模型对话的响应 SHALL 正确解析并显示。

#### Scenario: Chat response success
- **WHEN** 后端返回 `{code: 200, data: "actual response content"}`
- **THEN** 前端 SHALL 正确提取 "actual response content" 并显示在对话中

#### Scenario: Chat response error
- **WHEN** 后端返回 `{code: 500, message: "error message"}`
- **THEN** 前端 SHALL 显示错误信息 "[错误] error message"

#### Scenario: Model health check displays correctly
- **WHEN** 用户选择模型后
- **THEN** 系统 SHALL 自动检查模型健康状态并显示 "可用" 或 "不可用" 标签

#### Scenario: Model list loads on page mount
- **WHEN** 用户打开AI对话页面
- **THEN** 系统 SHALL 自动加载所有已启用的模型列表
