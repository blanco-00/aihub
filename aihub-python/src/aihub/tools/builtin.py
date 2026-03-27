"""
内置工具集合

这个模块提供 AI Agent 可调用的内置工具。
每个工具都是一个函数，接受参数并返回结果。

工具设计原则：
1. 纯函数：相同输入总是产生相同输出，没有副作用
2. 安全性：防止恶意使用，如命令注入、文件遍历
3. 容错性：所有异常都要捕获，返回有意义的错误信息
4. 可观测性：记录关键操作的日志

学习要点：
1. AST (抽象语法树) 解析 - 实现安全的数学计算器
2. HTTP 客户端使用 - 发送网络请求
3. 文件操作安全 - 沙箱机制防止路径遍历
4. 函数式编程 - 高阶函数、闭包
"""

# ============================================================
# 导入标准库和第三方库
# ============================================================

from typing import Dict, Any, TYPE_CHECKING
# typing.Dict: 类型注解，表示字典类型
# typing.Any: 任意类型
# TYPE_CHECKING: 仅在类型检查时导入，避免运行时循环依赖

import ast
# ast (Abstract Syntax Tree) 模块：将 Python 代码解析成语法树
# 用途：安全地执行数学表达式，而不执行任意代码
# 
# 为什么不用 eval()？
# eval("__import__('os').system('rm -rf /')")  # 危险！会执行系统命令
# ast.parse() 只解析语法，不执行代码，更安全

import operator
# operator 模块：提供内置运算符的函数形式
# 如 operator.add(a, b) 等同于 a + b
# 用于 AST 解释器中执行安全的数学运算

import os
# os 模块：操作系统相关功能
# 这里用于获取环境变量和路径操作

import logging
# logging 模块：记录日志

from pathlib import Path
# pathlib.Path：面向对象的路径操作
# 比 os.path 更直观，如 Path("/tmp") / "file.txt"

import json
# json 模块：JSON 序列化


# ============================================================
# 类型检查时的导入（避免循环依赖）
# ============================================================

"""
TYPE_CHECKING 技巧

if TYPE_CHECKING:
    from ..mcp.server import MCPServer

这样做的好处：
1. 类型检查器（如 mypy）可以看到这个导入
2. 运行时不会真正导入，避免循环依赖
3. 类型注解不会影响运行时性能

为什么需要 MCPServer 类型？
- 工具函数签名中有 server: "MCPServer" 参数
- 这让 IDE 知道 server 参数有哪些方法可用
- 实际运行时，传入的是 MCPServer 的实例
"""

if TYPE_CHECKING:
    from ..mcp.server import MCPServer


# ============================================================
# 配置日志
# ============================================================

logger = logging.getLogger(__name__)


# ============================================================
# 安全的数学运算操作符映射
# ============================================================

"""
运算符映射表

为什么需要这个映射？
- AST 解析后，操作符是 AST 节点对象
- 需要将它们转换成实际的操作函数

ast 模块中的节点类型：
- ast.Add: 加法 (+)
- ast.Sub: 减法 (-)
- ast.Mult: 乘法 (*)
- ast.Div: 除法 (/)
- ast.FloorDiv: 整除 (//)
- ast.Mod: 取模 (%)
- ast.Pow: 幂运算 (**)
- ast.USub: 一元负 (-x)
- ast.UAdd: 一元正 (+x)
"""

SAFE_OPERATORS = {
    ast.Add: operator.add,      # +
    ast.Sub: operator.sub,      # -
    ast.Mult: operator.mul,      # *
    ast.Div: operator.truediv,  # / (true division)
    ast.FloorDiv: operator.floordiv,  # // (floor division)
    ast.Mod: operator.mod,        # %
    ast.Pow: operator.pow,       # **
    ast.USub: operator.neg,      # -x (unary negation)
    ast.UAdd: operator.pos,      # +x (unary positive)
}


# ============================================================
# 安全数学函数映射
# ============================================================

"""
允许的数学函数

为什么限制函数？
- 不是所有 Python 内置函数都是安全的
- eval("__import__('os').system('ls')") 会执行系统命令
- 只允许特定的数学函数

允许的函数：
- abs: 绝对值
- round: 四舍五入
- min/max: 最小/最大值
- sum: 求和
- pow: 幂运算
"""

SAFE_FUNCTIONS = {
    'abs': abs,      # 绝对值
    'round': round,  # 四舍五入
    'min': min,      # 最小值
    'max': max,      # 最大值
    'sum': sum,      # 求和
    'pow': pow,      # 幂运算
}


# ============================================================
# Web 搜索工具
# ============================================================

