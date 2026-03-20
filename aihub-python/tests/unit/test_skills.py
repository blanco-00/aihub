import pytest
from unittest.mock import MagicMock, patch


class TestSkillsRegistry:
    def test_register_skill(self):
        from aihub.skills.registry import SkillRegistry, Skill
        
        registry = SkillRegistry()
        
        def handler(args):
            return {"result": "done"}
        
        skill = Skill(
            name="test_skill",
            description="Test skill",
            category="test",
            handler=handler
        )
        registry.register(skill)
        
        assert "test_skill" in registry._skills

    def test_get_skill(self):
        from aihub.skills.registry import SkillRegistry, Skill
        
        registry = SkillRegistry()
        
        def handler(args):
            return {"result": "done"}
        
        skill = Skill(
            name="my_skill",
            description="My skill",
            category="test",
            handler=handler
        )
        registry.register(skill)
        
        retrieved = registry.get("my_skill")
        assert retrieved.name == "my_skill"

    def test_get_skill_not_found(self):
        from aihub.skills.registry import SkillRegistry
        
        registry = SkillRegistry()
        with pytest.raises(ValueError, match="Skill 'nonexistent' not found"):
            registry.get("nonexistent")

    def test_list_by_category(self):
        from aihub.skills.registry import SkillRegistry, Skill
        
        registry = SkillRegistry()
        
        registry.register(Skill(
            name="skill1", description="", category="dev", handler=MagicMock()
        ))
        registry.register(Skill(
            name="skill2", description="", category="analytics", handler=MagicMock()
        ))
        
        dev_skills = registry.list_by_category("dev")
        assert len(dev_skills) == 1

    def test_builtin_skills(self):
        from aihub.skills.registry import skill_registry
        
        skills = skill_registry.list_all()
        skill_names = [s["name"] for s in skills]
        
        assert "code_review" in skill_names
        assert "data_analysis" in skill_names

    def test_code_review_skill(self):
        from aihub.skills.registry import skill_registry
        
        result = skill_registry.get("code_review").handler({"code": "eval(1+1)"})
        assert len(result["issues"]) > 0
        assert result["issues"][0]["severity"] == "high"
