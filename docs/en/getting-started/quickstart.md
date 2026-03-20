# Quick Start in 3 Minutes

> **Prerequisites**: Docker 20.10+, Docker Compose 2.0+

## Step 1: Launch Services (2 min)

```bash
# Clone the project
git clone https://github.com/aihub/aihub.git
cd aihub

# One-click start
cd docker && docker compose up -d

# Check status
docker compose ps
```

## Step 2: Access the System (30 sec)

Open your browser and visit:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080

**Default Credentials**:
- Username: `admin`
- Password: `admin123`

## Step 3: Start Chatting (30 sec)

1. Log in to the system
2. Click "AI Chat" in the left menu
3. Select a model (e.g., GPT-3.5)
4. Type a message and start chatting!

## 🎉 Congratulations!

You've successfully created your first AI conversation. Here's what to do next:

- 📖 [Add more models](../user-guide/model-management/add-model.md)
- 🎨 [Use prompt templates](../user-guide/prompt-templates/using-templates.md)
- 📚 [View more examples](../examples/)

---

## Having Issues?

- [Common Issues](../troubleshooting/common-issues.md)
- [Error Messages](../troubleshooting/error-messages.md)
- [GitHub Issues](https://github.com/aihub/aihub/issues)

---

## Next Steps

| Want to... | Recommended Reading |
|-----------|---------------------|
| Add custom models | [Model Management Guide](../user-guide/model-management/) |
| Create prompt templates | [Prompt Template Guide](../user-guide/prompt-templates/) |
| Deploy to production | [Deployment Guide](../user-guide/deployment/) |
| Understand architecture | [Architecture Docs](../architecture/system-architecture.md) |
| Contribute code | [Contributing Guide](../developer/contributing.md) |
