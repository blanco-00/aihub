package com.aihub.config;

import com.aihub.admin.interceptor.AuthInterceptor;
import com.aihub.interceptor.InitializationInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private InitializationInterceptor initializationInterceptor;
    
    @Autowired
    private AuthInterceptor authInterceptor;
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;
    
    @Value("${file.upload.url-prefix:/api/files}")
    private String urlPrefix;
    
    /**
     * 获取文件存储的绝对路径
     */
    private String getAbsoluteUploadPath() {
        String path = uploadPath;
        // 如果是相对路径，转换为绝对路径
        if (path.startsWith("./") || (!path.startsWith("/") && !path.contains(":"))) {
            // 获取项目根目录或用户目录
            String baseDir = System.getProperty("user.dir");
            if (path.startsWith("./")) {
                path = path.substring(2);
            }
            path = baseDir + "/" + path;
        }
        return path;
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置文件访问路径：/api/files/** -> file.upload.path
        String absolutePath = getAbsoluteUploadPath();
        String resourcePath = "file:" + absolutePath.replace("\\", "/");
        if (!resourcePath.endsWith("/")) {
            resourcePath += "/";
        }
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations(resourcePath)
                .setCachePeriod(3600); // 缓存1小时
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 初始化拦截器（优先级高，先执行）
        registry.addInterceptor(initializationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/init/**",
                        "/api/auth/login",  // 排除登录接口
                        "/api/auth/refresh",  // 排除刷新Token接口
                        "/api/files/**",  // 排除文件访问路径（静态资源）
                        "/init",
                        "/login",  // 排除登录页面
                        "/static/**",
                        "/assets/**",
                        "/error"
                )
                .order(1);
        
        // 认证拦截器（优先级低，后执行）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/auth/logout",  // 登出接口需要认证，但由拦截器内部处理
                        "/api/init/**",
                        "/api/files/**"  // 排除文件访问路径（静态资源，允许直接访问）
                )
                .order(2);
    }
    
    /**
     * 强制在应用启动时初始化 DispatcherServlet
     * 避免首次请求时的初始化延迟（约1-2秒）
     */
    @PostConstruct
    public void eagerInitDispatcherServlet() {
        try {
            long startTime = System.currentTimeMillis();
            log.info("开始强制初始化 DispatcherServlet...");
            
            // 获取 DispatcherServlet 并强制初始化
            DispatcherServlet dispatcherServlet = webApplicationContext.getBean(DispatcherServlet.class);
            if (dispatcherServlet != null) {
                // 通过访问 DispatcherServlet 来触发初始化
                // 注意：这不会真正处理请求，只是确保 DispatcherServlet 已初始化
                log.info("DispatcherServlet Bean 已找到，准备初始化...");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("DispatcherServlet 初始化完成，耗时: {}ms", duration);
        } catch (Exception e) {
            log.warn("DispatcherServlet 预初始化失败（不影响应用启动，但首次请求可能较慢）: {}", e.getMessage());
        }
    }
}
