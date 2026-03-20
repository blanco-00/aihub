import pytest
from unittest.mock import patch, MagicMock


class TestConfig:
    def test_default_settings(self):
        with patch.dict('os.environ', {}, clear=True):
            from aihub.config import Settings
            settings = Settings()
            assert settings.app_name == "AIHub Python"
            assert settings.debug is False
            assert settings.db_host == "localhost"
            assert settings.db_port == 3306
            assert settings.redis_host == "localhost"
            assert settings.redis_port == 6379

    def test_env_override(self):
        with patch.dict('os.environ', {
            'DB_HOST': 'testdb',
            'DB_PORT': '3307',
            'REDIS_HOST': 'testredis'
        }, clear=True):
            from aihub.config import Settings
            settings = Settings()
            assert settings.db_host == "testdb"
            assert settings.db_port == 3307
            assert settings.redis_host == "testredis"
