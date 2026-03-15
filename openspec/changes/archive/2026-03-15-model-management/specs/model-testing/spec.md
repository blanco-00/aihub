## ADDED Requirements

### Requirement: 在线模型测试
系统 SHALL 提供模型测试页面，用户可选择模型并输入prompt进行测试。

#### Scenario: 选择模型测试
- **WHEN** 用户选择已配置的模型，输入测试prompt"你好"
- **THEN** 系统调用模型API，显示返回的回复内容

#### Scenario: 流式测试
- **WHEN** 开启流式输出测试
- **THEN** 逐字显示模型响应

#### Scenario: 测试多模型对比
- **WHEN** 选择多个模型输入相同prompt
- **THEN** 并行调用各模型，对比返回结果

### Requirement: 测试历史记录
系统 SHALL 保存测试记录，便于回顾和调试。

#### Scenario: 查看测试历史
- **WHEN** 用户查看测试历史
- **THEN** 显示历次测试的模型、prompt、回复内容
