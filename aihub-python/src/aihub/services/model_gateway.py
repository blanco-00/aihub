import pymysql
import logging
from datetime import datetime
from typing import Dict, Optional, List
from dataclasses import dataclass

logger = logging.getLogger(__name__)


@dataclass
class TokenUsage:
    input_tokens: int
    output_tokens: int
    total_tokens: int
    duration_ms: int


class ModelGateway:
    def __init__(self):
        self._models: Dict[str, Dict] = {}
    
    def register_model(
        self,
        name: str,
        provider: str,
        model_id: str,
        api_key: str,
        base_url: Optional[str] = None,
        **kwargs
    ):
        self._models[name] = {
            "provider": provider,
            "model_id": model_id,
            "api_key": api_key,
            "base_url": base_url,
            "config": kwargs
        }
    
    def get_model(self, name: str):
        if name not in self._models:
            raise ValueError(f"Model '{name}' not found")
        
        model_info = self._models[name]
        return model_info
    
    def list_models(self) -> list:
        return [
            {
                "name": name,
                "provider": info["provider"],
                "model_id": info["model_id"]
            }
            for name, info in self._models.items()
        ]
    
    def _save_token_usage(
        self,
        user_id: int,
        model_name: str,
        provider: str,
        usage: TokenUsage,
        session_id: Optional[str] = None,
        request_id: Optional[str] = None
    ):
        try:
            from ..config import get_db_connection
            conn = get_db_connection()
            with conn.cursor() as cur:
                cur.execute(
                    """INSERT INTO token_usage_log 
                       (user_id, model_name, provider, input_tokens, output_tokens, session_id, request_id, duration_ms)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s)""",
                    (
                        user_id,
                        model_name,
                        provider,
                        usage.input_tokens,
                        usage.output_tokens,
                        session_id,
                        request_id,
                        usage.duration_ms
                    )
                )
            conn.close()
        except Exception as e:
            logger.error(f"Failed to save token usage: {e}")
    
    def get_token_stats(
        self,
        user_id: int,
        start_date: Optional[datetime] = None,
        end_date: Optional[datetime] = None
    ) -> Dict:
        try:
            from ..config import get_db_connection
            conn = get_db_connection()
            
            where_clauses = ["user_id = %s"]
            params: List[object] = [user_id]
            
            if start_date:
                where_clauses.append("created_at >= %s")
                params.append(start_date.strftime("%Y-%m-%d %H:%M:%S"))
            if end_date:
                where_clauses.append("created_at <= %s")
                params.append(end_date.strftime("%Y-%m-%d %H:%M:%S"))
            
            where_sql = " AND ".join(where_clauses)
            
            with conn.cursor() as cur:
                cur.execute(
                    f"""SELECT 
                           model_name,
                           SUM(input_tokens) as total_input,
                           SUM(output_tokens) as total_output,
                           SUM(input_tokens + output_tokens) as total_tokens,
                           COUNT(*) as request_count,
                           AVG(duration_ms) as avg_duration_ms
                       FROM token_usage_log 
                       WHERE {where_sql}
                       GROUP BY model_name""",
                    params
                )
                by_model = []
                for row in cur.fetchall():
                    by_model.append({
                        "model_name": row[0],
                        "input_tokens": row[1] or 0,
                        "output_tokens": row[2] or 0,
                        "total_tokens": row[3] or 0,
                        "request_count": row[4] or 0,
                        "avg_duration_ms": round(row[5] or 0, 2)
                    })
                
                cur.execute(
                    f"""SELECT 
                           COALESCE(SUM(input_tokens), 0) as total_input,
                           COALESCE(SUM(output_tokens), 0) as total_output,
                           COALESCE(SUM(input_tokens + output_tokens), 0) as total_tokens,
                           COUNT(*) as request_count
                       FROM token_usage_log 
                       WHERE {where_sql}""",
                    params
                )
                row = cur.fetchone()
                total = {
                    "input_tokens": row[0] if row else 0,
                    "output_tokens": row[1] if row else 0,
                    "total_tokens": row[2] if row else 0,
                    "request_count": row[3] if row else 0
                }
            
            conn.close()
            
            return {
                "total": total,
                "by_model": by_model
            }
        except Exception as e:
            logger.error(f"Failed to get token stats: {e}")
            return {"total": {}, "by_model": []}
    
    def get_model_usage_today(self, model_name: str) -> Dict:
        today_str = datetime.now().strftime("%Y-%m-%d")
        try:
            from ..config import get_db_connection
            conn = get_db_connection()
            
            with conn.cursor() as cur:
                cur.execute(
                    """SELECT 
                           COALESCE(SUM(input_tokens + output_tokens), 0) as total_tokens,
                           COUNT(*) as request_count
                       FROM token_usage_log 
                       WHERE model_name = %s AND DATE(created_at) = %s""",
                    (model_name, today_str)
                )
                row = cur.fetchone()
            
            conn.close()
            
            return {
                "model_name": model_name,
                "date": today_str,
                "total_tokens": row[0] if row else 0,
                "request_count": row[1] if row else 0
            }
        except Exception as e:
            logger.error(f"Failed to get model usage: {e}")
            return {"model_name": model_name, "date": today_str, "total_tokens": 0, "request_count": 0}


model_gateway = ModelGateway()
