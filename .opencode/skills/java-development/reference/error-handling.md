# Error Handling

## 异常处理原则

### 使用 BusinessException

对于业务异常，应该使用 `BusinessException` 而不是通用的 RuntimeException 或其他异常类。

### 正确示例
```java
@Transactional(rollbackFor = Exception.class)
public void createUser(CreateUserRequest request) {
    // 检查用户名是否已存在
    User existingUser = userMapper.findByUsername(request.getUsername());
    if (existingUser != null && existingUser.getIsDeleted() == 0) {
        throw new BusinessException("用户名已存在");
    }

    // 检查邮箱是否已存在
    User existingEmail = userMapper.findByEmail(request.getEmail());
    if (existingEmail != null && existingEmail.getIsDeleted() == 0) {
        throw new BusinessException("邮箱已被注册");
    }

    // ... 业务逻辑 ...
}
```

## 事务管理

### @Transactional 注解

所有写操作（INSERT, UPDATE, DELETE）都应该使用 `@Transactional` 注解。

**必需参数**：
```java
@Transactional(rollbackFor = Exception.class)
```

**为什么需要 `rollbackFor = Exception.class`**：
- Spring 默认只对 `RuntimeException` 和 `Error` 回滚
- 如果抛出 checked exception（如自定义业务异常），事务不会回滚
- 显式指定 `rollbackFor = Exception.class` 确保任何异常都回滚

### 正确示例
```java
@Service
public class UserServiceImpl implements UserService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(CreateUserRequest request) {
        // 多个数据库操作
        User user = new User();
        userMapper.insert(user);

        // 如果这里抛出异常，上面的 insert 会自动回滚
        if (user.getUsername().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }

        userRoleMapper.insert(user.getId(), roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UpdateUserRequest request) {
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        // 更新操作
        user.setUsername(request.getUsername());
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        // 逻辑删除
        user.setIsDeleted(1);
        userMapper.updateById(user);
    }
}
```

## 异常消息规范

### 异常消息应该

1. **清晰准确**：准确描述问题，让用户能够理解
2. **中文提示**：面向用户的异常消息使用中文
3. **包含上下文**：如果可能，包含相关对象的标识（如用户名、ID等）

### 好的异常消息示例
```java
throw new BusinessException("用户名已存在");
throw new BusinessException("邮箱已被注册");
throw new BusinessException("用户不存在");
throw new BusinessException("不能删除最后一个超级管理员");
throw new BusinessException("验证码已过期，请重新获取");
throw new BusinessException("菜单不存在: " + menuId);
```

### 错误的异常消息示例
```java
// ❌ 错误：信息不够清晰
throw new BusinessException("操作失败");

// ❌ 错误：包含技术细节，用户无法理解
throw new BusinessException("Database constraint violation: UNIQUE constraint failed on user.username");

// ❌ 错误：缺少上下文
throw new BusinessException("记录不存在");
```

## Try-Catch 模式

### Service 层异常处理

Service 层通常不需要 try-catch，因为：
1. 异常应该抛出到 Controller 层
2. Controller 层通过全局异常处理器统一处理
3. `@Transactional` 会自动在异常时回滚

### 正确示例
```java
@Service
public class UserServiceImpl implements UserService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(CreateUserRequest request) {
        // ✅ 正确：直接抛出异常，不做 try-catch
        User existing = userMapper.findByUsername(request.getUsername());
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }

        userMapper.insert(user);
    }
}
```

### Controller 层异常处理

Controller 层也不需要 try-catch，因为：
1. 全局异常处理器会捕获所有异常
2. 统一返回 `Result<Void>` 格式

### 正确示例
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public Result<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        // ✅ 正确：直接调用 service，不做 try-catch
        userService.createUser(request);
        return Result.success();
    }
}
```

### 什么时候需要 Try-Catch

只有以下情况需要 try-catch：

1. **调用外部系统或第三方 API**：需要记录详细日志
2. **需要在异常后执行清理操作**：如释放资源
3. **需要转换异常类型**：将底层异常转换为业务异常

### 示例：调用外部 API
```java
@Service
public class ExternalApiServiceImpl implements ExternalApiService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncUserData(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        try {
            // 调用外部 API
            ExternalApiResponse response = externalApiClient.syncUser(user);

            // 处理响应
            user.setExternalId(response.getUserId());
            userMapper.updateById(user);

            log.info("同步用户数据成功: userId={}, externalId={}", userId, response.getUserId());

        } catch (ExternalApiException e) {
            // 记录详细错误日志
            log.error("同步用户数据失败: userId={}, error={}", userId, e.getMessage(), e);

            // 转换为业务异常
            throw new BusinessException("同步用户数据失败: " + e.getMessage());
        }
    }
}
```

## 全局异常处理器

项目应该有一个全局异常处理器来统一处理所有异常。

### 示例（GlobalExceptionHandler）
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", errorMessage);
        return Result.fail(errorMessage);
    }

    // 系统异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail("系统错误，请联系管理员");
    }
}
```

## 检查清单

提交代码前，检查：

- [ ] 业务异常使用 `BusinessException`
- [ ] 所有写操作使用 `@Transactional(rollbackFor = Exception.class)`
- [ ] 异常消息清晰准确，使用中文
- [ ] 异常消息包含相关上下文（如用户名、ID等）
- [ ] Service 层和 Controller 层不做过度的 try-catch
- [ ] 全局异常处理器已配置
