from langchain.agents import AgentExecutor, create_openai_functions_agent
from langchain_openai import ChatOpenAI
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain.tools import Tool
from typing import List, Dict, Any


class AIAgent:
    def __init__(
        self,
        model_name: str = "gpt-4",
        api_key: str = "",
        base_url: str = None,
        tools: List[Tool] = None,
        system_message: str = "You are a helpful AI assistant."
    ):
        self.model_name = model_name
        self.llm = ChatOpenAI(
            model=model_name,
            api_key=api_key,
            base_url=base_url
        )
        self.tools = tools or []
        self.system_message = system_message
        self.agent = None
        self.agent_executor = None
        self._build_agent()

    def _build_agent(self):
        prompt = ChatPromptTemplate.from_messages([
            ("system", self.system_message),
            MessagesPlaceholder(variable_name="chat_history", optional=True),
            ("human", "{input}"),
            MessagesPlaceholder(variable_name="agent_scratchpad")
        ])
        self.agent = create_openai_functions_agent(
            llm=self.llm,
            tools=self.tools,
            prompt=prompt
        )
        self.agent_executor = AgentExecutor(
            agent=self.agent,
            tools=self.tools,
            verbose=True
        )

    def chat(self, input_text: str, chat_history: List = None) -> Dict[str, Any]:
        result = self.agent_executor.invoke({
            "input": input_text,
            "chat_history": chat_history or []
        })
        return result

    def add_tool(self, tool: Tool):
        self.tools.append(tool)
        self._build_agent()
