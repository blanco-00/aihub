from fastapi import FastAPI, Depends
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Dict, Any, List, Optional
from .auth import get_current_user
from .agents.agent import AIAgent
from .agents.session import session_manager
import json

app = FastAPI(
    title="AIHub Python Service",
    description="AIHub Agent Service with LangChain",
    version="0.1.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
def health_check():
    return {"status": "healthy", "service": "aihub-python"}


@app.get("/")
def root():
    return {"message": "AIHub Python Service", "version": "0.1.0"}


class ChatRequest(BaseModel):
    message: str
    session_id: Optional[str] = None
    model: str = "gpt-4"
    api_key: Optional[str] = None
    base_url: Optional[str] = None
    system_message: Optional[str] = None


@app.post("/api/agent/chat")
def agent_chat(
    request: ChatRequest,
    current_user: dict = Depends(get_current_user)
):
    user_id = current_user.get("user_id", 0)
    session_id = request.session_id or session_manager.create_session(user_id, "default")
    
    history = session_manager.get_session(session_id)
    
    agent = AIAgent(
        model_name=request.model,
        api_key=request.api_key or "",
        base_url=request.base_url,
        system_message=request.system_message or "You are a helpful AI assistant."
    )
    result = agent.chat(request.message, history)
    
    session_manager.add_message(session_id, "user", request.message)
    session_manager.add_message(session_id, "assistant", result.get("output", ""))
    
    return {"response": result.get("output", ""), "session_id": session_id}


@app.post("/api/agent/chat/stream")
async def agent_stream_chat(
    request: ChatRequest,
    current_user: dict = Depends(get_current_user)
):
    from fastapi.responses import StreamingResponse
    
    user_id = current_user.get("user_id", 0)
    session_id = request.session_id or session_manager.create_session(user_id, "default")
    
    history = session_manager.get_session(session_id)
    
    agent = AIAgent(
        model_name=request.model,
        api_key=request.api_key or "",
        base_url=request.base_url,
        system_message=request.system_message or "You are a helpful AI assistant."
    )
    
    async def stream_response():
        session_manager.add_message(session_id, "user", request.message)
        full_response = ""
        
        try:
            for chunk in agent.chat_stream(request.message, history):
                full_response += chunk
                yield f"data: {json.dumps({'content': chunk})}\n\n"
            
            session_manager.add_message(session_id, "assistant", full_response)
            yield f"data: {json.dumps({'content': '', 'done': True})}\n\n"
        except Exception as e:
            yield f"data: {json.dumps({'error': str(e)})}\n\n"
    
    return StreamingResponse(stream_response(), media_type="text/event-stream")
