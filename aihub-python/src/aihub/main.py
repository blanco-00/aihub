"""
AIHub Python 服务主入口

这个文件是 FastAPI 应用的入口点，定义了所有的 API 路由。
FastAPI 是一个现代化的 Python Web 框架，性能接近 Node.js，易于使用且类型安全。

学习要点:
- FastAPI 路由装饰器 (@app.get, @app.post)
- 依赖注入 (Depends)
- Pydantic 数据验证 (BaseModel)
- 异步处理 (async/await)
- CORS 中间件配置
"""

# ============================================================
# 导入标准库和第三方库
# ============================================================

# FastAPI 核心：创建应用实例
from fastapi import FastAPI, Depends
from fastapi.responses import StreamingResponse

# CORS 中间件：解决跨域问题
from fastapi.middleware.cors import CORSMiddleware

# Pydantic BaseModel：用于请求/响应数据验证
# 类似于 Java 中的 DTO (Data Transfer Object)
from pydantic import BaseModel

# typing 模块：提供类型注解支持
# Python 3.5+ 引入，让你可以在代码中声明变量的"期望类型"
# 这不是强制的，但能帮助 IDE 提供自动补全和类型检查
from typing import Dict, Any, List, Optional

# 导入自定义模块
from .auth import get_current_user  # JWT 认证依赖
from .agents.agent import AIAgent  # AI Agent 核心类
from .agents.session import session_manager  # 会话管理器
from .mcp.server import mcp_server  # MCP 工具服务器

# json 模块：Python 标准库，用于 JSON 序列化/反序列化
import json


# ============================================================
# 创建 FastAPI 应用实例
# ============================================================

# FastAPI 会自动生成 OpenAPI (Swagger) 文档
# 访问 http://localhost:8001/docs 可以查看交互式 API 文档
app = FastAPI(
    title="AIHub Python Service",  # API 标题
    description="AIHub Agent Service with LangChain",  # API 描述
    version="0.1.0"  # 版本号
)


# ============================================================
# 配置 CORS (跨域资源共享)
# ============================================================

"""
CORS 是什么？
当浏览器运行在 http://localhost:3000 时，如果要请求 http://localhost:8001 的资源，
浏览器会阻止这个请求（除非服务器明确允许），这就是同源策略。

CORS 中间件告诉浏览器："允许来自其他源的请求"

配置说明：
- allow_origins=["*"]：允许所有来源。生产环境应该指定具体域名
- allow_credentials=True：允许携带认证信息（如 cookies）
- allow_methods=["*"]：允许所有 HTTP 方法 (GET, POST, PUT, DELETE...)
- allow_headers=["*"]：允许所有请求头
"""
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境应改为具体域名，如 ["http://localhost:3000"]
    allow_credentials=True,  # 允许认证信息
    allow_methods=["*"],  # 允许所有 HTTP 方法
    allow_headers=["*"],  # 允许所有请求头
)


# ============================================================
# 基础路由：健康检查和根路径
# ============================================================

@app.get("/health")
def health_check():
    """
    健康检查接口
    
    这个接口用于：
    1. Kubernetes/负载均衡器检测服务是否存活
    2. 监控服务状态
    3. 快速验证服务是否启动
    
    Returns:
        dict: 包含服务状态的字典
    """
    return {"status": "healthy", "service": "aihub-python"}


@app.get("/")
def root():
    """
    根路径接口
    
    访问 http://localhost:8001/ 时返回服务信息
    
    Returns:
        dict: 包含欢迎消息和版本号
    """
    return {"message": "AIHub Python Service", "version": "0.1.0"}


# ============================================================
# 请求/响应数据模型 (Pydantic)
# ============================================================

"""
Pydantic 是 Python 中用于数据验证的库

为什么要用 Pydantic？
1. 自动验证请求数据格式
2. 提供清晰的错误信息
3. 支持默认值和可选字段
4. 与 IDE 类型提示完美集成

类比 Java：
- Pydantic BaseModel ≈ Lombok @Data + Bean Validation
"""

class ChatRequest(BaseModel):
    message: str
    session_id: Optional[str] = None
    model: str = "gpt-4"
    system_message: Optional[str] = None
    skill_ids: Optional[List[int]] = None  # 启用的技能ID列表


# ============================================================
# 聊天 API 路由
# ============================================================

