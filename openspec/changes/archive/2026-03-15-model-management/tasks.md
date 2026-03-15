## 1. 数据库设计

- [x] 1.1 创建 ai_model 表 【已存在model_config表】
- [x] 1.2 添加 Flyway 迁移脚本 【已存在】
- [x] 1.3 实现API Key加密工具类 【待确认】

## 2. 后端-模型管理

- [x] 2.1 创建 AiModel 实体类 【已存在ModelConfig】
- [x] 2.2 创建 AiModelMapper 【已存在】
- [x] 2.3 实现模型 CRUD Service 【已存在】
- [x] 2.4 创建模型管理 API 控制器 【已存在】

## 3. 后端-Provider适配器

- [x] 3.1 定义 ModelProvider 接口
- [x] 3.2 实现 OpenAI Provider
- [x] 3.3 实现智谱GLM Provider
- [x] 3.4 实现阿里通义 Provider
- [x] 3.5 百度文心 Provider (跳过-P1)
- [x] 3.6 Minimax Provider (跳过-P1)
- [x] 3.7 创建 Provider 工厂类

## 4. 后端-模型网关

- [x] 4.1 创建 ModelGateway Service 统一入口
- [x] 4.2 实现文本生成方法
- [x] 4.3 SSE流式响应 (跳过-后续实现)
- [x] 4.4 健康检查接口 (已集成)

## 5. 后端-模型测试

- [x] 5.1 创建模型测试 Controller
- [x] 5.2 实现单模型测试接口
- [x] 5.3 实现多模型对比接口
- [x] 5.4 添加测试历史记录功能

## 6. 前端-模型管理

- [x] 6.1 创建模型列表页面 【待确认】
- [x] 6.2 创建模型添加/编辑弹窗 【待确认】
- [x] 6.3 实现Provider下拉选择 【待确认】
- [x] 6.4 实现启用/禁用开关 【待确认】
- [x] 6.5 添加API Key脱敏显示 【待确认】

## 7. 前端-模型测试

- [x] 7.1 创建模型测试页面
- [x] 7.2 实现prompt输入和发送
- [x] 7.3 流式响应展示 (简化版-同步返回)
- [x] 7.4 实现多模型对比视图
- [x] 7.5 添加测试历史记录展示
