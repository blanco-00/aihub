from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, SystemMessage
from langchain_core.callbacks import BaseCallbackHandler
from typing import List, Dict, Any, Optional
from ..config import MODEL_CONFIGS
import time


class TokenUsageCallback(BaseCallbackHandler):
    def __init__(self):
        self.input_tokens = 0
        self.output_tokens = 0
        self.start_time = None
        self.end_time = None
    
    def on_llm_start(self, serialized, prompts, **kwargs):
        self.start_time = time.perf_counter()
    
    def on_llm_end(self, response, **kwargs):
        self.end_time = time.perf_counter()
        if hasattr(response, 'usage'):
            usage = response.usage
            if usage:
                self.input_tokens = usage.prompt_tokens or 0
                self.output_tokens = usage.completion_tokens or 0
    
    @property
    def duration_ms(self) -> int:
        if self.start_time and self.end_time:
            return int((self.end_time - self.start_time) * 1000)
        return 0


class AIAgent:
    def __init__(
        self,
        model_name: str = "gpt-4",
        api_key: str = None,
        base_url: str = None,
        system_message: str = "You are a helpful AI assistant.",
        user_id: int = None,
        session_id: str = None
    ):
        self.model_name = model_name
        self.system_message = system_message
        self.user_id = user_id
        self.session_id = session_id
        
        if api_key is None or api_key == "":
            config = MODEL_CONFIGS.get(model_name, {})
            self.api_key = config.get("api_key", "")
            self.base_url = config.get("base_url") or base_url
            self.provider = config.get("provider", "openai")
        else:
            self.api_key = api_key
            self.base_url = base_url
            self.provider = "openai"
        
        self._llm = None
    
    @property
    def llm(self) -> ChatOpenAI:
        if self._llm is None:
            self._llm = ChatOpenAI(
                model=self.model_name,
                api_key=self.api_key,
                base_url=self.base_url,
                streaming=True
            )
        return self._llm
    
    def _save_token_usage(self, callback: TokenUsageCallback, request_id: str = None):
        if self.user_id is None:
            return
        
        try:
            from ..services.model_gateway import model_gateway
            from ..services.model_gateway import TokenUsage
            
            usage = TokenUsage(
                input_tokens=callback.input_tokens,
                output_tokens=callback.output_tokens,
                total_tokens=callback.input_tokens + callback.output_tokens,
                duration_ms=callback.duration_ms
            )
            
            model_gateway._save_token_usage(
                user_id=self.user_id,
                model_name=self.model_name,
                provider=self.provider,
                usage=usage,
                session_id=self.session_id,
                request_id=request_id
            )
        except Exception as e:
            import logging
            logging.getLogger(__name__).error(f"Failed to save token usage: {e}")
    
    def chat(self, input_text: str, chat_history: List = None) -> Dict[str, Any]:
        messages = []
        if self.system_message:
            messages.append(SystemMessage(content=self.system_message))
        if chat_history:
            for msg in chat_history:
                if isinstance(msg, dict):
                    if msg.get("role") == "user":
                        messages.append(HumanMessage(content=msg.get("content", "")))
                    else:
                        messages.append(HumanMessage(content=msg.get("content", "")))
        messages.append(HumanMessage(content=input_text))
        
        callback = TokenUsageCallback()
        response = self.llm.invoke(messages, config={"callbacks": [callback]})
        
        self._save_token_usage(callback)
        
        return {"output": response.content}
    
    def chat_stream(self, input_text: str, chat_history: List = None):
        messages = []
        if self.system_message:
            messages.append(SystemMessage(content=self.system_message))
        if chat_history:
            for msg in chat_history:
                if isinstance(msg, dict):
                    messages.append(HumanMessage(content=msg.get("content", "")))
        messages.append(HumanMessage(content=input_text))
        
        callback = TokenUsageCallback()
        
        for chunk in self.llm.stream(messages, config={"callbacks": [callback]}):
            yield chunk.content
        
        self._save_token_usage(callback)
