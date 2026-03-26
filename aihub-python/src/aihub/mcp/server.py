"""
MCP Server - Model Context Protocol 工具服务器

MCP 是什么？
Model Context Protocol (MCP) 是一种让 AI 模型与外部工具交互的标准协议。
它允许 AI 在对话过程中调用各种外部工具来完成任务，如搜索网页、计算数学题等。

核心概念：
1. 工具注册 (register_tool)：将一个函数注册为 AI 可调用的工具
2. 工具列表 (list_tools)：列出所有可用工具及其参数规范
3. 工具执行 (execute_tool)：执行指定的工具并返回结果
4. 执行日志 (execution_logs)：记录工具调用的详细信息

学习要点：
- Python 类和面向对象编程
- HTTP 客户端连接池管理
- 异步编程基础 (async/await)
- 装饰器模式和策略模式
- 上下文管理器
- 日志记录最佳实践
"""

# ============================================================
# 导入标准库和第三方库
# ============================================================

from typing import Dict, Any, Callable, Optional, Awaitable, Union
from datetime import datetime
import json
import asyncio
import logging
import time
import httpx
import pymysql
from collections import defaultdict
from dataclasses import dataclass, field
from threading import Lock


# ============================================================
# 配置日志记录器
# ============================================================

"""
日志级别（从低到高）：
DEBUG: 调试信息，详细记录程序执行细节
INFO: 一般信息，记录程序正常运行
WARNING: 警告信息，潜在问题但不影响运行
ERROR: 错误信息，功能受损
CRITICAL: 严重错误，程序可能崩溃

__name__ 是什么？
Python 内置变量，表示当前模块的完整名称
logging.getLogger(__name__) 创建一个以模块名命名的 logger
这样可以方便地追踪日志来自哪个模块
"""
logger = logging.getLogger(__name__)


# ============================================================
# ExecutionLog 类：工具执行记录
# ============================================================

class ExecutionLog:
    """
    工具执行记录类
    
    用途：保存每次工具调用的详细信息，用于：
    1. 调试：查看工具输入输出
    2. 监控：统计工具使用情况
    3. 性能分析：计算执行耗时
    
    类比 Java：
    - 这个类 ≈ POJO (Plain Old Java Object)
    - __init__ 中的参数 ≈ 构造器参数
    - self.属性名 = 参数值 ≈ Lombok 自动生成的赋值
    
    属性:
        tool_name: 被执行的工具名称
        arguments: 传入工具的参数
        started_at: 开始执行的时间
        completed_at: 完成执行的时间
        result: 执行结果
        error: 错误信息（如果执行失败）
        duration_ms: 执行耗时（毫秒）
    """
    
    def __init__(
        self,
        tool_name: str,  # 工具名称
        arguments: Dict[str, Any],  # 执行参数
        started_at: datetime,  # 开始时间
        completed_at: Optional[datetime] = None,  # 完成时间
        result: Optional[Any] = None,  # 执行结果
        error: Optional[str] = None,  # 错误信息
        duration_ms: Optional[float] = None  # 执行耗时
    ):
        # 实例属性赋值
        self.tool_name = tool_name
        self.arguments = arguments
        self.started_at = started_at
        self.completed_at = completed_at
        self.result = result
        self.error = error
        self.duration_ms = duration_ms
    
    def to_dict(self) -> Dict[str, Any]:
        """
        将执行记录转换为字典格式
        
        为什么要这个方法？
        - 方便序列化为 JSON
        - 便于网络传输和存储
        
        Returns:
            dict: 包含所有字段的字典
        
        学习要点：
        - self.method() 调用同类其他方法
        - 三元表达式: A if 条件 else B
        - f-string: f"text {variable}" 字符串格式化
        - 切片操作: [:500] 取前500个字符
        """
        return {
            "tool_name": self.tool_name,
            "arguments": self.arguments,
            # .isoformat() 将 datetime 转为 ISO 8601 字符串格式
            # 如 "2024-01-15T10:30:00"
            "started_at": self.started_at.isoformat() if self.started_at else None,
            "completed_at": self.completed_at.isoformat() if self.completed_at else None,
            # str(result)[:500] 将结果转为字符串并截取前500字符
            # 防止结果过大导致日志膨胀
            "result": str(self.result)[:500] if self.result else None,
            "error": self.error,
            "duration_ms": self.duration_ms
        }


# ============================================================
# MCPServer 类：MCP 协议实现
# ============================================================

@dataclass
class RateLimitConfig:
    max_calls: int = 100
    window_seconds: int = 60

@dataclass
class UserRateLimit:
    calls: list = field(default_factory=list)
    lock: Lock = field(default_factory=Lock)

