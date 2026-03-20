from typing import Dict, Any, Callable
import json


class MCPServer:
    def __init__(self):
        self._tools: Dict[str, Dict[str, Any]] = {}

    def register_tool(
        self,
        name: str,
        description: str,
        input_schema: Dict[str, Any],
        handler: Callable
    ):
        self._tools[name] = {
            "description": description,
            "inputSchema": input_schema,
            "handler": handler
        }

    def list_tools(self) -> list:
        return [
            {
                "name": name,
                "description": info["description"],
                "inputSchema": info["inputSchema"]
            }
            for name, info in self._tools.items()
        ]

    def execute_tool(self, name: str, arguments: Dict[str, Any]) -> Any:
        if name not in self._tools:
            raise ValueError(f"Tool '{name}' not found")
        handler = self._tools[name]["handler"]
        return handler(arguments)

    def get_tool(self, name: str) -> Dict[str, Any]:
        if name not in self._tools:
            raise ValueError(f"Tool '{name}' not found")
        return self._tools[name]


mcp_server = MCPServer()


def handle_order_query(args: Dict[str, Any]) -> str:
    order_id = args.get("orderId", "")
    return f"Order {order_id}: Status=Shipped, Expected delivery=2024-01-15"


def handle_user_info(args: Dict[str, Any]) -> str:
    user_id = args.get("userId", "")
    return f"User {user_id}: Name=John Doe, Email=john@example.com"


mcp_server.register_tool(
    name="order_query",
    description="Query order status by order ID",
    input_schema={
        "type": "object",
        "properties": {
            "orderId": {"type": "string", "description": "Order ID"}
        },
        "required": ["orderId"]
    },
    handler=handle_order_query
)

mcp_server.register_tool(
    name="user_info",
    description="Get user information by user ID",
    input_schema={
        "type": "object",
        "properties": {
            "userId": {"type": "string", "description": "User ID"}
        },
        "required": ["userId"]
    },
    handler=handle_user_info
)
