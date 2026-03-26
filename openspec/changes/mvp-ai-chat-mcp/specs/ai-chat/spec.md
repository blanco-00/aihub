## ADDED Requirements

### Requirement: AI Chat Stream

The system SHALL provide SSE-based streaming chat API that returns AI responses in real-time.

#### Scenario: Successful streaming chat request
- **WHEN** user sends a message via POST `/api/chat/stream`
- **THEN** system returns SSE stream with AI response chunks

#### Scenario: Chat with tool invocation
- **WHEN** user message triggers a tool call
- **THEN** system executes the tool and includes result in response context

#### Scenario: Chat with session context
- **WHEN** user continues an existing session
- **THEN** system includes previous messages in context

### Requirement: AI Chat Non-Stream

The system SHALL provide a non-streaming chat API for simple requests.

#### Scenario: Simple chat request
- **WHEN** user sends a message via POST `/api/chat`
- **THEN** system returns complete AI response in JSON

### Requirement: Session Context Management

The system SHALL manage conversation context per session, supporting message history.

#### Scenario: Load session history
- **WHEN** user requests messages for a session
- **THEN** system returns all messages for that session in chronological order

#### Scenario: Add message to session
- **WHEN** user sends a message in a session
- **THEN** system stores the message with timestamp

### Requirement: Model Selection

The system SHALL support selecting different AI models for chat.

#### Scenario: Chat with specified model
- **WHEN** user specifies a model in chat request
- **THEN** system uses that model for the chat

#### Scenario: Default model fallback
- **WHEN** user does not specify a model
- **THEN** system uses default model configured in settings