class MCPServer:
    def __init__(
        self,
        http_timeout: float = 30.0,
        max_connections: int = 100,
        max_concurrent: int = 10,
        rate_limit_config: Optional[RateLimitConfig] = None
    ):
        self._tools: Dict[str, Dict[str, Any]] = {}
        self._execution_logs: list[ExecutionLog] = []
        self._http_client: Optional[httpx.AsyncClient] = None
        self._sync_http_client: Optional[httpx.Client] = None
        
        self._http_timeout = http_timeout
        self._max_connections = max_connections
        
        self._max_concurrent = max_concurrent
        self._semaphore = asyncio.Semaphore(max_concurrent)
        
        self._rate_limit_config = rate_limit_config or RateLimitConfig()
        self._user_rate_limits: Dict[str, UserRateLimit] = defaultdict(UserRateLimit)
        self._rate_limit_lock = Lock()
    
    def _check_rate_limit(self, user_id: str) -> bool:
        if not self._rate_limit_config or self._rate_limit_config.max_calls <= 0:
            return True
        
        with self._rate_limit_lock:
            user_limit = self._user_rate_limits[user_id]
            with user_limit.lock:
                now = time.time()
                window = self._rate_limit_config.window_seconds
                max_calls = self._rate_limit_config.max_calls
                
                user_limit.calls = [
                    t for t in user_limit.calls
                    if now - t < window
                ]
                
                if len(user_limit.calls) >= max_calls:
                    return False
                
                user_limit.calls.append(now)
                return True
    
    def _get_rate_limit_remaining(self, user_id: str) -> int:
        if not self._rate_limit_config or self._rate_limit_config.max_calls <= 0:
            return -1
        
        with self._rate_limit_lock:
            user_limit = self._user_rate_limits[user_id]
            with user_limit.lock:
                now = time.time()
                window = self._rate_limit_config.window_seconds
                active_calls = [
                    t for t in user_limit.calls
                    if now - t < window
                ]
                return max(0, self._rate_limit_config.max_calls - len(active_calls))
    
    # ========================================================
    # HTTP 客户端管理
    # ========================================================
    
    def _get_sync_http_client(self) -> httpx.Client:
        """
        获取或创建同步 HTTP 客户端（懒加载模式）
        
        什么是懒加载？
        - 第一次调用时才创建客户端
        - 之后复用已创建的客户端
        - 避免不必要的资源占用
        
        什么是连接池？
        - 预先建立多个 HTTP 连接
        - 请求来时直接使用已有连接
        - 省去建立连接的开销
        
        httpx.Limits 参数：
        - max_connections: 最大连接数
        - max_keepalive_connections: 保持活跃的最大连接数
        
        Returns:
            httpx.Client: 配置好的同步 HTTP 客户端
        """
        if self._sync_http_client is None:  # 尚未创建
            # httpx.Limits 控制连接池大小
            limits = httpx.Limits(max_connections=self._max_connections)
            
            # 创建同步客户端
            # timeout: 单次请求的超时时间
            # limits: 连接池配置
            self._sync_http_client = httpx.Client(
                timeout=self._http_timeout,
                limits=limits
            )
        
        return self._sync_http_client
    
    async def _get_async_http_client(self) -> httpx.AsyncClient:
        """
        获取或创建异步 HTTP 客户端
        
        与同步版本区别：
        - 返回 httpx.AsyncClient（不是 httpx.Client）
        - 支持 await 异步调用
        - 用于 async 工具处理器
        
        Returns:
            httpx.AsyncClient: 配置好的异步 HTTP 客户端
        """
        if self._http_client is None:
            limits = httpx.Limits(max_connections=self._max_connections)
            self._http_client = httpx.AsyncClient(
                timeout=self._http_timeout,
                limits=limits
            )
        
        return self._http_client
    
    def get_http_client(self) -> httpx.Client:
        """
        公开方法：获取同步 HTTP 客户端
        
        工具处理器调用此方法获取 HTTP 客户端
        
        Returns:
            httpx.Client: 同步 HTTP 客户端
        """
        return self._get_sync_http_client()
    
    async def get_async_http_client(self) -> httpx.AsyncClient:
        """
        公开方法：获取异步 HTTP 客户端
        
        async 工具处理器调用此方法
        
        Returns:
            httpx.AsyncClient: 异步 HTTP 客户端
        """
        return await self._get_async_http_client()
    
    # ========================================================
    # 工具管理
    # ========================================================
    
    def register_tool(
        self,
        name: str,  # 工具唯一名称
        description: str,  # 工具描述，AI 会看到这个描述来决定何时调用
        input_schema: Dict[str, Any],  # JSON Schema 格式的参数规范
        handler: Callable,  # 处理函数
        is_async: bool = False  # 是否为异步处理器
    ):
        """
        注册一个新工具
        
        工具注册流程：
        1. 开发者定义工具函数
        2. 定义输入参数的 JSON Schema
        3. 调用 register_tool 注册
        4. 工具即可被 AI 调用
        
        JSON Schema 示例：
        ```json
        {
            "type": "object",
            "properties": {
                "expression": {
                    "type": "string",
                    "description": "数学表达式，如 2+2"
                }
            },
            "required": ["expression"]
        }
        ```
        
        参数:
            name: 工具唯一标识名
            description: 工具功能描述
            input_schema: 参数规范（JSON Schema 格式）
            handler: 执行逻辑的函数
            is_async: 处理器是否为异步函数
        """
        self._tools[name] = {
            "description": description,
            "inputSchema": input_schema,
            "handler": handler,  # 保存函数引用
            "is_async": is_async  # 标记是否异步
        }
        # 记录日志
        logger.info(f"Registered tool: {name}")
    
    def list_tools(self) -> list:
        """
        列出所有已注册的工具
        
        不返回处理器函数，只返回元数据
        这样 AI 可以知道有哪些工具可用
        但不能直接执行它们
        
        Returns:
            list: 包含工具信息的列表
        """
        return [
            {
                "name": name,
                "description": info["description"],
                "inputSchema": info["inputSchema"]
            }
            for name, info in self._tools.items()
        ]
    
    # ========================================================
    # 工具执行
    # ========================================================
    
    def execute_tool(
        self,
        name: str,
        arguments: Dict[str, Any],
        user_id: Optional[str] = None,
        session_id: Optional[str] = None
    ) -> Any:
        if user_id is None:
            user_id = "default"
        
        if name not in self._tools:
            raise ValueError(f"Tool '{name}' not found")
        
        if not self._check_rate_limit(user_id):
            raise ValueError(
                f"Rate limit exceeded. Try again later. "
                f"Remaining calls: {self._get_rate_limit_remaining(user_id)}"
            )
        
        handler = self._tools[name]["handler"]
        is_async = self._tools[name].get("is_async", False)
        
        started_at = datetime.now()
        start_time = time.perf_counter()
        
        error = None
        result = None
        
        try:
            if is_async:
                result = asyncio.run(handler(arguments, self))
            else:
                result = handler(arguments, self)
        except Exception as e:
            error = str(e)
            logger.error(f"Tool '{name}' execution failed: {error}")
            raise
        finally:
            completed_at = datetime.now()
            duration_ms = (time.perf_counter() - start_time) * 1000
            
            log_entry = ExecutionLog(
                tool_name=name,
                arguments=arguments,
                started_at=started_at,
                completed_at=completed_at,
                result=result,
                error=error,
                duration_ms=duration_ms
            )
            
            self._execution_logs.append(log_entry)
            self._save_log_to_db(log_entry, user_id, session_id)
            
            logger.info(f"Tool '{name}' executed in {duration_ms:.2f}ms")
        
        return result
    
    async def execute_tool_async(
        self,
        name: str,
        arguments: Dict[str, Any],
        user_id: Optional[str] = None,
        session_id: Optional[str] = None
    ) -> Any:
        if user_id is None:
            user_id = "default"
        
        if name not in self._tools:
            raise ValueError(f"Tool '{name}' not found")
        
        if not self._check_rate_limit(user_id):
            raise ValueError(
                f"Rate limit exceeded. Try again later. "
                f"Remaining calls: {self._get_rate_limit_remaining(user_id)}"
            )
        
        handler = self._tools[name]["handler"]
        is_async = self._tools[name].get("is_async", False)
        
        started_at = datetime.now()
        start_time = time.perf_counter()
        
        error = None
        result = None
        
        async with self._semaphore:
            try:
                if is_async:
                    result = await handler(arguments, self)
                else:
                    result = await asyncio.to_thread(handler, arguments, self)
            except Exception as e:
                error = str(e)
                logger.error(f"Tool '{name}' execution failed: {error}")
                raise
            finally:
                completed_at = datetime.now()
                duration_ms = (time.perf_counter() - start_time) * 1000
                
                log_entry = ExecutionLog(
                    tool_name=name,
                    arguments=arguments,
                    started_at=started_at,
                    completed_at=completed_at,
                    result=result,
                    error=error,
                    duration_ms=duration_ms
                )
                self._execution_logs.append(log_entry)
                self._save_log_to_db(log_entry, user_id, session_id)
                logger.info(f"Tool '{name}' executed in {duration_ms:.2f}ms")
            
            return result
    
    def get_tool(self, name: str) -> Dict[str, Any]:
        """
        获取工具的完整元数据（包含处理器）
        
        参数:
            name: 工具名称
        
        返回:
            dict: 包含所有元数据的字典
        """
        if name not in self._tools:
            raise ValueError(f"Tool '{name}' not found")
        return self._tools[name]
    
    # ========================================================
    # 日志管理
    # ========================================================
    
    def get_execution_logs(self, limit: int = 100) -> list[Dict[str, Any]]:
        """
        获取执行日志
        
        返回最近的 limit 条日志
        
        参数:
            limit: 最大返回条数，默认 100
        
        返回:
            list: 执行日志列表（字典格式）
        """
        return [log.to_dict() for log in self._execution_logs[-limit:]]
    
    def _save_log_to_db(self, log_entry: ExecutionLog, user_id: Optional[str] = None, session_id: Optional[str] = None):
        try:
            from ..config import get_db_connection
            conn = get_db_connection()
            with conn.cursor() as cur:
                cur.execute(
                    """INSERT INTO mcp_execution_log 
                       (tool_name, arguments, result, error, duration_ms, user_id, session_id, started_at, completed_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)""",
                    (
                        log_entry.tool_name,
                        json.dumps(log_entry.arguments, ensure_ascii=False),
                        str(log_entry.result)[:5000] if log_entry.result else None,
                        log_entry.error,
                        log_entry.duration_ms,
                        user_id,
                        session_id,
                        log_entry.started_at,
                        log_entry.completed_at
                    )
                )
            conn.close()
        except Exception as e:
            logger.error(f"Failed to save log to DB: {e}")
    
    def clear_execution_logs(self):
        """
        清空所有执行日志
        
        用途：释放内存，或在日志已存储到数据库后清空
        """
        self._execution_logs.clear()
    
    # ========================================================
    # 资源清理
    # ========================================================
    
    async def close(self):
        """
        关闭所有 HTTP 客户端连接
        
        为什么需要这个方法？
        - HTTP 客户端持有网络连接
        - 不关闭会导致资源泄漏
        - 应该在应用退出时调用
        
        在 FastAPI 中，可以这样使用：
        ```python
        @app.on_event("shutdown")
        async def shutdown_event():
            await mcp_server.close()
        ```
        """
        if self._http_client:
            # aclose() 是异步关闭方法
            await self._http_client.aclose()
            self._http_client = None
        
        if self._sync_http_client:
            # close() 是同步关闭方法
            self._sync_http_client.close()
            self._sync_http_client = None


