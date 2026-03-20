import pytest
from unittest.mock import MagicMock, patch


class TestMCPServer:
    def test_register_tool(self):
        from aihub.mcp.server import MCPServer
        
        server = MCPServer()
        handler = MagicMock(return_value="result")
        
        server.register_tool(
            name="test_tool",
            description="Test tool",
            input_schema={"type": "object"},
            handler=handler
        )
        
        assert "test_tool" in server._tools
        assert server._tools["test_tool"]["description"] == "Test tool"

    def test_list_tools(self):
        from aihub.mcp.server import MCPServer
        
        server = MCPServer()
        server.register_tool(
            name="tool1",
            description="Tool 1",
            input_schema={"type": "object"},
            handler=MagicMock()
        )
        
        tools = server.list_tools()
        assert len(tools) == 1
        assert tools[0]["name"] == "tool1"

    def test_execute_tool(self):
        from aihub.mcp.server import MCPServer
        
        server = MCPServer()
        handler = MagicMock(return_value="executed")
        server.register_tool(
            name="exec_tool",
            description="Exec tool",
            input_schema={"type": "object"},
            handler=handler
        )
        
        result = server.execute_tool("exec_tool", {"arg": "value"})
        assert result == "executed"
        handler.assert_called_once_with({"arg": "value"})

    def test_execute_tool_not_found(self):
        from aihub.mcp.server import MCPServer
        
        server = MCPServer()
        with pytest.raises(ValueError, match="Tool 'nonexistent' not found"):
            server.execute_tool("nonexistent", {})

    def test_builtin_tools(self):
        from aihub.mcp.server import mcp_server
        
        tools = mcp_server.list_tools()
        tool_names = [t["name"] for t in tools]
        
        assert "order_query" in tool_names
        assert "user_info" in tool_names

    def test_order_query_handler(self):
        from aihub.mcp.server import mcp_server
        
        result = mcp_server.execute_tool("order_query", {"orderId": "12345"})
        assert "12345" in result
        assert "Status=Shipped" in result
