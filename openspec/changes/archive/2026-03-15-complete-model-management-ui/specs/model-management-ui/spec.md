## ADDED Requirements

### Requirement: Display model list with pagination and filtering

The system SHALL display a paginated list of all configured AI models with search and filter capabilities.

#### Scenario: User views model list
- **WHEN** user navigates to model management page
- **THEN** system displays list of models with pagination (default 10 items per page)
- **AND** list includes model name, vendor, type, status, created/updated time
- **AND** user can search by keyword (model name)
- **AND** user can filter by vendor (OpenAI, Anthropic, Azure, Baidu, Ali, Tencent)
- **AND** user can filter by status (enabled/disabled)

#### Scenario: User searches models by keyword
- **WHEN** user enters "GPT" in search box
- **THEN** system filters models containing "GPT" in name
- **AND** displays filtered results

#### Scenario: User filters models by vendor
- **WHEN** user selects "OpenAI" from vendor dropdown
- **THEN** system displays only OpenAI models
- **AND** clears other filters unless explicitly selected

### Requirement: Create new model configuration

The system SHALL allow users to create new AI model configurations through a form dialog.

#### Scenario: User opens create dialog
- **WHEN** user clicks "Add Model" button
- **THEN** system displays dialog with form fields
- **AND** form includes: model name (required), vendor (required), model type (required), API key (required), endpoint URL (optional), additional parameters (dynamic based on vendor)

#### Scenario: User creates OpenAI model
- **WHEN** user selects "OpenAI" vendor
- **THEN** system shows OpenAI-specific fields
- **AND** user enters model name "GPT-4", API key "sk-xxx", model type "gpt-4"
- **WHEN** user clicks "Save"
- **THEN** system validates all required fields
- **AND** system calls backend API to create model
- **AND** system displays success message
- **AND** system refreshes model list

#### Scenario: User creates model with invalid API key
- **WHEN** user enters invalid or empty API key
- **AND** user clicks "Save"
- **THEN** system displays validation error "API Key is required"
- **AND** does NOT call backend API

### Requirement: Edit existing model configuration

The system SHALL allow users to edit existing AI model configurations.

#### Scenario: User opens edit dialog
- **WHEN** user clicks "Edit" button on a model
- **THEN** system displays dialog with form fields pre-filled
- **AND** all existing configuration data is loaded
- **AND** model name field is disabled (cannot be changed)

#### Scenario: User updates model configuration
- **WHEN** user modifies API key or other fields
- **AND** clicks "Save"
- **THEN** system validates all modified fields
- **AND** system calls backend API to update model
- **AND** system displays success message
- **AND** system refreshes model list

### Requirement: Delete model configuration

The system SHALL allow users to delete model configurations with confirmation.

#### Scenario: User deletes model
- **WHEN** user clicks "Delete" button
- **THEN** system displays confirmation dialog
- **AND** confirmation message includes model name
- **WHEN** user confirms deletion
- **THEN** system calls backend API to delete model
- **AND** system displays success message
- **AND** system refreshes model list
- **AND** deleted model no longer appears in list

#### Scenario: User cancels deletion
- **WHEN** user clicks "Delete" button
- **AND** clicks "Cancel" in confirmation dialog
- **THEN** system does NOT delete model
- **AND** model remains in list

### Requirement: Enable/disable model status

The system SHALL allow users to enable or disable model configurations without deletion.

#### Scenario: User disables model
- **WHEN** user clicks status toggle to disable model
- **THEN** system calls backend API to toggle status
- **AND** system displays disabled indicator for model
- **AND** model is no longer available for use

#### Scenario: User enables model
- **WHEN** user clicks status toggle to enable model
- **THEN** system calls backend API to toggle status
- **AND** system displays enabled indicator for model
- **AND** model is available for use

### Requirement: Display API Key securely

The system SHALL display API keys in masked format to protect sensitive information.

#### Scenario: API key is displayed in list
- **WHEN** user views model list
- **THEN** system displays API keys as "sk-****xxxx" (first 3 characters visible)
- **AND** only masked keys are shown
- **WHEN** user hovers or clicks "Show" button
- **THEN** system temporarily shows full API key
- **AND** system hides full key after 5 seconds or on blur

### Requirement: Handle vendor-specific parameters

The system SHALL display different form fields based on selected vendor type.

#### Scenario: User selects OpenAI vendor
- **WHEN** user selects "OpenAI" from vendor dropdown
- **THEN** system shows OpenAI-specific fields:
  - Model Name (text input)
  - Model Type (select: gpt-3.5-turbo, gpt-4, gpt-4-turbo, gpt-4-32k)
  - API Key (password input with toggle visibility)
  - Base URL (text input, optional, default: https://api.openai.com/v1)

#### Scenario: User selects Anthropic vendor
- **WHEN** user selects "Anthropic" from vendor dropdown
- **THEN** system shows Anthropic-specific fields:
  - Model Name (text input)
  - Model Type (select: claude-3-opus, claude-3-sonnet, claude-2)
  - API Key (password input with toggle visibility)
  - Base URL (text input, optional, default: https://api.anthropic.com)

#### Scenario: User selects Azure vendor
- **WHEN** user selects "Azure" from vendor dropdown
- **THEN** system shows Azure-specific fields:
  - Model Name (text input)
  - Deployment Name (text input, required)
  - API Key (password input with toggle visibility)
  - Endpoint URL (text input, required)
  - API Version (select, required)

#### Scenario: User selects Chinese vendor (Baidu/Ali/Tencent)
- **WHEN** user selects any Chinese vendor
- **THEN** system shows vendor-specific fields based on vendor requirements
- **AND** supports common fields: model name, API key, endpoint URL, model type

### Requirement: Error handling and user feedback

The system SHALL provide clear error messages and user feedback for all operations.

#### Scenario: Backend API returns error
- **WHEN** user creates/edits/deletes model
- **AND** backend API returns error (e.g., invalid API key)
- **THEN** system displays error message
- **AND** message is user-friendly (e.g., "Failed to save: Invalid API key")
- **AND** form remains open for correction

#### Scenario: Network error occurs
- **WHEN** user performs operation
- **AND** network error occurs
- **THEN** system displays error message
- **AND** message includes "Network error, please try again"
- **AND** operation does not complete

#### Scenario: Loading state
- **WHEN** user performs operation (create/edit/delete/search)
- **THEN** system displays loading indicator
- **AND** operation buttons are disabled during loading
- **AND** loading indicator disappears when operation completes
