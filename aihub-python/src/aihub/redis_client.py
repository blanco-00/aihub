"""
Redis 客户端封装

Redis 是一种高性能的内存数据库，常用于：
1. 缓存：热点数据存储
2. 会话存储：用户会话数据
3. 消息队列：异步任务
4. 实时排行：有序集合

在这个项目中，Redis 用于：
- 存储 AI 对话的会话历史
- 利用 TTL 实现自动过期

学习要点：
1. redis-py 库的基本用法
2. 连接池概念
3. decode_responses 参数
"""

# ============================================================
# 导入
# ============================================================

import redis
# redis: Python Redis 客户端库
# 官方推荐的 Redis Python 客户端

from .config import settings
# settings: 配置对象，从环境变量读取


# ============================================================
# Redis 客户端工厂函数
# ============================================================

def get_redis_client():
    """
    创建 Redis 客户端实例
    
    配置参数说明：
    - host: Redis 服务器地址
    - port: Redis 端口（默认 6379）
    - password: 密码（如果没有则为空）
    - decode_responses=True: 自动将响应解码为字符串
    
    为什么需要 decode_responses=True？
    - Redis 存储的是字节
    - True: 自动转成字符串，使用更方便
    - False: 返回字节，需要手动 decode
    
    Returns:
        redis.Redis: Redis 客户端实例
    """
    client = redis.Redis(
        host=settings.redis_host,      # 从配置读取
        port=settings.redis_port,      # 从配置读取
        password=settings.redis_password,  # 从配置读取（可能为空）
        decode_responses=True  # 自动解码为字符串
    )
    return client


# ============================================================
# 全局 Redis 客户端单例
# ============================================================

"""
模块级单例

整个应用共用一个 Redis 客户端连接。

为什么不每次请求都创建新连接？
- 创建连接开销大（网络 + 握手）
- 连接池复用已有连接
- redis-py 默认使用连接池

redis-py 连接池：
- 管理多个 Redis 连接
- 线程安全
- 自动处理连接断开重连
"""

redis_client = get_redis_client()
