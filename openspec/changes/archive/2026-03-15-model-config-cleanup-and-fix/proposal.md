## Why

当前AIHub的模型配置和使用存在4个问题：
1. 用户配置模型时需要手动填写baseUrl，但实际上每个厂商都有默认URL，应该自动设置
2. 项目中存在6个测试生成的截图和console文件，应该清理并防止再次提交
3. 厂商和模型的UI交互设计不合理——厂商配置只需填写API Key，URL默认；模型应该可以快速切换
4. 模型对话页面存在Bug：后端返回成功但前端显示异常，模型状态显示也有问题

## What Changes

1. **模型配置优化**：选择厂商时自动填充默认baseUrl，用户可选覆盖
2. **清理测试文件**：删除截图和console文件，更新.gitignore
3. **优化UI交互**：调研并改进厂商/模型的界面交互设计
4. **修复对话Bug**：修复模型对话和状态显示的问题

## Capabilities

### New Capabilities
- `model-vendor-default-url`: 厂商选择时自动设置默认baseUrl
- `model-config-ui-redesign`: 重新设计厂商和模型的配置界面交互

### Modified Capabilities
- `model-chat`: 修复对话响应处理和模型状态显示的Bug

## Impact

- 前端：修改模型配置对话框、模型测试页面、.gitignore
- 后端：可能需要调整厂商默认URL配置（如果后端存储默认URL）
