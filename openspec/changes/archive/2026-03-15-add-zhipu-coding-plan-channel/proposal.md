## Why

用户需要添加智谱AI的coding plan渠道来进行测试。目前系统中虽然已有ZhipuAI provider，但前端厂商选项中没有智谱(智谱)选项，导致用户无法在界面上配置和测试智谱的coding模型。

## What Changes

1. **前端新增智谱厂商选项** - 在模型配置对话框的厂商下拉框中添加"智谱"选项
2. **确保后端支持** - 验证现有ZhipuAI provider能正确处理智谱API调用
3. **测试验证** - 在模型配置页面测试智谱coding plan渠道是否正常工作

## Capabilities

### New Capabilities
- `zhipu-channel`: 新增智谱AI模型渠道，支持配置和测试

### Modified Capabilities
- (无)

## Impact

- 前端: `frontend/src/views/system/model/components/ModelConfigDialog.vue` - 添加厂商选项
- 后端: `ModelProviderFactory.java` - 已支持ZhipuAI，无需修改
- 数据: 无需新增数据库表

