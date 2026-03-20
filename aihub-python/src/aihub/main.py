from fastapi import FastAPI, Depends
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Dict, Any, List
from .auth import get_current_user
from .mcp.server import mcp_server
from .skills.registry import skill_registry

app = FastAPI(
    title="AIHub Python Service",
    description="AIHub Agent Service with LangChain/LangGraph",
    version="0.1.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class ToolExecuteRequest(BaseModel):
    toolName: str
    arguments: Dict[str, Any]


@app.get("/health")
def health_check():
    return {"status": "healthy", "service": "aihub-python"}


@app.get("/")
def root():
    return {"message": "AIHub Python Service", "version": "0.1.0"}


@app.get("/models")
def list_models(current_user: dict = Depends(get_current_user)):
    return {"models": []}


@app.post("/chat")
def chat(message: str, current_user: dict = Depends(get_current_user)):
    return {"response": "Echo: " + message}


@app.get("/mcp/tools")
def list_mcp_tools(current_user: dict = Depends(get_current_user)):
    return {"tools": mcp_server.list_tools()}


@app.post("/mcp/execute")
def execute_mcp_tool(
    request: ToolExecuteRequest,
    current_user: dict = Depends(get_current_user)
):
    result = mcp_server.execute_tool(request.toolName, request.arguments)
    return {"result": result}


@app.get("/skills")
def list_skills(category: str = None, current_user: dict = Depends(get_current_user)):
    if category:
        skills = skill_registry.list_by_category(category)
    else:
        skills = skill_registry.list_all()
    return {"skills": skills}


@app.post("/skills/{skill_name}/execute")
def execute_skill(
    skill_name: str,
    arguments: Dict[str, Any],
    current_user: dict = Depends(get_current_user)
):
    skill = skill_registry.get(skill_name)
    result = skill.handler(arguments)
    return {"result": result}


from .agents.agent import AIAgent
from .agents.session import session_manager
from .tools.registry import registry
from .services.model_gateway import model_gateway
from fastapi.responses import StreamingResponse
import json
from typing import Optional


class ChatRequest(BaseModel):
    message: str
    session_id: Optional[str] = None
    model: str = "gpt-4"


@app.post("/api/agent/chat")
def agent_chat(
    request: ChatRequest,
    current_user: dict = Depends(get_current_user)
):
    user_id = current_user.get("user_id", 0)
    session_id = request.session_id or session_manager.create_session(user_id, "default")
    
    history = session_manager.get_session(session_id)
    
    agent = AIAgent(model_name=request.model, tools=registry.get_all_funcs())
    result = agent.chat(request.message, history)
    
    session_manager.add_message(session_id, "user", request.message)
    session_manager.add_message(session_id, "assistant", result.get("output", ""))
    
    return {"response": result.get("output", ""), "session_id": session_id}


async def chat_stream(agent: AIAgent, message: str):
    result = agent.chat(message)
    response = result.get("output", "")
    for char in response:
        yield f"data: {json.dumps({'content': char})}\n\n"
    yield "data: [DONE]\n\n"


@app.post("/api/agent/chat/stream")
async def agent_stream_chat(
    request: ChatRequest,
    current_user: dict = Depends(get_current_user)
):
    agent = AIAgent(model_name=request.model, tools=registry.get_all_funcs())
    return StreamingResponse(chat_stream(agent, request.message), media_type="text/event-stream")
