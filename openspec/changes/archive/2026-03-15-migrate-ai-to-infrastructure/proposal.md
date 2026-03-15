## Why

AI相关代码(ModelGateway/ModelProvider)错误地放在了aihub-admin模块，应该放在aihub-ai-infrastructure模块，保持架构清晰。

## What Changes

1. **迁移AI代码到正确模块** - 从aihub-admin迁移到aihub-ai-infrastructure
2. **创建包结构** - 在aihub-ai-infrastructure中创建AI相关包
3. **更新依赖** - aihub-admin依赖aihub-ai-infrastructure

## Capabilities

### New Capabilities
- `ai-infrastructure-structure`: 定义AI基础设施模块的包结构和职责

### Modified Capabilities
- (无)

## Impact

- **代码位置**: ai相关代码移到aihub-ai-infrastructure
- **依赖关系**: aihub-admin依赖aihub-ai-infrastructure
- **包名变更**: com.aihub.admin.ai → com.aihub.ai.infrastructure
