# Prompt Templates

> **Feature Status**: ✅ Available (v1.0+)
> **Last Updated**: 2026-03-20

## Overview

Prompt templates allow you to:

- 📋 Reuse common prompts
- 🎯 Standardize output formats
- 🔧 Use variables for dynamic generation
- 📚 Share templates across teams

## Built-in Templates

| Template Name | Purpose | Example Variables |
|--------------|---------|-------------------|
| General Assistant | Daily conversations | - |
| Code Assistant | Programming questions | `{{language}}` |
| Data Analysis | Data processing | `{{data_type}}` |
| Copywriting | Marketing content | `{{style}}` |
| Customer Service | Support responses | `{{product}}` |
| Translation | Multi-language translation | `{{source_lang}}`, `{{target_lang}}` |

## Quick Usage

1. Go to "Prompt Templates"
2. Select a template
3. Fill in variables (if any)
4. Start chatting

## Create Custom Templates

### 1. Basic Structure

```
You are a {{role}}, specializing in {{domain}}.

User question: {{question}}

Please respond in a {{style}} manner.
```

### 2. Variable Syntax

- `{{variable_name}}` - Required variable
- `[[optional_content]]` - Optional block

## Detailed Documentation

- [Using Templates](./using-templates.md) - Detailed usage guide
- [Creating Templates](./creating-templates.md) - Custom template guide
- [Built-in Templates](./built-in-templates.md) - Complete template list

---

[Back to User Guide](../) · [Having Issues?](../../troubleshooting/)
