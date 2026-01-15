package com.aihub.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * 封装常用的 Redis 操作，提供简洁易用的 API
 */
@Slf4j
@Component
public class RedisUtil {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // ============================== 通用操作 ==============================
    
    /**
     * 判断 key 是否存在
     * @param key 键
     * @return true 存在 false 不存在
     */
    public boolean hasKey(String key) {
        long startTime = System.currentTimeMillis();
        try {
            boolean result = Boolean.TRUE.equals(redisTemplate.hasKey(key));
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 50) {
                log.warn("Redis hasKey 操作耗时过长: {}ms, key={} (超过50ms，可能存在性能问题)", duration, key);
            }
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("判断 key 是否存在失败: key={}, 耗时={}ms", key, duration, e);
            return false;
        }
    }
    
    /**
     * 删除 key
     * @param key 键（可以传一个或多个）
     * @return 删除的 key 数量
     */
    public long delete(String... key) {
        try {
            if (key != null && key.length > 0) {
                if (key.length == 1) {
                    Boolean result = redisTemplate.delete(key[0]);
                    return Boolean.TRUE.equals(result) ? 1 : 0;
                } else {
                    return redisTemplate.delete(Arrays.asList(key));
                }
            }
            return 0;
        } catch (Exception e) {
            log.error("删除 key 失败: keys={}", (Object) key, e);
            return 0;
        }
    }
    
    /**
     * 设置 key 的过期时间
     * @param key 键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return true 成功 false 失败
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            log.error("设置 key 过期时间失败: key={}, timeout={}, unit={}", key, timeout, unit, e);
            return false;
        }
    }
    
    /**
     * 获取 key 的过期时间
     * @param key 键
     * @return 过期时间（秒），-1 表示永久有效，-2 表示 key 不存在
     */
    public long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("获取 key 过期时间失败: key={}", key, e);
            return -2;
        }
    }
    
    // ============================== String 操作 ==============================
    
    /**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        try {
            return key == null ? null : redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取缓存失败: key={}", key, e);
            return null;
        }
    }
    
    /**
     * 普通缓存获取（指定类型）
     * @param key 键
     * @param clazz 类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = get(key);
            if (value == null) {
                return null;
            }
            if (clazz.isInstance(value)) {
                return (T) value;
            }
            log.warn("缓存值类型不匹配: key={}, expected={}, actual={}", key, clazz, value.getClass());
            return null;
        } catch (Exception e) {
            log.error("获取缓存失败: key={}, clazz={}", key, clazz, e);
            return null;
        }
    }
    
    /**
     * 普通缓存放入
     * @param key 键
     * @param value 值
     * @return true 成功 false 失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
            return false;
        }
    }
    
    /**
     * 普通缓存放入并设置过期时间
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return true 成功 false 失败
     */
    public boolean set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            return true;
        } catch (Exception e) {
            log.error("设置缓存（带过期时间）失败: key={}, timeout={}, unit={}", key, timeout, unit, e);
            return false;
        }
    }
    
    /**
     * 普通缓存放入并设置过期时间（秒）
     * @param key 键
     * @param value 值
     * @param timeoutSeconds 过期时间（秒）
     * @return true 成功 false 失败
     */
    public boolean set(String key, Object value, long timeoutSeconds) {
        return set(key, value, timeoutSeconds, TimeUnit.SECONDS);
    }
    
    /**
     * 递增
     * @param key 键
     * @param delta 要增加几（大于0）
     * @return 递增后的值
     */
    public long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("递增失败: key={}, delta={}", key, delta, e);
            return 0;
        }
    }
    
    /**
     * 递减
     * @param key 键
     * @param delta 要减少几（大于0）
     * @return 递减后的值
     */
    public long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("递减失败: key={}, delta={}", key, delta, e);
            return 0;
        }
    }
    
    // ============================== Hash 操作 ==============================
    
    /**
     * HashGet
     * @param key 键（不能为 null）
     * @param item 项（不能为 null）
     * @return 值
     */
    public Object hGet(String key, String item) {
        try {
            return redisTemplate.opsForHash().get(key, item);
        } catch (Exception e) {
            log.error("获取 Hash 值失败: key={}, item={}", key, item, e);
            return null;
        }
    }
    
    /**
     * 获取 hashKey 对应的所有键值
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("获取 Hash 所有值失败: key={}", key, e);
            return null;
        }
    }
    
    /**
     * HashSet
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hSetAll(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error("设置 Hash 所有值失败: key={}", key, e);
            return false;
        }
    }
    
    /**
     * HashSet 并设置过期时间
     * @param key 键
     * @param map 对应多个键值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return true 成功 false 失败
     */
    public boolean hSetAll(String key, Map<String, Object> map, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            expire(key, timeout, unit);
            return true;
        } catch (Exception e) {
            log.error("设置 Hash 所有值（带过期时间）失败: key={}", key, e);
            return false;
        }
    }
    
    /**
     * 向一张 hash 表中放入数据，如果不存在将创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @return true 成功 false 失败
     */
    public boolean hSet(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error("设置 Hash 值失败: key={}, item={}", key, item, e);
            return false;
        }
    }
    
    /**
     * 删除 hash 表中的值
     * @param key 键（不能为 null）
     * @param item 项（可以多个，不能为 null）
     * @return 删除的项数
     */
    public long hDelete(String key, Object... item) {
        try {
            return redisTemplate.opsForHash().delete(key, item);
        } catch (Exception e) {
            log.error("删除 Hash 值失败: key={}, items={}", key, (Object) item, e);
            return 0;
        }
    }
    
    /**
     * 判断 hash 表中是否有该项的值
     * @param key 键（不能为 null）
     * @param item 项（不能为 null）
     * @return true 存在 false 不存在
     */
    public boolean hHasKey(String key, String item) {
        try {
            return redisTemplate.opsForHash().hasKey(key, item);
        } catch (Exception e) {
            log.error("判断 Hash 项是否存在失败: key={}, item={}", key, item, e);
            return false;
        }
    }
    
    // ============================== Set 操作 ==============================
    
    /**
     * 根据 key 获取 Set 中的所有值
     * @param key 键
     * @return Set 中的所有值
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("获取 Set 所有值失败: key={}", key, e);
            return null;
        }
    }
    
    /**
     * 根据 value 从一个 set 中查询，是否存在
     * @param key 键
     * @param value 值
     * @return true 存在 false 不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("判断 Set 值是否存在失败: key={}, value={}", key, value, e);
            return false;
        }
    }
    
    /**
     * 将数据放入 set 缓存
     * @param key 键
     * @param values 值（可以多个）
     * @return 成功添加的个数
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("设置 Set 值失败: key={}", key, e);
            return 0;
        }
    }
    
    /**
     * 将数据放入 set 缓存并设置过期时间
     * @param key 键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @param values 值（可以多个）
     * @return 成功添加的个数
     */
    public long sSet(String key, long timeout, TimeUnit unit, Object... values) {
        try {
            long count = redisTemplate.opsForSet().add(key, values);
            if (count > 0) {
                expire(key, timeout, unit);
            }
            return count;
        } catch (Exception e) {
            log.error("设置 Set 值（带过期时间）失败: key={}", key, e);
            return 0;
        }
    }
    
    /**
     * 获取 set 缓存的长度
     * @param key 键
     * @return 长度
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("获取 Set 长度失败: key={}", key, e);
            return 0;
        }
    }
    
    /**
     * 移除值为 value 的
     * @param key 键
     * @param values 值（可以多个）
     * @return 移除的个数
     */
    public long sRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error("移除 Set 值失败: key={}", key, e);
            return 0;
        }
    }
    
    // ============================== List 操作 ==============================
    
    /**
     * 获取 list 缓存的内容
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置（0 到 -1 代表所有值）
     * @return List
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("获取 List 值失败: key={}, start={}, end={}", key, start, end, e);
            return null;
        }
    }
    
    /**
     * 获取 list 缓存的长度
     * @param key 键
     * @return 长度
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("获取 List 长度失败: key={}", key, e);
            return 0;
        }
    }
    
    /**
     * 通过索引获取 list 中的值
     * @param key 键
     * @param index 索引（index >= 0 时，0 表头，1 第二个元素，依次类推；index < 0 时，-1 表尾，-2 倒数第二个元素，依次类推）
     * @return 值
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("获取 List 索引值失败: key={}, index={}", key, index, e);
            return null;
        }
    }
    
    /**
     * 将 list 放入缓存
     * @param key 键
     * @param value 值
     * @return true 成功 false 失败
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("设置 List 值失败: key={}", key, e);
            return false;
        }
    }
    
    /**
     * 将 list 放入缓存并设置过期时间
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return true 成功 false 失败
     */
    public boolean lSet(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            expire(key, timeout, unit);
            return true;
        } catch (Exception e) {
            log.error("设置 List 值（带过期时间）失败: key={}", key, e);
            return false;
        }
    }
    
    /**
     * 将 list 放入缓存
     * @param key 键
     * @param value 值
     * @return true 成功 false 失败
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.error("设置 List 所有值失败: key={}", key, e);
            return false;
        }
    }
    
    /**
     * 将 list 放入缓存并设置过期时间
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return true 成功 false 失败
     */
    public boolean lSet(String key, List<Object> value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            expire(key, timeout, unit);
            return true;
        } catch (Exception e) {
            log.error("设置 List 所有值（带过期时间）失败: key={}", key, e);
            return false;
        }
    }
    
    /**
     * 根据索引修改 list 中的某条数据
     * @param key 键
     * @param index 索引
     * @param value 值
     * @return true 成功 false 失败
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error("更新 List 索引值失败: key={}, index={}", key, index, e);
            return false;
        }
    }
    
    /**
     * 移除 N 个值为 value 的元素
     * @param key 键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            log.error("移除 List 值失败: key={}, count={}, value={}", key, count, value, e);
            return 0;
        }
    }
}
