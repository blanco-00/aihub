package com.aihub.admin.aspect;

import com.aihub.admin.annotation.OperationLog;
import com.aihub.admin.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 操作日志切面
 * 自动记录标注了 @OperationLog 注解的方法的操作日志
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {
    
    @Autowired
    private OperationLogService operationLogService;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 定义切点：拦截所有标注了 @OperationLog 注解的方法
     */
    @Pointcut("@annotation(com.aihub.admin.annotation.OperationLog)")
    public void operationLogPointcut() {
    }
    
    /**
     * 环绕通知：记录操作日志
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Integer status = 1; // 1-成功，0-失败
        String errorMessage = null;
        
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = 0; // 失败
            errorMessage = e.getMessage();
            throw e;
        } finally {
            try {
                // 记录操作日志（异步执行，不阻塞主流程）
                recordOperationLog(joinPoint, result, status, errorMessage, startTime);
            } catch (Exception e) {
                // 记录日志失败不影响业务流程
                log.error("记录操作日志失败", e);
            }
        }
    }
    
    /**
     * 记录操作日志
     */
    private void recordOperationLog(ProceedingJoinPoint joinPoint, Object result, 
                                    Integer status, String errorMessage, long startTime) {
        // 获取请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        
        // 获取方法签名和注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLogAnnotation = method.getAnnotation(OperationLog.class);
        
        if (operationLogAnnotation == null) {
            return;
        }
        
        // 获取用户信息（优先从 request attribute 获取，因为 AuthInterceptor 已经设置）
        Long userId = null;
        String username = null;
        try {
            // 从 request attribute 获取（AuthInterceptor 已设置）
            Object userIdObj = request.getAttribute("userId");
            Object usernameObj = request.getAttribute("username");
            
            if (userIdObj instanceof Long) {
                userId = (Long) userIdObj;
            } else if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).longValue();
            }
            
            if (usernameObj instanceof String) {
                username = (String) usernameObj;
            }
            
            // 如果从 request attribute 获取不到，尝试从 MDC 获取
            if (userId == null) {
                String userIdStr = MDC.get("userId");
                if (userIdStr != null && !userIdStr.isEmpty()) {
                    try {
                        userId = Long.parseLong(userIdStr);
                    } catch (NumberFormatException e) {
                        log.debug("从 MDC 解析 userId 失败: {}", userIdStr);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取用户信息失败，可能未登录", e);
        }
        
        // 获取操作信息
        String module = operationLogAnnotation.module();
        String operation = operationLogAnnotation.operation();
        
        // 如果模块或操作类型为空，使用默认值
        if (module == null || module.isEmpty()) {
            // 从类名提取模块名（去掉Controller后缀）
            String className = joinPoint.getTarget().getClass().getSimpleName();
            module = className.replace("Controller", "");
        }
        if (operation == null || operation.isEmpty()) {
            // 从方法名提取操作类型
            operation = method.getName();
        }
        
        // 获取请求信息
        String methodName = request.getMethod();
        String url = request.getRequestURI();
        
        // 获取请求参数
        String params = null;
        if (operationLogAnnotation.recordParams()) {
            try {
                Object[] args = joinPoint.getArgs();
                // 过滤掉 HttpServletRequest 和 HttpServletResponse 等对象
                Object[] filteredArgs = Arrays.stream(args)
                    .filter(arg -> !(arg instanceof HttpServletRequest) && 
                                  !(arg instanceof jakarta.servlet.http.HttpServletResponse))
                    .toArray();
                if (filteredArgs.length > 0) {
                    params = objectMapper.writeValueAsString(filteredArgs);
                    // 限制参数长度，避免过长
                    if (params != null && params.length() > 2000) {
                        params = params.substring(0, 2000) + "...";
                    }
                }
            } catch (Exception e) {
                log.debug("序列化请求参数失败", e);
            }
        }
        
        // 获取操作结果
        String resultStr = null;
        if (operationLogAnnotation.recordResult() && result != null) {
            try {
                resultStr = objectMapper.writeValueAsString(result);
                // 限制结果长度，避免过长
                if (resultStr != null && resultStr.length() > 2000) {
                    resultStr = resultStr.substring(0, 2000) + "...";
                }
            } catch (Exception e) {
                log.debug("序列化操作结果失败", e);
            }
        }
        
        // 如果有错误信息，添加到结果中
        if (errorMessage != null) {
            resultStr = errorMessage;
        }
        
        // 计算耗时
        long duration = System.currentTimeMillis() - startTime;
        
        // 异步记录操作日志
        operationLogService.recordOperation(
            userId, username, module, operation, methodName, url, 
            params, resultStr, status, (int) duration, request
        );
    }
}
