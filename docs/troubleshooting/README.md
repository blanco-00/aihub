# 故障排查

## 常见问题

### 服务启动失败

**症状**: `docker compose up -d` 失败

**解决方案**:
```bash
# 检查端口占用
lsof -i :3000
lsof -i :8080

# 检查Docker状态
docker info

# 查看日志
docker compose logs
```

### 模型连接失败

**症状**: 对话时报错 "模型连接失败"

**排查步骤**:
1. 检查API Key是否正确
2. 检查API地址是否可访问
3. 检查网络代理设置
4. 查看后端日志: `docker compose logs aihub-api`

### 登录失败

**症状**: 提示"用户名或密码错误"

**解决方案**:
1. 确认使用默认账号: `admin` / `admin123`
2. 检查数据库是否正常启动
3. 重置密码: 查看后端日志获取重置命令

### 流式响应不工作

**症状**: 对话没有打字机效果

**排查**:
1. 检查浏览器是否支持 SSE
2. 检查Nginx配置（如使用）
3. 确认后端流式接口正常

## 错误信息

| 错误代码 | 含义 | 解决方案 |
|---------|------|---------|
| E001 | 数据库连接失败 | 检查MySQL状态 |
| E002 | Redis连接失败 | 检查Redis状态 |
| E003 | 模型API调用失败 | 检查API Key和网络 |
| E004 | Token超限 | 检查账户余额 |

## 获取帮助

- 📖 [GitHub Discussions](https://github.com/aihub/aihub/discussions)
- 🐛 [提交Issue](https://github.com/aihub/aihub/issues)
- 💬 加入社区群组

---

[返回文档首页](../../)
