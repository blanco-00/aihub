## Why

企业级AI Agent平台市场需求爆发(25%企业已试点)，但现有方案存在三大痛点：1) 缺乏可视化配置工具，业务人员难以使用；2) 缺少成本控制能力，企业不敢大规模部署；3) 私有化部署能力不足，数据安全受限。AIHub需要首先实现Agent模板系统作为核心差异化功能。

## What Changes

1. **Agent模板定义与存储** - 支持JSON Schema定义Agent模板结构
2. **模板实例化引擎** - 从模板快速创建Agent实例
3. **模板市场基础** - 内置Agent模板库(客服/研发/分析等场景)
4. **模板继承机制** - 支持模板间继承和扩展

## Capabilities

### New Capabilities
- `agent-template`: Agent模板的元数据定义、版本管理、继承关系
- `agent-instance`: 从模板实例化Agent，参数化配置
- `template-marketplace`: 内置模板库和模板市场基础

### Modified Capabilities
- (无) - 基础功能，不涉及现有能力修改

## Impact

- **后端**: 新增模板管理模块 (aihub-ai-applications)
- **前端**: 新增模板选择/配置页面
- **数据库**: 新增agent_template, agent_instance表
- **依赖**: 无新增外部依赖
