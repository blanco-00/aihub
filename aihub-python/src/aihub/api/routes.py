from fastapi import FastAPI, Depends
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from typing import Dict, Any, List, Optional, AsyncGenerator
import json
from .auth import get_current_user
from .agents.agent import AIAgent
from .agents.session import session_manager
from .services.model_gateway import model_gateway
from .tools.registry import registry

app = FastAPI(
    title="AIHub Python Service",
    description="AIHub Agent Service with LangChain/LangGraph",
    version="0.1.0"
)


class ChatRequest(BaseModel):
    message: str
    session_id: Optional[str] = None
    model: Optional[str] = "gpt-4"


class StreamChatRequest(BaseModel):
    message: str
    session_id: Optional[str] = None
    model: Optional[str] = "gpt-4"


async def chat_generator(agent: AIAgent, message: str) -> AsyncGenerator[str, None]:
    result = agent.chat(message)
    response = result.get("output", "")
    for char in response:
        yield f"data: {json.dumps({'content': char})}\n\n"
    yield "data: [DONE]\n\n"


@app.post("/api/agent/chat")
async def chat(
    request: ChatRequest,
    current_user: dict = Depends(get_current_user)
):
    user_id = current_user.get("user_id", 0)
    session_id = request.session_id or session_manager.create_session(user_id, "default")
    
    history = session_manager.get_session(session_id)
    
    agent = AIAgent(
        model_name=request.model,
        tools=registry.get_all_funcs()
    )
    
    result = agent.chat(request.message, history)
    
    session_manager.add_message(session_id, "user", request.message)
    session_manager.add_message(session_id, "assistant", result.get("output", ""))
    
    return {
        "response": result.get("output", ""),
        "session_id": session_id
    }


@app.post("/api/agent/chat/stream")
async def stream_chat(
    request: StreamChatRequest,
    current_user: dict = Depends(get_current_user)
):
    user_id = current_user.get("user_id", 0)
    session_id = request.session_id or session_manager.create_session(user_id, "default")
    
    agent = AIAgent(
        model_name=request.model,
        tools=registry.get_all_funcs()
    )
    
    return StreamingResponse(
        chat_generator(agent, request.message),
        media_type="text/event-stream"
    )
