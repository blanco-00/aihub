from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, SystemMessage
from typing import List, Dict, Any, Optional

class AIAgent:
    """Simple AI Agent using LangChain"""
    
    def __init__(
        self,
        model_name: str = "gpt-4",
        api_key: str = "",
        base_url: str = None,
        system_message: str = "You are a helpful AI assistant."
    ):
        self.model_name = model_name
        self.system_message = system_message
        self.api_key = api_key
        self.base_url = base_url
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
        
        response = self.llm.invoke(messages)
        return {"output": response.content}
    
    def chat_stream(self, input_text: str, chat_history: List = None):
        """Streaming chat"""
        messages = []
        if self.system_message:
            messages.append(SystemMessage(content=self.system_message))
        if chat_history:
            for msg in chat_history:
                if isinstance(msg, dict):
                    messages.append(HumanMessage(content=msg.get("content", "")))
        messages.append(HumanMessage(content=input_text))
        
        for chunk in self.llm.stream(messages):
            yield chunk.content
