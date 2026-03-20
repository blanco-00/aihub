from typing import Dict, List, Any
import json
from ..redis_client import redis_client


class SessionManager:
    def __init__(self):
        self.ttl = 3600

    def create_session(self, user_id: int, agent_id: str) -> str:
        session_id = f"session:{user_id}:{agent_id}"
        redis_client.setex(session_id, self.ttl, json.dumps([]))
        return session_id

    def get_session(self, session_id: str) -> List[Any]:
        data = redis_client.get(session_id)
        if data:
            return json.loads(data)
        return []

    def add_message(self, session_id: str, role: str, content: str):
        messages = self.get_session(session_id)
        messages.append({"role": role, "content": content})
        redis_client.setex(session_id, self.ttl, json.dumps(messages))

    def clear_session(self, session_id: str):
        redis_client.delete(session_id)


session_manager = SessionManager()