def web_search_tool(args: Dict[str, Any], server: "MCPServer") -> str:
    """
    使用 DuckDuckGo API 进行网页搜索
    
    为什么选择 DuckDuckGo？
    - 不需要 API Key，适合免费使用
    - 有公开的即时答案 API
    - 返回结构化的 JSON 数据
    
    DuckDuckGo Instant Answer API：
    - URL: https://api.duckduckgo.com/
    - 参数：
        - q: 搜索关键词
        - format: 返回格式 (json)
        - no_html: 跳过 HTML 标签
        - skip_disambig: 跳过消歧义页面
    
    参数:
        args: 包含 'query' 键的字典
        server: MCPServer 实例，用于获取 HTTP 客户端
    
    返回:
        str: 格式化的搜索结果，或错误信息
    """
    # 安全地获取参数，提供默认值避免 KeyError
    query = args.get("query", "")
    
    # 参数验证
    if not query:
        return "Error: 'query' parameter is required"
    
    try:
        # 获取 HTTP 客户端（从连接池复用）
        client = server.get_http_client()
        
        # DuckDuckGo Instant Answer API 端点
        url = "https://api.duckduckgo.com/"
        
        # 请求参数
        params = {
            "q": query,
            "format": "json",
            "no_html": 1,  # 不返回 HTML
            "skip_disambig": 1  # 跳过消歧义
        }
        
        # 发送 GET 请求
        # client.get() 返回 Response 对象
        response = client.get(url, params=params)
        
        # 检查 HTTP 状态码，非 2xx 会抛出异常
        response.raise_for_status()
        
        # 解析 JSON 响应
        data = response.json()
        
        results = []  # 用于存储结果
        
        # 1. 提取摘要信息（通常是最相关的）
        if data.get("Abstract"):
            results.append(f"Summary: {data['Abstract']}")
            if data.get("AbstractURL"):
                results.append(f"Source: {data['AbstractURL']}")
        
        # 2. 提取相关主题
        if data.get("RelatedTopics"):
            topics = []
            for topic in data.get("RelatedTopics", [])[:5]:  # 只取前5个
                if isinstance(topic, dict) and topic.get("Text"):
                    topics.append(f"- {topic['Text']}")
            if topics:
                results.append(f"\nRelated:\n" + "\n".join(topics))
        
        # 3. 提取即时答案（如数学计算、定义等）
        if data.get("Answer"):
            results.append(f"Answer: {data['Answer']}")
        
        # 如果没有结果
        if not results:
            return f"No results found for: {query}"
        
        # 用换行连接所有结果
        return "\n".join(results)
        
    except Exception as e:
        # 捕获所有异常，返回友好的错误信息
        # 不要让内部错误泄露给用户
        logger.error(f"Web search failed: {e}")
        return f"Search error: {str(e)}"


# ============================================================
# 计算器工具（AST 安全实现）
# ============================================================

def calculator_tool(args: Dict[str, Any], server: "MCPServer") -> str:
    """
    安全数学表达式计算器
    
    实现原理：AST 解析
    1. 将表达式字符串解析成语法树
    2. 遍历语法树，只执行允许的操作
    3. 拒绝任何不允许的操作（如函数调用、变量引用）
    
    为什么不用 eval()？
    eval("__import__('os').system('rm -rf /')")  # 危险！
    eval("open('/etc/passwd').read()")  # 危险！
    
    AST 方法只解析不执行，无法调用危险函数
    
    参数:
        args: 包含 'expression' 键的字典
        server: MCPServer 实例（此工具不需要）
    
    返回:
        str: 计算结果或错误信息
    """
    expression = args.get("expression", "")
    
    if not expression:
        return "Error: 'expression' parameter is required"
    
    try:
        # 步骤1：解析表达式为 AST
        # mode='eval' 表示解析的是表达式，不是语句
        # 如果语法错误，会抛出 SyntaxError
        tree = ast.parse(expression, mode='eval')
        
        # 步骤2：递归计算 AST
        result = _eval_node(tree.body)
        return str(result)
        
    except SyntaxError as e:
        return f"Syntax error: {str(e)}"
    except ValueError as e:
        # 自定义的验证错误（如使用了不允许的操作）
        return f"Error: {str(e)}"
    except Exception as e:
        return f"Calculation error: {str(e)}"