@app.post("/api/agent/chat")
def agent_chat(
    request: ChatRequest,  # 请求体，会自动验证格式
    current_user: dict = Depends(get_current_user)  # 依赖注入：自动获取当前用户
):
    """
    非流式聊天接口
    
    处理流程：
    1. 验证用户身份
    2. 创建或获取会话
    3. 调用 AI Agent 获取回复
    4. 保存对话历史到 Redis
    5. 返回 AI 回复
    
    参数:
        request: ChatRequest 对象，包含聊天请求的所有数据
        current_user: 从 JWT Token 解析出的用户信息（通过 Depends 注入）
    
    返回:
        dict: 包含 AI 回复内容和会话ID
    """
    # 从 current_user 获取用户 ID，如果不存在则默认为 0
    user_id = current_user.get("user_id", 0)
    
    # 创建新会话或使用现有的会话
    # session_manager.create_session() 返回一个 session_id
    # 如果 request.session_id 为 None，则创建新会话
    session_id = request.session_id or session_manager.create_session(user_id, "default")
    
    # 从 Redis 获取该会话的历史消息
    history = session_manager.get_session(session_id)
    
    # 创建 AI Agent 实例
    # API Key 从配置文件读取，不再从请求传递
    agent = AIAgent(
        model_name=request.model,
        system_message=request.system_message or "You are a helpful AI assistant."
    )
    
    # 调用 AI 获取回复（非流式）
    # chat_history 包含之前的对话，用于保持上下文
    result = agent.chat(request.message, history)
    
    # 将用户消息和 AI 回复保存到会话历史
    # 这样下次对话时 AI 就知道之前发生了什么
    session_manager.add_message(session_id, "user", request.message)
    session_manager.add_message(session_id, "assistant", result.get("output", ""))
    
    # 返回响应
    return {"response": result.get("output", ""), "session_id": session_id}


# ============================================================
# 流式聊天 API 路由 (SSE - Server-Sent Events)
# ============================================================

@app.post("/api/agent/chat/stream")
async def agent_stream_chat(
    request: ChatRequest,
    current_user: dict = Depends(get_current_user)
):
    """
    流式聊天接口 (SSE)
    
    与普通聊天接口的区别：
    1. 返回类型是 StreamingResponse，不是普通 JSON
    2. AI 的回复是分块返回的，不是等待完整回复
    3. 前端可以实时显示 AI 正在输入的过程（打字机效果）
    
    SSE (Server-Sent Events) 是什么？
    - 一种服务器向浏览器推送数据的技术
    - 类似于 WebSocket，但只能是单向（服务器→浏览器）
    - 适用于 AI 流式输出、实时通知等场景
    
    返回格式：
    每个数据块都是 "data: {...}\n\n" 的格式
    前端通过 EventSource API 监听这些数据
    
    参数:
        request: 聊天请求
        current_user: 当前用户（依赖注入）
    
    返回:
        StreamingResponse: FastAPI 的流式响应对象
    """
    from fastapi.responses import StreamingResponse  # 异步响应
    
    # 获取用户ID和会话ID
    user_id = current_user.get("user_id", 0)
    session_id = request.session_id or session_manager.create_session(user_id, "default")
    
    # 获取对话历史
    history = session_manager.get_session(session_id)
    
    # 创建 AI Agent
    agent = AIAgent(
        model_name=request.model,
        system_message=request.system_message or "You are a helpful AI assistant."
    )
    
    async def stream_response():
        """
        异步生成器函数：产生 SSE 格式的数据流
        
        为什么用生成器？
        - 生成器可以逐步产生数据，不需要一次性准备好所有数据
        - 配合 yield，可以实现边生成边发送
        - 非常适合 AI 流式输出的场景
        
        yield 关键字：
        - 类似于 return，但不会终止函数
        - 下次调用时，函数会从 yield 之后继续执行
        - 可以产生多个值
        """
        # 先保存用户消息
        session_manager.add_message(session_id, "user", request.message)
        full_response = ""  # 累积完整回复
        
        try:
            # agent.chat_stream() 是一个生成器
            # 它会逐步返回 AI 生成的每个字符/词
            for chunk in agent.chat_stream(request.message, history):
                full_response += chunk  # 累积到完整回复
                
                # SSE 格式：每个数据块以 "data: " 开头，"\n\n" 结尾
                # JSON.dumps 将 Python 对象转换为 JSON 字符串
                yield f"data: {json.dumps({'content': chunk})}\n\n"
            
            # 消息生成完毕，保存到会话历史
            session_manager.add_message(session_id, "assistant", full_response)
            
            # 发送结束信号
            yield f"data: {json.dumps({'content': '', 'done': True})}\n\n"
            
        except Exception as e:
            # 如果发生错误，发送错误信息
            yield f"data: {json.dumps({'error': str(e)})}\n\n"
    
    # 返回流式响应
    # media_type="text/event-stream" 告诉浏览器这是 SSE 数据
    return StreamingResponse(stream_response(), media_type="text/event-stream")


