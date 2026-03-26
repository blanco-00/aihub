from fastapi import FastAPI, Depends
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from typing import Optional, AsyncGenerator
import json
from ..auth import get_current_user
from ..agents.agent import AIAgent
from ..agents.session import session_manager

app = FastAPI(
    title="AIHub Python Service",
    description="AIHub Agent Service with LangChain/LangGraph",
    version="0.1.0"
)


class ChatRequest(BaseModel):
    message: str
    session_id: Optional[str] = None
    model: Optional[str] = "glm-4.7"


class StreamChatRequest(BaseModel):
    message: str
    session_id: Optional[str] = None
    model: Optional[str] = "glm-4.7"


async def chat_generator(agent: AIAgent, message: str, session_id: str) -> AsyncGenerator[str, None]:
    session_manager.add_message(session_id, "user", message)
    
    collected = []
    for chunk in agent.chat_stream(message):
        if chunk:
            collected.append(chunk)
            yield f"data: {json.dumps({'content': chunk})}\n\n"
    
    full_response = "".join(collected)
    session_manager.add_message(session_id, "assistant", full_response)
    
    yield "data: [DONE]\n\n"


@app.post("/api/agent/chat")
async def chat(
    request: ChatRequest,
    current_user: dict = Depends(get_current_user)
):
    user_id = current_user.get("user_id", 0)
    session_id = request.session_id or session_manager.create_session(user_id, "default")
    
    history = session_manager.get_session(session_id)
    
    model_name = request.model or "glm-4.7"
    agent = AIAgent(model_name=model_name, user_id=user_id, session_id=session_id)
    
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
    
    model_name = request.model or "glm-4.7"
    agent = AIAgent(model_name=model_name, user_id=user_id, session_id=session_id)
    
    return StreamingResponse(
        chat_generator(agent, request.message, session_id),
        media_type="text/event-stream"
    )


@app.get("/api/token/stats")
async def get_token_stats(
    start_date: Optional[str] = None,
    end_date: Optional[str] = None,
    current_user: dict = Depends(get_current_user)
):
    from datetime import datetime
    from ..services.model_gateway import model_gateway
    
    user_id = current_user.get("user_id", 0)
    
    start = datetime.strptime(start_date, "%Y-%m-%d") if start_date else None
    end = datetime.strptime(end_date, "%Y-%m-%d") if end_date else None
    
    stats = model_gateway.get_token_stats(user_id=user_id, start_date=start, end_date=end)
    
    return stats
