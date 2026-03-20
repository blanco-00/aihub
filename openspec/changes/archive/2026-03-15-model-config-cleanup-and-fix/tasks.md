## 1. 清理测试文件

- [x] 1.1 更新 .gitignore 添加测试产物忽略规则
  - 添加 `*screenshot*.png`, `chat-*.png`, `console.txt` 等模式
  - 添加 `playwright-report/`, `test-results/` 等测试输出目录

- [x] 1.2 删除已跟踪的测试文件
  - chat-final.png, chat-working.png, page-screenshot.png, menu-screenshot.png, chat-page.png, console.txt

## 2. 模型配置优化 - 厂商默认URL

- [x] 2.1 在前端添加厂商默认URL映射表
  - 文件: `frontend/src/views/system/model/components/ModelConfigDialog.vue`
  - 添加 vendorDefaultUrls 常量

- [x] 2.2 实现厂商选择时自动填充baseUrl
  - 在 vendor select 的 @change 事件中设置默认URL

- [x] 2.3 更新 baseUrl 校验规则
  - 改为非必填，保留URL格式校验（可选）

## 3. 对话页面Bug修复

- [x] 3.1 修复响应处理逻辑
  - 文件: `frontend/src/views/ai/chat/index.vue`
  - 修改 `response.data.response || response.data` 为 `response.data`

- [x] 3.2 修复健康检查结果存储
  - 将 health check 结果存入响应式变量
  - 在模型切换时自动检查

- [x] 3.3 修复模型列表加载
  - 在 onMounted 中加载模型列表
  - 确保响应式更新

## 4. UI交互改进（可选/调研）

- [x] 4.1 调研厂商/模型分离管理的可行性
  - 结论：当前不建议分离，改动较大且收益有限
  - 已通过"厂商默认URL"优化改善用户体验

- [x] 4.2 修复聊天框状态显示问题
  - 添加"检查中..."状态
  - 处理 null 状态
