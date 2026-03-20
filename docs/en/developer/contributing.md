# Contributing Guide

Thank you for considering contributing to AIHub!

## 🤔 How to Contribute

### Report Issues

1. Search [Existing Issues](https://github.com/aihub/aihub/issues) to avoid duplicates
2. Use the issue template to submit
3. Provide detailed reproduction steps

### Submit Code

1. Fork the project
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push branch: `git push origin feature/amazing-feature`
5. Submit a Pull Request

### Code Standards

**Frontend (TypeScript/Vue)**
- 2-space indentation
- Double quotes
- Trailing semicolons

**Backend (Java)**
- Follow Google Java Style
- Method length < 50 lines
- Meaningful naming

### Development Environment

```bash
# Frontend
cd frontend && pnpm install && pnpm dev

# Backend
cd backend/aihub-api && mvn spring-boot:run

# AI Service
cd aihub-agent && pip install -r requirements.txt && uvicorn main:app
```

## 📖 Documentation Contributions

- Fix typos
- Improve documentation content
- Add code examples

## 🙏 Thanks

Every contributor is an important part of AIHub!

---

[Back to Developer Docs](./) · [Back to Home](../../)