def _eval_node(node) -> Any:
    """
    递归计算 AST 节点
    
    这是一个"安全解释器"的核心
    遍历语法树，只执行我们允许的操作
    
    参数:
        node: AST 节点对象
    
    返回:
        Any: 计算结果（数字）
    
    学习要点：
    - isinstance() 检查对象类型
    - 递归函数：函数调用自身
    - 模式匹配：通过 if-elif 处理不同节点类型
    """
    # 数字字面量 (Python 3.7 兼容)
    if isinstance(node, ast.Num):
        return node.n
    
    # 常量 (Python 3.8+)：更通用的数字处理
    # Python 3.8 后，推荐使用 ast.Constant 代替 ast.Num
    elif isinstance(node, ast.Constant):
        # 只处理数值类型（int/float）
        if isinstance(node.value, (int, float)):
            return node.value
        raise ValueError(f"Unsupported constant type: {type(node.value)}")
    
    # 二元运算：a + b, a - b, a * b 等
    elif isinstance(node, ast.BinOp):
        # 递归计算左右操作数
        left = _eval_node(node.left)
        right = _eval_node(node.right)
        
        # 获取运算符类型
        op_type = type(node.op)
        
        # 从映射表查找对应的操作函数
        if op_type in SAFE_OPERATORS:
            # 执行运算，如 operator.add(left, right)
            return SAFE_OPERATORS[op_type](left, right)
        
        raise ValueError(f"Unsupported operator: {op_type.__name__}")
    
    # 一元运算：-a, +a
    elif isinstance(node, ast.UnaryOp):
        operand = _eval_node(node.operand)
        op_type = type(node.op)
        
        if op_type in SAFE_OPERATORS:
            return SAFE_OPERATORS[op_type](operand)
        
        raise ValueError(f"Unsupported unary operator: {op_type.__name__}")
    
    # 函数调用：abs(x), round(x), min(x, y) 等
    elif isinstance(node, ast.Call):
        # 只处理直接函数调用，如 abs(x)
        # 不支持方法调用，如 x.abs() 或 len(x.list)
        if isinstance(node.func, ast.Name):
            func_name = node.func.id
            
            # 检查是否在白名单中
            if func_name in SAFE_FUNCTIONS:
                # 递归计算所有参数
                args = [_eval_node(arg) for arg in node.args]
                # 调用函数，如 SAFE_FUNCTIONS['abs'](5)
                return SAFE_FUNCTIONS[func_name](*args)
        
        raise ValueError(f"Unsupported function call")
    
    # 变量引用：不允许！
    # 这防止了 eval("x") 类型的攻击
    elif isinstance(node, ast.Name):
        raise ValueError(f"Variables are not allowed: {node.id}")
    
    # 其他节点类型：暂不支持
    else:
        raise ValueError(f"Unsupported expression type: {type(node).__name__}")


# ============================================================
# HTTP 请求工具
# ============================================================

def http_request_tool(args: Dict[str, Any], server: "MCPServer") -> str:
    """
    通用 HTTP 请求工具
    
    支持：
    - GET 请求
    - POST 请求（JSON body 或 raw body）
    - 自定义请求头
    - 可配置超时
    
    安全考虑：
    - 只允许 http:// 和 https://
    - 限制响应体大小（最多 5000 字符）
    - 超时保护
    
    参数:
        args: 包含以下键的字典:
            - url: 请求 URL (必填)
            - method: HTTP 方法，默认 GET
            - headers: 请求头字典
            - body: 请求体 (POST 用)
            - timeout: 超时秒数
        server: MCPServer 实例，用于获取 HTTP 客户端
    
    返回:
        str: 格式化的响应信息，或错误信息
    """
    url = args.get("url", "")
    
    # URL 必填
    if not url:
        return "Error: 'url' parameter is required"
    
    # HTTP 方法验证
    method = args.get("method", "GET").upper()
    if method not in ("GET", "POST"):
        return f"Error: Unsupported method '{method}'. Use GET or POST."
    
    # 可选参数
    headers = args.get("headers", {})
    body = args.get("body")
    timeout = args.get("timeout", 30)  # 默认 30 秒
    
    # URL 安全验证：只允许 http/https
    if not url.startswith(("http://", "https://")):
        return "Error: URL must start with http:// or https://"
    
    try:
        # 获取 HTTP 客户端
        client = server.get_http_client()
        
        if method == "GET":
            # GET 请求
            response = client.get(url, headers=headers, timeout=timeout)
        else:
            # POST 请求
            # 如果 body 是字典，当作 JSON 发送
            if isinstance(body, dict):
                response = client.post(url, json=body, headers=headers, timeout=timeout)
            else:
                # 否则当作原始 body 发送
                response = client.post(url, content=body, headers=headers, timeout=timeout)
        
        # 构造响应信息
        result = {
            "status_code": response.status_code,
            "headers": dict(response.headers),
            # 限制响应体大小，防止内存溢出
            "body": response.text[:5000]
        }
        
        # 序列化为格式化的 JSON 字符串
        return json.dumps(result, indent=2, ensure_ascii=False)
        
    except Exception as e:
        logger.error(f"HTTP request failed: {e}")
        return f"Request error: {str(e)}"


