package com.aihub.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;

/**
 * 请求日志过滤器
 * 记录每个HTTP请求的详细信息，方便问题追踪
 */
@Slf4j
@Component
@Order(2)
public class RequestLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("RequestLoggingFilter 初始化完成");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // 记录请求信息
            logRequest(httpRequest);

            long startTime = System.currentTimeMillis();

            try {
                chain.doFilter(request, response);
            } finally {
                long duration = System.currentTimeMillis() - startTime;

                // 记录响应信息
                logResponse(httpRequest, duration);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * 记录请求信息
     */
    private void logRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        StringBuilder logMessage = new StringBuilder();
        logMessage.append(">>> 请求: ").append(method).append(" ").append(uri);

        if (queryString != null && !queryString.isEmpty()) {
            logMessage.append("?").append(queryString);
        }

        // 记录用户ID（如果已认证）
        Object userId = request.getAttribute("userId");
        Object username = request.getAttribute("username");
        if (userId != null) {
            logMessage.append(" | 用户ID: ").append(userId);
        }
        if (username != null) {
            logMessage.append(" | 用户名: ").append(username);
        }

        // 记录Content-Type
        String contentType = request.getContentType();
        if (contentType != null) {
            logMessage.append(" | Content-Type: ").append(contentType);
        }

        log.debug(logMessage.toString());
    }

    /**
     * 记录响应信息
     */
    private void logResponse(HttpServletRequest request, long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("<<< 响应: ").append(method).append(" ").append(uri);
        logMessage.append(" | 耗时: ").append(duration).append("ms");

        if (duration > 1000) {
            log.warn(logMessage.toString());
        } else if (duration > 500) {
            log.info(logMessage.toString());
        } else {
            log.debug(logMessage.toString());
        }
    }

    @Override
    public void destroy() {
        log.info("RequestLoggingFilter 销毁");
    }
}
