## ADDED Requirements

### Requirement: Model Registration

The system SHALL allow registration of AI models with provider, model_id, API key, and base URL.

#### Scenario: Register a new model
- **WHEN** admin registers a model with vendor, model_id, api_key, base_url
- **THEN** system stores model configuration and makes it available

### Requirement: Model Chat

The system SHALL provide a unified chat interface that routes to the appropriate model provider.

#### Scenario: Chat with OpenAI model
- **WHEN** user sends chat request to an OpenAI model
- **THEN** system routes to OpenAI provider and returns response

#### Scenario: Chat with Anthropic model
- **WHEN** user sends chat request to an Anthropic model
- **THEN** system routes to Anthropic provider and returns response

#### Scenario: Chat with ZhipuAI model
- **WHEN** user sends chat request to a ZhipuAI model
- **THEN** system routes to ZhipuAI provider and returns response

### Requirement: Streaming Chat

The system SHALL support streaming responses from models that support it.

#### Scenario: Streaming chat
- **WHEN** user requests streaming chat
- **THEN** system returns response as SSE stream

### Requirement: Model Health Check

The system SHALL provide health check for models to verify connectivity.

#### Scenario: Check model health
- **WHEN** user calls health check for a model
- **THEN** system attempts to reach the model API and returns status

### Requirement: Token Statistics

The system SHALL track token usage for each chat request.

#### Scenario: Record token usage
- **WHEN** chat request completes
- **THEN** system records input_tokens and output_tokens

#### Scenario: Get token statistics
- **WHEN** admin requests token stats for a date range
- **THEN** system returns aggregated token usage
