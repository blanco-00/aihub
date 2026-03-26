from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import Optional, Dict
import pymysql


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_prefix="aihub_python_")
    
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
    jwt_algorithm: str = "HS512"


settings = Settings()


def get_db_connection(autocommit: bool = True):
    return pymysql.connect(
        host=settings.db_host,
        port=settings.db_port,
        user=settings.db_user,
        password=settings.db_password,
        database=settings.db_name,
        charset='utf8mb4',
        autocommit=autocommit
    )


def get_model_configs_from_db() -> Dict[str, Dict[str, str]]:
    configs = {}
    try:
        conn = get_db_connection()
        with conn.cursor(pymysql.cursors.DictCursor) as cur:
            cur.execute("SELECT model_id, api_key, base_url FROM model_config WHERE status = 1")
            rows = cur.fetchall()
            for row in rows:
                configs[row["model_id"]] = {
                    "api_key": row["api_key"],
                    "base_url": row["base_url"]
                }
        conn.close()
    except Exception as e:
        print(f"Failed to load model configs from DB: {e}")
    
    return configs


MODEL_CONFIGS = get_model_configs_from_db()
