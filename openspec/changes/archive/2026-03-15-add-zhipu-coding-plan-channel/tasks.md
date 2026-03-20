## 1. 前端实现

- [x] 1.1 在 ModelConfigDialog.vue 的 vendorOptions 中添加智谱选项 `{ label: "智谱", value: "zhipuai" }`

## 2. 优化: 自动获取模型列表

- [x] 2.1 后端添加 `ModelProvider.getModels()` 接口
- [x] 2.2 后端添加 `/api/ai/chat/models` API
- [x] 2.3 前端添加获取模型列表 API
- [x] 2.4 前端模型选择改为下拉框+手动输入混合模式
- [x] 2.5 添加"获取模型"按钮，输入API Key后自动获取可用模型

## 3. 测试验证

- [ ] 3.1 重启前端和后端服务
- [ ] 3.2 打开 http://localhost:3000/model/config/index
- [ ] 3.3 点击"新增模型"
- [ ] 3.4 选择"智谱"厂商
- [ ] 3.5 输入API Key（你的智谱key）
- [ ] 3.6 点击"获取模型"按钮
- [ ] 3.7 从下拉框选择模型
- [ ] 3.8 保存并测试连通性
