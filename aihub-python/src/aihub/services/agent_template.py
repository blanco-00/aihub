from dataclasses import dataclass
from typing import Optional, List, Dict, Any
import pymysql
import json
import logging

logger = logging.getLogger(__name__)


@dataclass
class AgentTemplate:
    id: int
    name: str
    description: Optional[str]
    system_message: str
    model_name: str
    tools: List[str]
    config: Dict[str, Any]
    is_default: bool
    status: int
    created_by: Optional[int]


class AgentTemplateService:
    def __init__(self):
        self._cache: Dict[int, AgentTemplate] = {}
    
    def _get_connection(self):
        from ..config import get_db_connection
        return get_db_connection()
    
    def _row_to_template(self, row) -> AgentTemplate:
        tools = []
        config = {}
        if row[5]:
            try:
                tools = json.loads(row[5])
            except:
                pass
        if row[6]:
            try:
                config = json.loads(row[6])
            except:
                pass
        
        return AgentTemplate(
            id=row[0],
            name=row[1],
            description=row[2],
            system_message=row[3] or "",
            model_name=row[4] or "glm-4.7",
            tools=tools,
            config=config,
            is_default=bool(row[7]),
            status=row[8] or 1,
            created_by=row[9]
        )
    
    def get_template(self, template_id: int) -> Optional[AgentTemplate]:
        if template_id in self._cache:
            return self._cache[template_id]
        
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                cur.execute(
                    """SELECT id, name, description, system_message, model_name, 
                              tools, config, is_default, status, created_by
                       FROM agent_template 
                       WHERE id = %s AND is_deleted = 0 AND status = 1""",
                    (template_id,)
                )
                row = cur.fetchone()
            conn.close()
            
            if row:
                template = self._row_to_template(row)
                self._cache[template_id] = template
                return template
            return None
        except Exception as e:
            logger.error(f"Failed to get template: {e}")
            return None
    
    def get_template_by_name(self, name: str) -> Optional[AgentTemplate]:
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                cur.execute(
                    """SELECT id, name, description, system_message, model_name, 
                              tools, config, is_default, status, created_by
                       FROM agent_template 
                       WHERE name = %s AND is_deleted = 0 AND status = 1""",
                    (name,)
                )
                row = cur.fetchone()
            conn.close()
            
            if row:
                return self._row_to_template(row)
            return None
        except Exception as e:
            logger.error(f"Failed to get template by name: {e}")
            return None
    
    def get_default_template(self) -> Optional[AgentTemplate]:
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                cur.execute(
                    """SELECT id, name, description, system_message, model_name, 
                              tools, config, is_default, status, created_by
                       FROM agent_template 
                       WHERE is_default = 1 AND is_deleted = 0 AND status = 1
                       LIMIT 1"""
                )
                row = cur.fetchone()
            conn.close()
            
            if row:
                return self._row_to_template(row)
            return None
        except Exception as e:
            logger.error(f"Failed to get default template: {e}")
            return None
    
    def list_templates(self, user_id: Optional[int] = None) -> List[AgentTemplate]:
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                if user_id:
                    cur.execute(
                        """SELECT id, name, description, system_message, model_name, 
                                  tools, config, is_default, status, created_by
                           FROM agent_template 
                           WHERE is_deleted = 0 AND status = 1
                           ORDER BY is_default DESC, id ASC"""
                    )
                else:
                    cur.execute(
                        """SELECT id, name, description, system_message, model_name, 
                                  tools, config, is_default, status, created_by
                           FROM agent_template 
                           WHERE is_deleted = 0 AND status = 1
                           ORDER BY is_default DESC, id ASC"""
                    )
                rows = cur.fetchall()
            conn.close()
            
            return [self._row_to_template(row) for row in rows]
        except Exception as e:
            logger.error(f"Failed to list templates: {e}")
            return []
    
    def create_template(
        self,
        name: str,
        system_message: str,
        description: Optional[str] = None,
        model_name: str = "glm-4.7",
        tools: Optional[List[str]] = None,
        config: Optional[Dict[str, Any]] = None,
        created_by: Optional[int] = None
    ) -> Optional[AgentTemplate]:
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                cur.execute(
                    """INSERT INTO agent_template 
                       (name, description, system_message, model_name, tools, config, created_by)
                       VALUES (%s, %s, %s, %s, %s, %s, %s)""",
                    (
                        name,
                        description,
                        system_message,
                        model_name,
                        json.dumps(tools or [], ensure_ascii=False),
                        json.dumps(config or {}, ensure_ascii=False),
                        created_by
                    )
                )
                template_id = cur.lastrowid
            conn.close()
            
            return self.get_template(template_id)
        except Exception as e:
            logger.error(f"Failed to create template: {e}")
            return None
    
    def update_template(
        self,
        template_id: int,
        name: Optional[str] = None,
        system_message: Optional[str] = None,
        description: Optional[str] = None,
        model_name: Optional[str] = None,
        tools: Optional[List[str]] = None,
        config: Optional[Dict[str, Any]] = None
    ) -> Optional[AgentTemplate]:
        try:
            updates = []
            params = []
            
            if name is not None:
                updates.append("name = %s")
                params.append(name)
            if description is not None:
                updates.append("description = %s")
                params.append(description)
            if system_message is not None:
                updates.append("system_message = %s")
                params.append(system_message)
            if model_name is not None:
                updates.append("model_name = %s")
                params.append(model_name)
            if tools is not None:
                updates.append("tools = %s")
                params.append(json.dumps(tools, ensure_ascii=False))
            if config is not None:
                updates.append("config = %s")
                params.append(json.dumps(config, ensure_ascii=False))
            
            if not updates:
                return self.get_template(template_id)
            
            params.append(template_id)
            
            conn = self._get_connection()
            with conn.cursor() as cur:
                cur.execute(
                    f"""UPDATE agent_template SET {', '.join(updates)} WHERE id = %s""",
                    params
                )
            conn.close()
            
            self._cache.pop(template_id, None)
            return self.get_template(template_id)
        except Exception as e:
            logger.error(f"Failed to update template: {e}")
            return None
    
    def delete_template(self, template_id: int) -> bool:
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                cur.execute(
                    "UPDATE agent_template SET is_deleted = 1 WHERE id = %s",
                    (template_id,)
                )
            conn.close()
            
            self._cache.pop(template_id, None)
            return True
        except Exception as e:
            logger.error(f"Failed to delete template: {e}")
            return False


agent_template_service = AgentTemplateService()