# ============================================================
# MCP 工具相关的数据模型
# ============================================================

class ToolExecuteRequest(BaseModel):
    """
    工具执行请求
    
    用于前端请求执行某个 MCP 工具
    """
    toolName: str  # 工具名称，如 "calculator"
    arguments: Dict[str, Any] = {}  # 工具参数，如 {"expression": "2+2"}


# ============================================================
# MCP 工具 API 路由
# ============================================================

@app.get("/api/mcp/tools")
def list_mcp_tools():
    tools = mcp_server.list_tools()
    return {"tools": tools, "total": len(tools)}


@app.post("/api/mcp/tools/execute")
def execute_mcp_tool(
    request: ToolExecuteRequest,
    current_user: dict = Depends(get_current_user)
):
    """
    执行指定的 MCP 工具
    
    流程：
    1. 根据工具名称查找对应的处理器
    2. 传入参数执行
    3. 返回执行结果
    
    参数:
        request: 包含工具名和参数的请求
        current_user: 当前用户（依赖注入）
    
    返回:
        dict: 包含执行结果或错误信息
    """
    try:
        # 调用 MCP 服务器执行工具
        result = mcp_server.execute_tool(request.toolName, request.arguments)
        return {
            "success": True,  # 执行成功
            "result": result,  # 执行结果
            "toolName": request.toolName
        }
    except ValueError as e:
        # ValueError: 工具不存在
        return {
            "success": False,
            "error": str(e),  # 错误信息
            "toolName": request.toolName
        }
    except Exception as e:
        # 其他异常（如工具内部错误）
        return {
            "success": False,
            "error": f"Execution failed: {str(e)}",
            "toolName": request.toolName
        }


@app.get("/api/mcp/tools/logs")
def get_mcp_tool_logs(
    limit: int = 100,  # 默认返回最近 100 条日志
    current_user: dict = Depends(get_current_user)
):
    """
    获取工具执行日志
    
    用于调试和监控：
    - 查看工具被执行了多少次
    - 查看每次执行的参数和结果
    - 查看执行耗时
    
    参数:
        limit: 最大返回日志数量
        current_user: 当前用户
    
    返回:
        dict: 包含日志列表和总数
    """
    logs = mcp_server.get_execution_logs(limit=limit)
    return {"logs": logs, "total": len(logs)}


@app.get("/api/token/stats")
def get_token_stats(
    start_date: Optional[str] = None,
    end_date: Optional[str] = None,
    current_user: dict = Depends(get_current_user)
):
    """
    获取 Token 使用统计
    
    统计当前用户的 Token 使用情况：
    - 按模型分组统计
    - 支持日期范围筛选
    - 返回总调用次数和 Token 消耗
    
    参数:
        start_date: 开始日期 (YYYY-MM-DD)
        end_date: 结束日期 (YYYY-MM-DD)
        current_user: 当前用户
    
    返回:
        dict: 包含总计和按模型分组的统计
    """
    from datetime import datetime
    from .services.model_gateway import model_gateway
    
    user_id = current_user.get("user_id", 0)
    
    start = datetime.strptime(start_date, "%Y-%m-%d") if start_date else None
    end = datetime.strptime(end_date, "%Y-%m-%d") if end_date else None
    
    stats = model_gateway.get_token_stats(user_id=user_id, start_date=start, end_date=end)
    
    return stats


@app.get("/api/agent/templates")
def list_agent_templates(
    current_user: dict = Depends(get_current_user)
):
    from .services.agent_template import agent_template_service
    templates = agent_template_service.list_templates()
    return {
        "templates": [
            {
                "id": t.id,
                "name": t.name,
                "description": t.description,
                "system_message": t.system_message,
                "model_name": t.model_name,
                "tools": t.tools,
                "is_default": t.is_default
            }
            for t in templates
        ]
    }


@app.get("/api/agent/templates/{template_id}")
def get_agent_template(
    template_id: int,
    current_user: dict = Depends(get_current_user)
):
    from .services.agent_template import agent_template_service
    template = agent_template_service.get_template(template_id)
    if not template:
        return {"error": "Template not found"}, 404
    return {
        "id": template.id,
        "name": template.name,
        "description": template.description,
        "system_message": template.system_message,
        "model_name": template.model_name,
        "tools": template.tools,
        "config": template.config,
        "is_default": template.is_default
    }


