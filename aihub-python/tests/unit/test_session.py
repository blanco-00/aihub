import pytest
from unittest.mock import MagicMock, patch
import json


class TestSessionManager:
    @patch('aihub.agents.session.redis_client')
    def test_create_session(self, mock_redis):
        from aihub.agents.session import SessionManager
        
        mock_redis.setex.return_value = True
        
        manager = SessionManager()
        session_id = manager.create_session(user_id=1, agent_id="test-agent")
        
        assert session_id == "session:1:test-agent"
        mock_redis.setex.assert_called_once()

    @patch('aihub.agents.session.redis_client')
    def test_get_session(self, mock_redis):
        from aihub.agents.session import SessionManager
        
        test_messages = [{"role": "user", "content": "hello"}]
        mock_redis.get.return_value = json.dumps(test_messages)
        
        manager = SessionManager()
        messages = manager.get_session("session:1:test")
        
        assert messages == test_messages

    @patch('aihub.agents.session.redis_client')
    def test_get_session_empty(self, mock_redis):
        from aihub.agents.session import SessionManager
        
        mock_redis.get.return_value = None
        
        manager = SessionManager()
        messages = manager.get_session("session:1:test")
        
        assert messages == []

    @patch('aihub.agents.session.redis_client')
    def test_add_message(self, mock_redis):
        from aihub.agents.session import SessionManager
        
        mock_redis.get.return_value = json.dumps([])
        
        manager = SessionManager()
        manager.add_message("session:1:test", "user", "Hello")
        
        mock_redis.setex.assert_called()

    @patch('aihub.agents.session.redis_client')
    def test_clear_session(self, mock_redis):
        from aihub.agents.session import SessionManager
        
        manager = SessionManager()
        manager.clear_session("session:1:test")
        
        mock_redis.delete.assert_called_once_with("session:1:test")
