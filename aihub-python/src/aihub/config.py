from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import Optional


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env")
    
    app_name: str = "AIHub Python"
    debug: bool = False

    db_host: str = "localhost"
    db_port: int = 3306
    db_name: str = "aihub"
    db_user: str = "aihub"
    db_password: str = ""

    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_password: str = ""

    jwt_secret: str = "change-me-in-production"
    jwt_algorithm: str = "HS256"


settings = Settings()
