"""
SessionManager - Redis 会话管理器

这个模块管理 AI 对话的上下文历史。

核心概念：
1. 会话 (Session)：一次完整的对话上下文
2. 历史 (History)：对话中的所有消息
3. Redis：高性能内存数据库，用于存储会话数据

为什么用 Redis 而不是 MySQL？
- Redis 读写速度极快（内存数据库）
- 自动过期 (TTL) 功能，适合临时会话
- 支持复杂数据结构

学习要点：
1. Redis 基本操作 (get, setex, delete)
2. JSON 序列化/反序列化
3. 面向对象封装
"""

# ============================================================
# 导入
# ============================================================

from typing import Dict, List, Any
# typing 模块提供类型注解

import json
# json 模块：将 Python 对象 <-> JSON 字符串

from ..redis_client import redis_client
# redis_client: 自定义的 Redis 客户端封装


# ============================================================
# SessionManager 类
# ============================================================

class SessionManager:
    """
    会话管理器
    
    职责：
    1. 创建新会话
    2. 获取会话历史
    3. 添加消息到历史
    4. 清空会话
    
    会话 ID 格式：
    "session:{user_id}:{agent_id}"
    如 "session:123:default"
    
    Redis 数据存储：
    Key: session:123:default
    Value: JSON 字符串，如 '[{"role": "user", "content": "你好"}, ...]'
    TTL: 3600 秒（1小时）
    
    属性:
        ttl: 会话过期时间（秒），默认 3600
    """
    
    def __init__(self):
        """
        初始化会话管理器
        
        ttl (Time To Live)：
        - Redis 的自动过期机制
        - 3600 秒 = 1 小时
        - 用户 1 小时不活动，会话自动删除
        - 节省内存
        """
        self.ttl = 3600  # 1 小时过期
    
    def create_session(self, user_id: int, agent_id: str) -> str:
        """
        创建新会话
        
        会话 ID 格式："session:{user_id}:{agent_id}"
        初始消息列表为空数组 []
        
        参数:
            user_id: 用户 ID
            agent_id: Agent ID（可以理解为对话场景/模型 ID）
        
        返回:
            str: 新创建的会话 ID
        
        示例:
            session_id = manager.create_session(user_id=123, agent_id="default")
            # 返回 "session:123:default"
        """
        # 构造会话 ID
        session_id = f"session:{user_id}:{agent_id}"
        
        # Redis setex: 设置值并指定过期时间
        # 格式: setex(key, ttl_seconds, value)
        # json.dumps([]) 将空列表转为 "[]"
        redis_client.setex(session_id, self.ttl, json.dumps([]))
        
        return session_id
    
    def get_session(self, session_id: str) -> List[Any]:
        """
        获取会话的消息历史
        
        参数:
            session_id: 会话 ID
        
        返回:
            list: 消息历史列表
                  如果会话不存在，返回空列表 []
        
        示例:
            history = manager.get_session("session:123:default")
            # 返回 [{"role": "user", "content": "你好"}, ...]
        """
        # Redis get: 获取值
        data = redis_client.get(session_id)
        
        if data:
            # json.loads() 将 JSON 字符串转为 Python 对象
            return json.loads(data)
        
        # 会话不存在
        return []
    
    def add_message(self, session_id: str, role: str, content: str):
        """
        添加消息到会话历史
        
        步骤：
        1. 获取现有历史
        2. 追加新消息
        3. 重新存入 Redis（更新 TTL）
        
        参数:
            session_id: 会话 ID
            role: 消息角色 ("user", "assistant", "system")
            content: 消息内容
        
        示例:
            manager.add_message("session:123:default", "user", "你好")
            manager.add_message("session:123:default", "assistant", "你好！有什么可以帮你？")
        """
        # 1. 获取现有历史
        messages = self.get_session(session_id)
        
        # 2. 追加新消息
        messages.append({
            "role": role,      # 角色：user/assistant/system
            "content": content  # 内容
        })
        
        # 3. 重新存入 Redis
        # 注意：每次添加消息都会重置 TTL
        # 如果用户持续对话，会话不会过期
        redis_client.setex(session_id, self.ttl, json.dumps(messages))
    
    def clear_session(self, session_id: str):
        """
        清空/删除会话
        
        参数:
            session_id: 会话 ID
        """
        redis_client.delete(session_id)


# ============================================================
# 全局单例实例
# ============================================================

"""
模块级单例

整个应用共用一个 SessionManager 实例
确保会话数据集中管理
"""

session_manager = SessionManager()
