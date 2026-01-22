# 日志规范

## 日志等级使用

- **日志等级应该恰当**：在满足追踪流程、问题定位、避免日志过多等要素中找到平衡
- **合理使用日志级别**：
  - `DEBUG`: 详细的调试信息，通常只在开发环境启用
  - `INFO`: 关键业务流程节点，重要操作记录
  - `WARN`: 警告信息，潜在问题但不影响功能
  - `ERROR`: 错误信息，需要关注和处理的异常

## 避免重复日志

- **相似内容的日志不要重复打印**：相同或相似的日志信息应该合并或去重
- **避免在循环中重复打印相同日志**：在循环中应该记录关键信息，避免每次循环都打印相同内容

### 错误示例
```java
// ❌ 错误：在循环中重复打印相同日志
for (User user : users) {
    log.info("处理用户: {}", user.getUsername());
    // 如果 users 有 1000 个，会打印 1000 次相同格式的日志
}
```

### 正确示例
```java
// ✅ 正确：记录批量操作信息
log.info("开始批量处理用户，总数: {}", users.size());
for (User user : users) {
    // 处理逻辑
    // 只在关键节点或异常时记录日志
}
log.info("批量处理用户完成");
```

## 日志内容规范

- **关键操作必须记录日志**：如创建、更新、删除等重要操作
- **敏感信息不得记录日志**：密码、密钥、完整 token 等敏感信息不应记录到日志
- **记录必要的上下文信息**：如用户ID、操作类型、关键参数等，便于问题追踪

## 日志跟踪规范（易于跟踪问题）

- **记录关键业务标识**：日志中必须包含能够唯一标识业务对象的字段（如用户ID、订单ID、菜单ID等）
- **记录请求上下文**：关键操作必须记录请求ID、用户ID、IP地址等上下文信息，便于追踪请求链路
- **记录操作前后状态**：重要状态变更必须记录变更前后的值，便于问题回溯
- **记录异常完整信息**：异常日志必须包含完整的堆栈信息，不能只记录异常消息
- **使用结构化日志**：使用参数化日志，便于日志分析和搜索
- **记录操作时间**：关键操作应记录操作耗时，便于性能分析

### 错误示例
```java
// ❌ 错误：缺少关键标识，无法定位具体业务对象
log.info("用户登录成功");

// ❌ 错误：缺少上下文信息，无法追踪请求链路
log.error("创建菜单失败");

// ❌ 错误：缺少状态变更信息，无法回溯问题
log.info("更新用户状态");

// ❌ 错误：只记录异常消息，丢失堆栈信息
log.error("删除菜单失败: " + e.getMessage());

// ❌ 错误：缺少业务标识，无法定位问题
log.warn("验证码错误");
```

### 正确示例
```java
// ✅ 正确：包含用户ID和用户名，便于定位
log.info("用户登录成功: userId={}, username={}", userId, username);

// ✅ 正确：包含请求ID、用户ID、IP等上下文信息
log.error("创建菜单失败: requestId={}, userId={}, ip={}, menuName={}",
    requestId, userId, ip, menuName, e);

// ✅ 正确：记录状态变更前后的值
log.info("更新用户状态: userId={}, oldStatus={}, newStatus={}",
    userId, oldStatus, newStatus);

// ✅ 正确：包含完整堆栈信息
log.error("删除菜单失败: menuId={}, menuName={}", menuId, menuName, e);

// ✅ 正确：包含业务标识和上下文
log.warn("验证码错误: email={}, inputCode={}, requestId={}",
    email, code, requestId);

// ✅ 正确：记录操作耗时
long startTime = System.currentTimeMillis();
// ... 业务逻辑 ...
log.info("查询用户列表完成: userId={}, count={}, duration={}ms",
    userId, count, System.currentTimeMillis() - startTime);
```

## 性能日志规范

- **慢操作日志**：执行时间超过 500ms 的操作应该记录 WARN 日志
- **非常慢操作日志**：执行时间超过 1000ms 的操作应该记录 ERROR 日志
- **记录性能上下文**：记录操作耗时、影响的数据量等性能指标

### 性能日志示例
```java
long startTime = System.currentTimeMillis();
// ... 业务逻辑 ...
long duration = System.currentTimeMillis() - startTime;

if (duration > 1000) {
    log.error("[性能警告] 查询用户列表耗时过长: duration={}ms, count={}", duration, count);
} else if (duration > 500) {
    log.warn("[性能警告] 查询用户列表耗时: duration={}ms, count={}", duration, count);
}
```
