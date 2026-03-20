import pytest
from unittest.mock import MagicMock, patch


class TestModelGateway:
    def test_register_model(self):
        from aihub.services.model_gateway import ModelGateway
        
        gateway = ModelGateway()
        gateway.register_model(
            name="gpt-4",
            provider="openai",
            model_id="gpt-4",
            api_key="test-key"
        )
        
        assert "gpt-4" in gateway._models
        assert gateway._models["gpt-4"]["provider"] == "openai"

    def test_register_model_with_base_url(self):
        from aihub.services.model_gateway import ModelGateway
        
        gateway = ModelGateway()
        gateway.register_model(
            name="custom-model",
            provider="openai",
            model_id="custom",
            api_key="key",
            base_url="https://custom.api.com"
        )
        
        assert gateway._models["custom-model"]["base_url"] == "https://custom.api.com"

    def test_list_models(self):
        from aihub.services.model_gateway import ModelGateway
        
        gateway = ModelGateway()
        gateway.register_model("model1", "openai", "gpt-4", "key1")
        gateway.register_model("model2", "anthropic", "claude-3", "key2")
        
        models = gateway.list_models()
        assert len(models) == 2
        model_names = [m["name"] for m in models]
        assert "model1" in model_names
        assert "model2" in model_names

    def test_get_model_not_found(self):
        from aihub.services.model_gateway import ModelGateway
        
        gateway = ModelGateway()
        with pytest.raises(ValueError, match="Model 'nonexistent' not found"):
            gateway.get_model("nonexistent")

    def test_get_model_unsupported_provider(self):
        from aihub.services.model_gateway import ModelGateway
        
        gateway = ModelGateway()
        gateway.register_model("test", "unsupported", "model", "key")
        
        with pytest.raises(ValueError, match="Provider 'unsupported' not supported"):
            gateway.get_model("test")

    def test_get_model(self):
        from aihub.services.model_gateway import ModelGateway
        
        gateway = ModelGateway()
        gateway.register_model("gpt-4", "openai", "gpt-4", "test-key")
        
        model = gateway.get_model("gpt-4")
        
        # Verify model is returned (actual ChatOpenAI instance)
        assert model is not None