# ============================================================
# 文件写入工具（沙箱保护）
# ============================================================

def file_write_tool(args: Dict[str, Any], server: "MCPServer") -> str:
    """
    向项目目录下的文件写入内容
    
    安全机制 - 沙箱模式：
    1. 所有文件操作限制在配置的基础目录内
    2. 防止路径遍历攻击
    3. 只允许写入文件，不允许覆盖系统文件
    4. 自动创建父目录（如果不存在）
    
    参数:
        args: 包含以下键的字典:
            - path: 相对于沙箱目录的文件路径 (必填)
            - content: 要写入的内容 (必填)
            - mode: 写入模式，'w' 覆盖或 'a' 追加，默认 'w'
        server: MCPServer 实例（此工具不需要）
    
    返回:
        str: 成功或错误信息
    """
    path = args.get("path", "")
    content = args.get("content", "")
    mode = args.get("mode", "w")
    
    # 参数验证
    if not path:
        return "Error: 'path' parameter is required"
    
    if content is None:
        return "Error: 'content' parameter is required"
    
    # 写入模式验证
    if mode not in ("w", "a"):
        return f"Error: Unsupported mode '{mode}'. Use 'w' (overwrite) or 'a' (append)."
    
    # 获取沙箱目录
    sandbox_dir = os.environ.get("AIHUB_SANDBOX_DIR", os.getcwd())
    sandbox_path = Path(sandbox_dir).resolve()
    
    try:
        # 拼接目标路径
        target_path = (sandbox_path / path).resolve()
        
        # ===== 安全检查：路径遍历防护 =====
        if not str(target_path).startswith(str(sandbox_path)):
            return f"Error: Access denied. Path must be within sandbox directory: {sandbox_dir}"
        
        # 检查是否是目录
        if target_path.is_dir():
            return f"Error: Cannot write to directory: {path}"
        
        # 创建父目录（如果不存在）
        target_path.parent.mkdir(parents=True, exist_ok=True)
        
        # 写入文件
        # encoding='utf-8': 指定字符编码
        # errors='replace': 遇到无法编码的字符时用替换字符
        write_mode = 'w' if mode == 'w' else 'a'
        with open(target_path, write_mode, encoding='utf-8', errors='replace') as f:
            f.write(content)
        
        # 获取写入的字节数
        byte_count = len(content.encode('utf-8'))
        
        return f"Successfully wrote {byte_count} bytes to {path}"
        
    except PermissionError:
        return f"Error: Permission denied writing file: {path}"
    except OSError as e:
        # 磁盘满、无效路径等操作系统错误
        return f"Error writing file: {str(e)}"
    except Exception as e:
        logger.error(f"File write failed: {e}")
        return f"Error writing file: {str(e)}"


# ============================================================
# 文件读取工具（沙箱保护）
# ============================================================

