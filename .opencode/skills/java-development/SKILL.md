---
name: java-development
description: Guides Java development in AIHub project. Use when writing or modifying Java code, including Entity, Service, Controller, Mapper layers. Covers code style, logging patterns, error handling, and best practices for Spring Boot 3.2.0.
---

# Java Development Skill

## 我能帮助你做什么

### Java Code Development
- 遵循 AIHub 约定编写简洁、可维护的 Java 代码
- 正确的 import 组织，避免全限定名
- 方法长度控制和嵌套层级管理
- 代码抽象和复用模式

### Logging Best Practices
- 合适的日志级别使用（DEBUG, INFO, WARN, ERROR）
- 结构化日志，包含业务标识符
- 慢操作性能日志（>500ms）
- 避免重复日志

### Error Handling
- 使用 BusinessException 处理业务错误
- 正确的异常处理模式
- 使用 @Transactional 管理事务

### Code Quality
- 方法长度 < 50 行（最多 100 行）
- 嵌套层级 < 3 层
- Early Return 模式减少嵌套
- Clean Code 原则

## 快速参考

### Import 规则
- ❌ 永远不要使用全限定名：`com.aihub.entity.User user`
- ✅ 始终使用 import 语句：`import com.aihub.entity.User;`

### 命名约定
- **Classes**: PascalCase (`UserService`, `UserServiceImpl`)
- **Methods**: camelCase with action verbs (`getUserById`, `createUser`)
- **Variables**: camelCase (`userName`, `totalCount`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`)

### Method Guidelines
- 长度：< 50 行（最多 100）
- 嵌套：< 3 层
- 使用 early return 减少嵌套

## 详细参考

完整的规则和示例，请查看：

- **[Code Style & Conventions](reference/code-style.md)** - Import 规则、命名约定、代码简洁性
- **[Logging Guidelines](reference/logging.md)** - 日志级别、结构化日志、性能跟踪
- **[Error Handling](reference/error-handling.md)** - 异常模式、事务管理
- **[Examples](examples/controller-example.md)** - 完整示例（Controller, Service, Mapper）

## Common Patterns

### Service Layer Pattern
```java
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(CreateUserRequest request) {
        // Validation
        User existing = userMapper.findByUsername(request.getUsername());
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }

        // Business logic
        User user = new User();
        user.setUsername(request.getUsername());
        // ...

        // Logging
        userMapper.insert(user);
        log.info("用户创建成功: username={}", user.getUsername());
    }
}
```

### Controller Pattern
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public Result<PageResult<UserListResponse>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<UserListResponse> result = userService.getUserList(current, size);
        return Result.success(result);
    }
}
```

## Checklist

提交 Java 代码前，检查：

- [ ] 没有全限定名（使用 imports）
- [ ] 方法长度 < 50 行
- [ ] 嵌套层级 < 3 层
- [ ] 业务异常使用 BusinessException
- [ ] 写操作使用 @Transactional
- [ ] 结构化日志，包含业务标识符
- [ ] 慢操作（>500ms）有性能日志
- [ ] 日志中不包含敏感信息（密码、tokens）
- [ ] 删除未使用的代码

## Related Skills

- **[database-operations](../database-operations/)** - SQL 优化、MyBatis 模式
- **[feature-development](../feature-development/)** - 完整的功能开发流程
