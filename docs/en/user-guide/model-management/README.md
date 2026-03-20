# Model Management

> **Feature Status**: ✅ Available (v1.0+)
> **Last Updated**: 2026-03-20

## Overview

AIHub provides unified management for multiple LLM providers, allowing you to:

- 🔄 Switch between models with one click
- 📊 Compare results from different models
- 💰 Optimize cost and performance
- 🔒 Flexible API key configuration

## Supported Providers

| Provider | Example Models | Features |
|----------|---------------|----------|
| OpenAI | GPT-4, GPT-3.5 | Strong general capability, rich ecosystem |
| Anthropic | Claude 3, Claude 2 | Long context, high safety |
| Zhipu AI | GLM-4, GLM-3 | Strong Chinese capability, cost-effective |
| Alibaba Cloud | Tongyi Qianwen | Chinese optimized, enterprise services |

## Quick Start

### 1. Add a Model

Navigate to "System Management" → "Model Configuration" → Click "Add Model"

### 2. Configure Parameters

- **Model Name**: Display name
- **API Endpoint**: Provider API URL
- **API Key**: Your API key
- **Model ID**: e.g., `gpt-4`, `claude-3-opus`

### 3. Test Connection

Click "Test" button to verify configuration is correct.

## Detailed Documentation

- [Add Model](./add-model.md) - Detailed configuration steps
- [Test Model](./test-model.md) - Connection testing and debugging
- [Model Parameters](./model-parameters.md) - Temperature, MaxTokens, etc.

---

[Back to User Guide](../) · [Having Issues?](../../troubleshooting/)
