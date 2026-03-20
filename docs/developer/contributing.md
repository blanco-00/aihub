# 贡献指南

感谢你考虑为 AIHub 做贡献！

## 🤔 如何贡献

### 报告问题

1. 搜索 [现有Issues](https://github.com/aihub/aihub/issues) 避免重复
2. 使用Issue模板提交问题
3. 提供详细的复现步骤

### 提交代码

1. Fork 项目
2. 创建特性分支: `git checkout -b feature/amazing-feature`
3. 提交更改: `git commit -m 'Add amazing feature'`
4. 推送分支: `git push origin feature/amazing-feature`
5. 提交 Pull Request

### 代码规范

**前端 (TypeScript/Vue)**
- 2空格缩进
- 双引号
- 末尾分号

**后端 (Java)**
- 遵循Google Java Style
- 方法长度 < 50行
- 有意义的命名

### 开发环境

```bash
# 前端
cd frontend && pnpm install && pnpm dev

# 后端
cd backend/aihub-api && mvn spring-boot:run

# AI服务
cd aihub-agent && pip install -r requirements.txt && uvicorn main:app
```

## 📖 文档贡献

- 修复拼写错误
- 完善文档内容
- 添加示例代码

## 🙏 感谢

每一位贡献者都是 AIHub 的重要组成部分！

---

[返回开发者文档](./) · [返回首页](../../)
