## ADDED Requirements

### Requirement: 用户可以在前端配置智谱AI模型
系统 SHALL 支持用户在模型配置界面选择智谱作为厂商，并成功保存配置。

#### Scenario: 智谱厂商选项可见
- **WHEN** 用户打开模型配置对话框
- **THEN** 厂商下拉框中显示"智谱"选项

#### Scenario: 智谱配置可以保存
- **WHEN** 用户选择"智谱"厂商，输入模型ID和API Key，点击保存
- **THEN** 系统 SHALL 保存配置并显示保存成功提示

### Requirement: 智谱模型可以测试
系统 SHALL 支持用户测试已配置的智谱模型是否可用。

#### Scenario: 智谱模型健康检查
- **WHEN** 用户点击已配置的智谱模型的"测试"按钮
- **THEN** 系统 SHALL 调用智谱API验证配置有效性并返回测试结果

#### Scenario: 智谱模型对话测试
- **WHEN** 用户在模型测试界面发送测试消息
- **THEN** 系统 SHALL 调用智谱API并返回模型响应
