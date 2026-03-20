import pytest
from unittest.mock import patch, MagicMock
from datetime import datetime, timedelta


class TestAuth:
    def test_create_access_token(self):
        with patch('aihub.auth.settings') as mock_settings:
            mock_settings.jwt_secret = "test-secret"
            mock_settings.jwt_algorithm = "HS256"
            
            from aihub.auth import create_access_token
            token = create_access_token({"user_id": 1})
            assert token is not None
            assert isinstance(token, str)

    def test_verify_token_valid(self):
        with patch('aihub.auth.settings') as mock_settings:
            mock_settings.jwt_secret = "test-secret"
            mock_settings.jwt_algorithm = "HS256"
            
            from aihub.auth import create_access_token, verify_token
            token = create_access_token({"user_id": 1})
            payload = verify_token(token)
            assert payload["user_id"] == 1

    def test_verify_token_invalid(self):
        with patch('aihub.auth.settings') as mock_settings:
            mock_settings.jwt_secret = "test-secret"
            mock_settings.jwt_algorithm = "HS256"
            
            from aihub.auth import verify_token
            from fastapi import HTTPException
            
            with pytest.raises(HTTPException) as exc_info:
                verify_token("invalid-token")
            assert exc_info.value.status_code == 401