def file_read_tool(args: Dict[str, Any], server: "MCPServer") -> str:
    """
    读取项目目录下的文件
    
    安全机制 - 沙箱模式：
    1. 所有文件操作限制在配置的基础目录内
    2. 防止路径遍历攻击，如 ../../../etc/passwd
    3. 只允许读取文件，不允许写入或删除
    4. 限制读取行数，防止大文件撑爆内存
    
    路径遍历攻击示例（危险！）：
    - ../../etc/passwd -> 试图读取系统密码文件
    - 防御：解析后检查路径是否在沙箱内
    
    参数:
        args: 包含以下键的字典:
            - path: 相对于沙箱目录的文件路径 (必填)
            - max_lines: 最大读取行数，默认 100
        server: MCPServer 实例（此工具不需要）
    
    返回:
        str: 文件内容，或错误信息
    """
    path = args.get("path", "")
    
    if not path:
        return "Error: 'path' parameter is required"
    
    max_lines = args.get("max_lines", 100)
    
    # 获取沙箱目录
    # 可以通过环境变量 AIHUB_SANDBOX_DIR 配置
    # 如果未配置，使用当前工作目录
    sandbox_dir = os.environ.get("AIHUB_SANDBOX_DIR", os.getcwd())
    
    # Path 对象：更面向对象的路径操作
    sandbox_path = Path(sandbox_dir).resolve()  # resolve() 解析为绝对路径
    
    try:
        # 拼接目标路径
        # Path / "relative/path" 会正确拼接
        target_path = (sandbox_path / path).resolve()
        
        # ===== 安全检查：路径遍历防护 =====
        # 解析后的路径必须仍在沙箱目录内
        # 否则就是路径遍历攻击
        if not str(target_path).startswith(str(sandbox_path)):
            return f"Error: Access denied. Path must be within sandbox directory: {sandbox_dir}"
        
        # 检查文件是否存在
        if not target_path.exists():
            return f"Error: File not found: {path}"
        
        # 检查是否是文件（不是目录）
        if not target_path.is_file():
            return f"Error: Path is not a file: {path}"
        
        # ===== 读取文件内容 =====
        # encoding='utf-8': 指定字符编码
        # errors='replace': 遇到无法解码的字节时用替换字符
        with open(target_path, 'r', encoding='utf-8', errors='replace') as f:
            lines = []
            for i, line in enumerate(f):
                # 超过最大行数时停止读取
                if i >= max_lines:
                    lines.append(f"\n... (truncated, {max_lines} lines shown)")
                    break
                # rstrip() 移除行尾的换行符
                lines.append(line.rstrip('\n\r'))
            
            return '\n'.join(lines)
            
    except PermissionError:
        # 没有读取权限
        return f"Error: Permission denied reading file: {path}"
    except UnicodeDecodeError:
        # 文件不是文本文件（如二进制文件）
        return f"Error: Cannot read binary file: {path}"
    except Exception as e:
        # 其他错误
        logger.error(f"File read failed: {e}")
        return f"Error reading file: {str(e)}"


# ============================================================
# 工具定义列表（用于批量注册）
# ============================================================

"""
工具定义列表

这个列表定义了所有内置工具的元数据。
每个工具定义包含：
- name: 工具唯一名称
- description: 功能描述（AI 会看到这个）
- input_schema: JSON Schema 格式的参数规范
- handler: 实际执行函数
- is_async: 是否异步执行

为什么用列表而不是直接注册？
- 集中管理，便于查看所有可用工具
- 方便批量注册
- 便于生成文档
"""

BUILTIN_TOOLS = [
    {
        "name": "web_search",
        "description": "Search the web using DuckDuckGo. Returns relevant summaries and related topics.",
        "input_schema": {
            "type": "object",
            "properties": {
                "query": {
                    "type": "string",
                    "description": "Search query"
                }
            },
            "required": ["query"]
        },
        "handler": web_search_tool,
        "is_async": False
    },
    {
        "name": "calculator",
        "description": "Safely evaluate mathematical expressions. Supports +, -, *, /, //, %, ** and functions like abs, round, min, max, sum, pow.",
        "input_schema": {
            "type": "object",
            "properties": {
                "expression": {
                    "type": "string",
                    "description": "Mathematical expression to evaluate (e.g., '2 + 2', 'sqrt(16)', 'max(1, 5, 3)')"
                }
            },
            "required": ["expression"]
        },
        "handler": calculator_tool,
        "is_async": False
    },
    {
        "name": "http_request",
        "description": "Make HTTP GET or POST requests to external APIs and services.",
        "input_schema": {
            "type": "object",
            "properties": {
                "url": {
                    "type": "string",
                    "description": "URL to request (must start with http:// or https://)"
                },
                "method": {
                    "type": "string",
                    "description": "HTTP method (GET or POST)",
                    "enum": ["GET", "POST"]
                },
                "headers": {
                    "type": "object",
                    "description": "HTTP headers as key-value pairs"
                },
                "body": {
                    "type": ["string", "object"],
                    "description": "Request body for POST requests"
                },
                "timeout": {
                    "type": "number",
                    "description": "Timeout in seconds (default 30)"
                }
            },
            "required": ["url"]
        },
        "handler": http_request_tool,
        "is_async": False
    }
]


# ============================================================
# 注册所有内置工具
# ============================================================

def register_builtin_tools(server: "MCPServer"):
    """
    批量注册所有内置工具
    
    遍历 BUILTIN_TOOLS 列表，
    对每个工具调用 server.register_tool()
    
    参数:
        server: MCPServer 实例
    """
    for tool_def in BUILTIN_TOOLS:
        server.register_tool(
            name=tool_def["name"],
            description=tool_def["description"],
            input_schema=tool_def["input_schema"],
            handler=tool_def["handler"],
            is_async=tool_def.get("is_async", False)
        )
    
    logger.info(f"Registered {len(BUILTIN_TOOLS)} built-in tools")
