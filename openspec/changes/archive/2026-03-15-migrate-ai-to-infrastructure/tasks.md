## 1. 迁移AI代码

- [x] 1.1 在aihub-ai-infrastructure创建AI包结构
- [x] 1.2 移动ModelProvider接口到新模块
- [x] 1.3 移动ModelProviderFactory到新模块
- [x] 1.4 移动Provider实现类到新模块
- [x] 1.5 移动ModelGateway到新模块

## 2. 更新依赖

- [x] 2.1 aihub-admin添加对aihub-ai-infrastructure的依赖
- [x] 2.2 更新import语句(aihub-admin的ModelChatController)

## 3. 验证

- [x] 3.1 编译验证 (AI代码迁移完成，pre-existing type error需另行修复)
- [ ] 3.2 测试接口正常