# ============================================================
# 全局 MCP 服务器实例
# ============================================================

"""
单例模式的应用

mcp_server 是一个全局实例，整个应用共享同一个 MCPServer。
这样做的好处：
1. 所有工具只注册一次
2. HTTP 连接池只创建一次
3. 执行日志集中管理

类比 Java 的 static final 实例
"""

mcp_server = MCPServer()


# ============================================================
# 内置工具示例
# ============================================================

def handle_order_query(args: Dict[str, Any], server: MCPServer) -> str:
    """
    查询订单状态的示例工具
    
    这是一个模拟工具，返回固定格式的订单信息
    实际项目中，这里会调用真实的订单系统 API
    
    参数:
        args: 包含 orderId 的字典
        server: MCPServer 实例（用于访问 HTTP 客户端等资源）
    
    返回:
        str: 格式化的订单信息
    """
    # args.get("orderId", "") 安全地获取值
    # 如果不存在，返回空字符串而不是抛出异常
    order_id = args.get("orderId", "")
    return f"Order {order_id}: Status=Shipped, Expected delivery=2024-01-15"


def handle_user_info(args: Dict[str, Any], server: MCPServer) -> str:
    """
    查询用户信息的示例工具
    """
    user_id = args.get("userId", "")
    return f"User {user_id}: Name=John Doe, Email=john@example.com"


# ============================================================
# 注册内置工具
# ============================================================

"""
模块导入时自动注册工具

这里展示了 Python 的特色用法：
import 时执行代码块（side effect）

这种方式的好处：
1. 导入模块时工具就自动注册好了
2. 不需要额外的初始化代码
3. 工具始终可用

但也有缺点：循环导入问题
"""

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


# ============================================================
# 注册内置工具（从 builtin.py）
# ============================================================

"""
延迟导入避免循环依赖

如果直接在文件开头导入 builtin.py：
from ..tools.builtin import register_builtin_tools

可能会导致循环导入问题：
- main.py 导入 mcp.server
- server.py 导入 tools.builtin
- builtin.py 导入 mcp.server（如果需要）

解决方案：
1. 在需要时才导入（延迟导入）
2. 重构模块结构
3. 使用依赖注入

这里我们把导入放在最后，在所有类定义完成后才执行
"""

from ..tools.builtin import register_builtin_tools

# 调用函数注册所有内置工具
# 这个函数会遍历 BUILTIN_TOOLS 列表，逐个注册
register_builtin_tools(mcp_server)