@app.post("/api/agent/templates")
def create_agent_template(
    request: Dict[str, Any],
    current_user: dict = Depends(get_current_user)
):
    from .services.agent_template import agent_template_service
    user_id = current_user.get("user_id")
    
    template = agent_template_service.create_template(
        name=request.get("name"),
        description=request.get("description"),
        system_message=request.get("system_message", "You are a helpful AI assistant."),
        model_name=request.get("model_name", "glm-4.7"),
        tools=request.get("tools", []),
        config=request.get("config", {}),
        created_by=user_id
    )
    
    if not template:
        return {"error": "Failed to create template"}, 500
    return {
        "id": template.id,
        "name": template.name,
        "description": template.description,
        "system_message": template.system_message,
        "model_name": template.model_name,
        "tools": template.tools
    }


@app.post("/api/agent/chat/template/{template_id}")
async def chat_with_template(
    template_id: int,
    request: ChatRequest,
    current_user: dict = Depends(get_current_user)
):
    from .services.agent_template import agent_template_service
    
    template = agent_template_service.get_template(template_id)
    if not template:
        return {"error": "Template not found"}, 404
    
    user_id = current_user.get("user_id", 0)
    session_id = request.session_id or session_manager.create_session(user_id, template.name)
    
    model_name = request.model or template.model_name
    agent = AIAgent(
        model_name=model_name,
        system_message=template.system_message,
        user_id=user_id,
        session_id=session_id
    )
    
    async def stream_response():
        session_manager.add_message(session_id, "user", request.message)
        full_response = ""
        try:
            for chunk in agent.chat_stream(request.message):
                full_response += chunk
                yield f"data: {json.dumps({'content': chunk})}\n\n"
            session_manager.add_message(session_id, "assistant", full_response)
            yield "data: [DONE]\n\n"
        except Exception as e:
            yield f"data: {json.dumps({'error': str(e)})}\n\n"
    
    return StreamingResponse(
        stream_response(),
        media_type="text/event-stream"
    )


class DocumentUploadRequest(BaseModel):
    name: str
    content: str
    content_type: str = "text/plain"
    metadata: Optional[Dict[str, Any]] = None


@app.post("/api/rag/documents")
def upload_document(
    request: DocumentUploadRequest,
    current_user: dict = Depends(get_current_user)
):
    from .services.rag_service import rag_service
    user_id = current_user.get("user_id")
    
    doc = rag_service.parse_document(
        name=request.name,
        content=request.content,
        content_type=request.content_type,
        user_id=user_id,
        metadata=request.metadata
    )
    
    return {
        "id": doc.id,
        "name": doc.name,
        "chunks": len(doc.chunks)
    }


@app.get("/api/rag/documents")
def list_documents(
    current_user: dict = Depends(get_current_user)
):
    from .services.rag_service import rag_service
    user_id = current_user.get("user_id")
    
    docs = rag_service.list_documents(user_id)
    return {
        "documents": [
            {
                "id": d.id,
                "name": d.name,
                "content_type": d.content_type,
                "chunks": len(d.chunks)
            }
            for d in docs
        ]
    }


@app.get("/api/rag/search")
def search_documents(
    q: str,
    top_k: int = 5,
    current_user: dict = Depends(get_current_user)
):
    from .services.rag_service import rag_service
    
    results = rag_service.search(q, top_k=top_k)
    return {
        "results": [
            {
                "document_id": doc.id,
                "document_name": doc.name,
                "score": score,
                "chunk": chunk_text[:200]
            }
            for doc, score, chunk_text in results
        ]
    }


