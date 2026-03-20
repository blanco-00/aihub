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

/**
 * 请求时间记录过滤器
 * 用于记录请求到达服务器的时间，帮助定位性能问题
 * 优先级最高，在拦截器之前执行
 */
@Slf4j
@Component
@Order(0)  // 最高优先级，确保最先执行
public class RequestTimingFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("RequestTimingFilter 初始化完成");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String requestPath = httpRequest.getRequestURI();
            String method = httpRequest.getMethod();
            
            // 记录请求到达服务器的时间（最早的时间点）
            long requestArrivalTime = System.currentTimeMillis();
            String requestId = method + "_" + requestPath + "_" + requestArrivalTime;
            
            // 将请求ID和到达时间存储到request attribute中，供后续使用
            httpRequest.setAttribute("__requestId", requestId);
            httpRequest.setAttribute("__requestArrivalTime", requestArrivalTime);
            
            // 不记录每个请求的详细信息，只在性能问题时记录
            
            // 继续执行过滤器链
            chain.doFilter(request, response);
            
            // 请求处理完成后的时间
            long requestCompleteTime = System.currentTimeMillis();
            long totalTime = requestCompleteTime - requestArrivalTime;
            
            // 在响应头中添加时间信息，供前端代理层追踪
            if (response instanceof jakarta.servlet.http.HttpServletResponse) {
                jakarta.servlet.http.HttpServletResponse httpResponse = (jakarta.servlet.http.HttpServletResponse) response;
                httpResponse.setHeader("X-Response-Time", totalTime + "ms");
                httpResponse.setHeader("X-Request-Id", requestId);
                httpResponse.setHeader("X-Request-Arrival-Time", String.valueOf(requestArrivalTime));
            }
            
            // 只记录性能警告（超过1秒的请求）
            if (totalTime > 1000) {
                log.warn("[性能警告] {} 请求总耗时过长: {}ms, {} {}", requestId, totalTime, method, requestPath);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
    
    @Override
    public void destroy() {
        log.info("RequestTimingFilter 销毁");
    }
}
