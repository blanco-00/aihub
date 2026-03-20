from langchain.tools import tool
from typing import Dict, Callable


class ToolRegistry:
    def __init__(self):
        self._tools: Dict[str, Callable] = {}

    def register(self, name: str, func: Callable, description: str = ""):
        self._tools[name] = {"func": func, "description": description}

    def get_tool(self, name: str) -> Callable:
        if name not in self._tools:
            raise ValueError(f"Tool '{name}' not found")
        return self._tools[name]["func"]

    def list_tools(self) -> Dict[str, str]:
        return {name: info["description"] for name, info in self._tools.items()}

    def get_all_funcs(self):
        return [info["func"] for info in self._tools.values()]


registry = ToolRegistry()


@tool
def search_web(query: str) -> str:
    """Search the web for information."""
    return f"Search results for: {query}"


@tool
def calculator(expression: str) -> str:
    """Calculate a mathematical expression."""
    try:
        result = eval(expression)
        return str(result)
    except Exception as e:
        return f"Error: {str(e)}"


registry.register("search_web", search_web, "Search the web for information")
registry.register("calculator", calculator, "Calculate a mathematical expression")