@app.post("/api/rag/chat")
async def rag_chat(
    request: ChatRequest,
    top_k: int = 3,
    current_user: dict = Depends(get_current_user)
):
    from .services.rag_service import rag_service
    
    user_id = current_user.get("user_id", 0)
    session_id = request.session_id or session_manager.create_session(user_id, "rag")
    
    results = rag_service.search(request.message, top_k=top_k)
    
    context = ""
    if results:
        context = "参考信息：\n"
        for doc, score, chunk_text in results:
            context += f"- [{doc.name}] {chunk_text}\n"
        context += "\n"
    
    system_prompt = context + "你是一个有帮助的AI助手。根据上面的参考信息回答用户的问题。"
    
    agent = AIAgent(
        model_name=request.model or "glm-4.7",
        system_message=system_prompt,
        user_id=user_id,
        session_id=session_id
    )
    
    async def stream_response():
        session_manager.add_message(session_id, "user", request.message)
        full_response = ""
        try:
            for chunk in agent.chat_stream(request.message):
                full_response += chunk
                yield f"data: {json.dumps({'content': chunk})}\n\n"
            session_manager.add_message(session_id, "assistant", full_response)
            yield "data: [DONE]\n\n"
        except Exception as e:
            yield f"data: {json.dumps({'error': str(e)})}\n\n"
    
    return StreamingResponse(
        stream_response(),
        media_type="text/event-stream"
    )


class SkillCreateRequest(BaseModel):
    name: str
    description: Optional[str] = None
    trigger_keywords: Optional[List[str]] = None
    system_message: str = "你是一个有帮助的AI助手。"
    tools: Optional[List[str]] = None
    mcp_tools: Optional[List[str]] = None
    config: Optional[Dict[str, Any]] = None


@app.get("/api/skills")
def list_skills(
    current_user: dict = Depends(get_current_user)
):
    from .services.skill_service import skill_service
    skills = skill_service.list_skills()
    return {
        "skills": [
            {
                "id": s.id,
                "name": s.name,
                "description": s.description,
                "trigger_keywords": s.trigger_keywords,
                "is_builtin": s.is_builtin
            }
            for s in skills
        ]
    }


@app.get("/api/skills/{skill_id}")
def get_skill(
    skill_id: int,
    current_user: dict = Depends(get_current_user)
):
    from .services.skill_service import skill_service
    skill = skill_service.get_skill(skill_id)
    if not skill:
        return {"error": "Skill not found"}, 404
    return {
        "id": skill.id,
        "name": skill.name,
        "description": skill.description,
        "trigger_keywords": skill.trigger_keywords,
        "system_message": skill.system_message,
        "tools": skill.tools,
        "mcp_tools": skill.mcp_tools,
        "config": skill.config,
        "is_builtin": skill.is_builtin
    }


@app.post("/api/skills")
def create_skill(
    request: SkillCreateRequest,
    current_user: dict = Depends(get_current_user)
):
    from .services.skill_service import skill_service
    user_id = current_user.get("user_id")
    
    skill = skill_service.create_skill(
        name=request.name,
        description=request.description,
        trigger_keywords=request.trigger_keywords,
        system_message=request.system_message,
        tools=request.tools,
        mcp_tools=request.mcp_tools,
        config=request.config,
        created_by=user_id
    )
    
    if not skill:
        return {"error": "Failed to create skill"}, 500
    return {
        "id": skill.id,
        "name": skill.name,
        "description": skill.description
    }


class DetectSkillRequest(BaseModel):
    message: str


@app.post("/api/skills/detect")
def detect_skill(
    request: DetectSkillRequest,
    current_user: dict = Depends(get_current_user)
):
    from .services.skill_service import skill_service
    skill = skill_service.detect_skill(request.message)
    if not skill:
        return {"skill": None}
    return {
        "skill": {
            "id": skill.id,
            "name": skill.name,
            "description": skill.description,
            "system_message": skill.system_message
        }
    }


@app.post("/api/skills/chat/{skill_id}")
async def chat_with_skill(
    skill_id: int,
    request: ChatRequest,
    current_user: dict = Depends(get_current_user)
):
    from .services.skill_service import skill_service
    
    skill = skill_service.get_skill(skill_id)
    if not skill:
        return {"error": "Skill not found"}, 404
    
    user_id = current_user.get("user_id", 0)
    session_id = request.session_id or session_manager.create_session(user_id, skill.name)
    
    agent = AIAgent(
        model_name=request.model or "glm-4.7",
        system_message=skill.system_message,
        user_id=user_id,
        session_id=session_id
    )
    
    async def stream_response():
        session_manager.add_message(session_id, "user", request.message)
        full_response = ""
        try:
            for chunk in agent.chat_stream(request.message):
                full_response += chunk
                yield f"data: {json.dumps({'content': chunk})}\n\n"
            session_manager.add_message(session_id, "assistant", full_response)
            yield "data: [DONE]\n\n"
        except Exception as e:
            yield f"data: {json.dumps({'error': str(e)})}\n\n"
    
    return StreamingResponse(
        stream_response(),
        media_type="text/event-stream"
    )
