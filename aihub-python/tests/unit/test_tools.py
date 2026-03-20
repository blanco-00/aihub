import pytest
from unittest.mock import MagicMock, patch


class TestToolRegistry:
    def test_register_tool(self):
        with patch('aihub.tools.registry.registry') as registry:
            from aihub.tools.registry import ToolRegistry
            
            test_func = MagicMock(return_value="result")
            reg = ToolRegistry()
            reg.register("test_tool", test_func, "Test tool description")
            
            assert "test_tool" in reg._tools
            assert reg._tools["test_tool"]["description"] == "Test tool description"

    def test_get_tool(self):
        with patch('aihub.tools.registry.registry') as registry:
            from aihub.tools.registry import ToolRegistry
            
            test_func = MagicMock(return_value="result")
            reg = ToolRegistry()
            reg.register("test_tool", test_func, "Test tool")
            
            retrieved_func = reg.get_tool("test_tool")
            assert retrieved_func == test_func

    def test_get_tool_not_found(self):
        from aihub.tools.registry import ToolRegistry
        
        reg = ToolRegistry()
        with pytest.raises(ValueError, match="Tool 'nonexistent' not found"):
            reg.get_tool("nonexistent")

    def test_list_tools(self):
        from aihub.tools.registry import ToolRegistry
        
        reg = ToolRegistry()
        reg.register("tool1", MagicMock(), "Description 1")
        reg.register("tool2", MagicMock(), "Description 2")
        
        tools = reg.list_tools()
        assert len(tools) == 2
        assert "tool1" in tools
        assert "tool2" in tools

    def test_builtin_tools(self):
        from aihub.tools.registry import registry
        
        tools = registry.list_tools()
        assert "search_web" in tools
        assert "calculator" in tools
