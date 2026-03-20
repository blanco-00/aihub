# 3分钟快速开始

> **前置要求**: Docker 20.10+, Docker Compose 2.0+

## 第一步：启动服务（2分钟）

```bash
# 克隆项目
git clone https://github.com/aihub/aihub.git
cd aihub

# 一键启动
cd docker && docker compose up -d

# 查看状态
docker compose ps
```

## 第二步：访问系统（30秒）

打开浏览器访问：
- **前端**: http://localhost:3000
- **后端API**: http://localhost:8080

**默认账号**:
- 用户名: `admin`
- 密码: `admin123`

## 第三步：开始对话（30秒）

1. 登录系统
2. 点击左侧菜单「AI对话」
3. 选择模型（如 GPT-3.5）
4. 输入消息，开始对话！

## 🎉 恭喜！

你已经成功创建了第一个AI对话。接下来可以：

- 📖 [添加更多模型](../user-guide/model-management/add-model.md)
- 🎨 [使用Prompt模板](../user-guide/prompt-templates/using-templates.md)
- 📚 [查看更多示例](../examples/)

---

## 遇到问题？

- [常见问题](../troubleshooting/common-issues.md)
- [错误信息](../troubleshooting/error-messages.md)
- [GitHub Issues](https://github.com/aihub/aihub/issues)

---

## 下一步

| 想做什么 | 推荐阅读 |
|---------|---------|
| 添加自定义模型 | [模型管理指南](../user-guide/model-management/) |
| 创建Prompt模板 | [Prompt模板指南](../user-guide/prompt-templates/) |
| 部署到生产环境 | [部署指南](../user-guide/deployment/) |
| 了解系统架构 | [架构文档](../architecture/system-architecture.md) |
| 参与开发 | [贡献指南](../developer/contributing.md) |
