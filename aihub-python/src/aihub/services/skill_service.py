import json
import logging
from typing import List, Dict, Any, Optional
from dataclasses import dataclass
import pymysql

logger = logging.getLogger(__name__)


@dataclass
class Skill:
    id: int
    name: str
    description: Optional[str]
    trigger_keywords: List[str]
    system_message: str
    tools: List[str]
    mcp_tools: List[str]
    config: Dict[str, Any]
    is_builtin: bool
    status: int
    created_by: Optional[int]


class SkillService:
    def __init__(self):
        self._skills: Dict[int, Skill] = {}
        self._skills_by_name: Dict[str, Skill] = {}
    
    def _get_connection(self):
        from ..config import get_db_connection
        return get_db_connection()
    
    def _row_to_skill(self, row) -> Skill:
        trigger_keywords = []
        tools = []
        mcp_tools = []
        config = {}
        
        if row[3]:
            try:
                trigger_keywords = json.loads(row[3])
            except:
                pass
        if row[5]:
            try:
                tools = json.loads(row[5])
            except:
                pass
        if row[6]:
            try:
                mcp_tools = json.loads(row[6])
            except:
                pass
        if row[7]:
            try:
                config = json.loads(row[7])
            except:
                pass
        
        return Skill(
            id=row[0],
            name=row[1],
            description=row[2],
            trigger_keywords=trigger_keywords,
            system_message=row[4] or "",
            tools=tools,
            mcp_tools=mcp_tools,
            config=config,
            is_builtin=bool(row[8]),
            status=row[9] or 1,
            created_by=row[10]
        )
    
    def get_skill(self, skill_id: int) -> Optional[Skill]:
        if skill_id in self._skills:
            return self._skills[skill_id]
        
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                cur.execute(
                    """SELECT id, name, description, trigger_keywords, system_message, 
                              tools, mcp_tools, config, is_builtin, status, created_by
                       FROM skill WHERE id = %s AND is_deleted = 0 AND status = 1""",
                    (skill_id,)
                )
                row = cur.fetchone()
            conn.close()
            
            if row:
                skill = self._row_to_skill(row)
                self._skills[skill_id] = skill
                self._skills_by_name[skill.name] = skill
                return skill
            return None
        except Exception as e:
            logger.error(f"Failed to get skill: {e}")
            return None
    
    def get_skill_by_name(self, name: str) -> Optional[Skill]:
        if name in self._skills_by_name:
            return self._skills_by_name[name]
        
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                cur.execute(
                    """SELECT id, name, description, trigger_keywords, system_message, 
                              tools, mcp_tools, config, is_builtin, status, created_by
                       FROM skill WHERE name = %s AND is_deleted = 0 AND status = 1""",
                    (name,)
                )
                row = cur.fetchone()
            conn.close()
            
            if row:
                skill = self._row_to_skill(row)
                self._skills[skill.id] = skill
                self._skills_by_name[skill.name] = skill
                return skill
            return None
        except Exception as e:
            logger.error(f"Failed to get skill by name: {e}")
            return None
    
    def detect_skill(self, message: str) -> Optional[Skill]:
        message_lower = message.lower()
        
        skills = self.list_skills()
        for skill in skills:
            for keyword in skill.trigger_keywords:
                if keyword.lower() in message_lower:
                    return skill
        
        return None
    
    def list_skills(self, include_disabled: bool = False) -> List[Skill]:
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                if include_disabled:
                    cur.execute(
                        """SELECT id, name, description, trigger_keywords, system_message, 
                                  tools, mcp_tools, config, is_builtin, status, created_by
                           FROM skill WHERE is_deleted = 0
                           ORDER BY is_builtin DESC, id ASC"""
                    )
                else:
                    cur.execute(
                        """SELECT id, name, description, trigger_keywords, system_message, 
                                  tools, mcp_tools, config, is_builtin, status, created_by
                           FROM skill WHERE is_deleted = 0 AND status = 1
                           ORDER BY is_builtin DESC, id ASC"""
                    )
                rows = cur.fetchall()
            conn.close()
            
            return [self._row_to_skill(row) for row in rows]
        except Exception as e:
            logger.error(f"Failed to list skills: {e}")
            return []
    
    def create_skill(
        self,
        name: str,
        description: Optional[str] = None,
        trigger_keywords: Optional[List[str]] = None,
        system_message: str = "你是一个有帮助的AI助手。",
        tools: Optional[List[str]] = None,
        mcp_tools: Optional[List[str]] = None,
        config: Optional[Dict[str, Any]] = None,
        created_by: Optional[int] = None
    ) -> Optional[Skill]:
        try:
            conn = self._get_connection()
            with conn.cursor() as cur:
                cur.execute(
                    """INSERT INTO skill 
                       (name, description, trigger_keywords, system_message, tools, mcp_tools, config, created_by)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s)""",
                    (
                        name,
                        description,
                        json.dumps(trigger_keywords or [], ensure_ascii=False),
                        system_message,
                        json.dumps(tools or [], ensure_ascii=False),
                        json.dumps(mcp_tools or [], ensure_ascii=False),
                        json.dumps(config or {}, ensure_ascii=False),
                        created_by
                    )
                )
                skill_id = cur.lastrowid
            conn.close()
            
            return self.get_skill(skill_id)
        except Exception as e:
            logger.error(f"Failed to create skill: {e}")
            return None
    
    def update_skill(
        self,
        skill_id: int,
        name: Optional[str] = None,
        description: Optional[str] = None,
        trigger_keywords: Optional[List[str]] = None,
        system_message: Optional[str] = None,
        tools: Optional[List[str]] = None,
        mcp_tools: Optional[List[str]] = None,
        config: Optional[Dict[str, Any]] = None
    ) -> Optional[Skill]:
        try:
            updates = []
            params = []
            
            if name is not None:
                updates.append("name = %s")
                params.append(name)
            if description is not None:
                updates.append("description = %s")
                params.append(description)
            if trigger_keywords is not None:
                updates.append("trigger_keywords = %s")
                params.append(json.dumps(trigger_keywords, ensure_ascii=False))
            if system_message is not None:
                updates.append("system_message = %s")
                params.append(system_message)
            if tools is not None:
                updates.append("tools = %s")
                params.append(json.dumps(tools, ensure_ascii=False))
            if mcp_tools is not None:
                updates.append("mcp_tools = %s")
                params.append(json.dumps(mcp_tools, ensure_ascii=False))
            if config is not None:
                updates.append("config = %s")
                params.append(json.dumps(config, ensure_ascii=False))
            
            if not updates:
                return self.get_skill(skill_id)
            
            params.append(skill_id)
            
            conn = self._get_connection()
            with conn.cursor() as cur:
                cur.execute(
                    f"""UPDATE skill SET {', '.join(updates)} WHERE id = %s""",
                    params
                )
            conn.close()
            
            if skill_id in self._skills:
                del self._skills[skill_id]
            if name and name in self._skills_by_name:
                del self._skills_by_name[name]
            return self.get_skill(skill_id)
        except Exception as e:
            logger.error(f"Failed to update skill: {e}")
            return None
    
    def delete_skill(self, skill_id: int) -> bool:
        try:
            skill = self.get_skill(skill_id)
            
            conn = self._get_connection()
            with conn.cursor() as cur:
                cur.execute(
                    "UPDATE skill SET is_deleted = 1 WHERE id = %s",
                    (skill_id,)
                )
            conn.close()
            
            if skill:
                if skill.id in self._skills:
                    del self._skills[skill.id]
                if skill.name and skill.name in self._skills_by_name:
                    del self._skills_by_name[skill.name]
            return True
        except Exception as e:
            logger.error(f"Failed to delete skill: {e}")
            return False


skill_service = SkillService()
