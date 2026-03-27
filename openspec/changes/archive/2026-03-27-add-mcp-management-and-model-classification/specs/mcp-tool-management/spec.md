## ADDED Requirements

### Requirement: MCP 工具列表页
系统 SHALL 提供独立的 MCP 工具管理页面，路径为 `/mcp/index`。

### Requirement: MCP 工具列表展示
系统 SHALL 在页面顶部展示所有已注册的工具，显示名称、描述、类型、调用次数、最后执行时间。

### Requirement: MCP 工具启用/禁用
系统 SHALL 提供启用/禁用工具的功能，禁用的工具在列表中显示但不可执行。

### Requirement: MCP 工具测试执行
系统 SHALL 提供工具测试功能，用户可输入参数并执行工具，查看执行结果。

### Requirement: MCP 工具刷新
系统 SHALL 提供刷新按钮，重新加载工具列表。

#### Scenario: 查看工具列表
- **WHEN** 用户访问 `/mcp/index`
- **THEN** 显示所有 MCP 工具的列表（名称、描述、类型、调用次数、最后执行时间）

#### Scenario: 启用工具
- **WHEN** 用户点击工具的"启用"按钮
- **THEN** 工具状态更新为已启用，可执行

#### Scenario: 禁用工具
- **WHEN** 用户点击工具的"禁用"按钮
- **THEN** 工具状态更新为已禁用，列表中显示但不可执行

#### Scenario: 测试工具
- **WHEN** 用户选择一个工具并输入参数后点击"执行"
- **THEN** 系统调用工具处理器并返回执行结果

#### Scenario: 刷新工具列表
- **WHEN** 用户点击刷新按钮
- **THEN** 系统重新加载工具列表并更新显示
