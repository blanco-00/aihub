from langchain_openai import ChatOpenAI
from langchain_anthropic import ChatAnthropic
from typing import Dict, Optional


class ModelGateway:
    PROVIDERS = {
        "openai": ChatOpenAI,
        "anthropic": ChatAnthropic,
    }

    def __init__(self):
        self._models: Dict[str, Dict] = {}

    def register_model(
        self,
        name: str,
        provider: str,
        model_id: str,
        api_key: str,
        base_url: Optional[str] = None,
        **kwargs
    ):
        self._models[name] = {
            "provider": provider,
            "model_id": model_id,
            "api_key": api_key,
            "base_url": base_url,
            "config": kwargs
        }

    def get_model(self, name: str):
        if name not in self._models:
            raise ValueError(f"Model '{name}' not found")
        
        model_info = self._models[name]
        provider_class = self.PROVIDERS.get(model_info["provider"])
        
        if not provider_class:
            raise ValueError(f"Provider '{model_info['provider']}' not supported")
        
        return provider_class(
            model=model_info["model_id"],
            api_key=model_info["api_key"],
            base_url=model_info.get("base_url"),
            **model_info.get("config", {})
        )

    def list_models(self) -> list:
        return [
            {
                "name": name,
                "provider": info["provider"],
                "model_id": info["model_id"]
            }
            for name, info in self._models.items()
        ]


model_gateway = ModelGateway()
