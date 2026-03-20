from typing import Dict, Any, Callable, List
from pydantic import BaseModel


class Skill(BaseModel):
    name: str
    description: str
    category: str
    handler: Callable


class SkillRegistry:
    def __init__(self):
        self._skills: Dict[str, Skill] = {}

    def register(self, skill: Skill):
        self._skills[skill.name] = skill

    def get(self, name: str) -> Skill:
        if name not in self._skills:
            raise ValueError(f"Skill '{name}' not found")
        return self._skills[name]

    def list_by_category(self, category: str) -> List[Skill]:
        return [s for s in self._skills.values() if s.category == category]

    def list_all(self) -> List[Dict[str, str]]:
        return [
            {"name": s.name, "description": s.description, "category": s.category}
            for s in self._skills.values()
        ]


skill_registry = SkillRegistry()


def code_review_skill(args: Dict[str, Any]) -> Dict[str, Any]:
    code = args.get("code", "")
    issues = []
    if "eval(" in code:
        issues.append({"severity": "high", "message": "Avoid using eval()"})
    if len(code) > 500:
        issues.append({"severity": "medium", "message": "Consider splitting long function"})
    return {"issues": issues, "score": max(0, 100 - len(issues) * 20)}


def data_analysis_skill(args: Dict[str, Any]) -> Dict[str, Any]:
    data = args.get("data", "")
    return {
        "summary": f"Analyzed {len(data)} records",
        "insights": ["Trend detected", "Outliers found"]
    }


skill_registry.register(Skill(
    name="code_review",
    description="Analyze code for issues and best practices",
    category="development",
    handler=code_review_skill
))

skill_registry.register(Skill(
    name="data_analysis",
    description="Analyze data and generate insights",
    category="analytics",
    handler=data_analysis_skill
))
