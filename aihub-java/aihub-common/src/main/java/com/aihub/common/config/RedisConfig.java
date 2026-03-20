package com.aihub.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 */
@Slf4j
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 使用 Jackson2JsonRedisSerializer 来序列化和反序列化 redis 的 value 值
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        
        // 使用 StringRedisSerializer 来序列化和反序列化 redis 的 key 值
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        
        // key 采用 String 的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash 的 key 也采用 String 的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value 序列化方式采用 jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash 的 value 序列化方式采用 jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * 预热 Redis 连接（应用启动完成后执行一次连接测试，避免首次请求慢）
     * 使用 CommandLineRunner 确保在所有 Bean 初始化完成后再执行
     */
    @Bean
    public CommandLineRunner redisWarmupRunner(RedisConnectionFactory connectionFactory) {
        return args -> {
            try {
                long startTime = System.currentTimeMillis();
                log.info("开始预热 Redis 连接...");
                
                // 预热连接：执行多次 ping 操作，确保连接池已建立连接
                int successCount = 0;
                for (int i = 0; i < 5; i++) {
                    try {
                        var connection = connectionFactory.getConnection();
                        connection.ping();
                        connection.close();
                        successCount++;
                        log.debug("Redis 连接预热第{}次成功", i + 1);
                    } catch (Exception e) {
                        log.warn("Redis 连接预热第{}次失败: {}", i + 1, e.getMessage());
                    }
                }
                
                long duration = System.currentTimeMillis() - startTime;
                if (successCount == 0) {
                    log.error("Redis 连接预热失败：所有尝试都失败了，请检查 Redis 服务是否运行");
                } else if (duration > 500) {
                    log.warn("Redis 连接预热耗时较长: {}ms (成功: {}/5)，建议检查Redis连接配置", duration, successCount);
                } else {
                    log.info("Redis 连接预热成功，耗时: {}ms (成功: {}/5)", duration, successCount);
                }
            } catch (Exception e) {
                log.error("Redis 连接预热异常: {}", e.getMessage(), e);
            }
        };
    }
}
