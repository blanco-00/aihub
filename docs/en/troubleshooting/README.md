# Troubleshooting

## Common Issues

### Service Startup Failure

**Symptoms**: `docker compose up -d` fails

**Solutions**:
```bash
# Check port conflicts
lsof -i :3000
lsof -i :8080

# Check Docker status
docker info

# View logs
docker compose logs
```

### Model Connection Failure

**Symptoms**: Error "Model connection failed" during chat

**Troubleshooting Steps**:
1. Check if API Key is correct
2. Check if API endpoint is accessible
3. Check network proxy settings
4. View backend logs: `docker compose logs aihub-api`

### Login Failure

**Symptoms**: "Invalid username or password" message

**Solutions**:
1. Confirm default credentials: `admin` / `admin123`
2. Check if database is running properly
3. Reset password: Check backend logs for reset command

### Streaming Response Not Working

**Symptoms**: No typewriter effect during chat

**Troubleshooting**:
1. Check if browser supports SSE
2. Check Nginx configuration (if used)
3. Verify backend streaming endpoint is working

## Error Codes

| Error Code | Meaning | Solution |
|-----------|---------|----------|
| E001 | Database connection failed | Check MySQL status |
| E002 | Redis connection failed | Check Redis status |
| E003 | Model API call failed | Check API Key and network |
| E004 | Token limit exceeded | Check account balance |

## Getting Help

- 📖 [GitHub Discussions](https://github.com/aihub/aihub/discussions)
- 🐛 [Submit an Issue](https://github.com/aihub/aihub/issues)
- 💬 Join our community chat

---

[Back to Documentation](../)
