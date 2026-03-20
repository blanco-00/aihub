# Deployment Guide

> **Feature Status**: ✅ Available (v1.0+)
> **Last Updated**: 2026-03-20

## Overview

AIHub supports multiple deployment methods:

- 🐳 Docker Compose (Recommended)
- ☸️ Kubernetes
- 📦 Manual Deployment

## Docker Compose (Recommended)

### Quick Start

```bash
# Clone the project
git clone https://github.com/aihub/aihub.git
cd aihub

# Start all services
cd docker && docker compose up -d

# Check status
docker compose ps
```

### Configuration

Edit `docker/.env` to customize:

```env
# Database
MYSQL_ROOT_PASSWORD=your_password
MYSQL_DATABASE=aihub

# Redis
REDIS_PASSWORD=your_redis_password

# Application
SPRING_PROFILES_ACTIVE=prod
```

### Production Checklist

- [ ] Change default passwords
- [ ] Configure SSL/HTTPS
- [ ] Set up backup strategy
- [ ] Configure log rotation
- [ ] Enable monitoring

## Kubernetes

Coming soon...

## Manual Deployment

### Prerequisites

- Node.js 20+
- Java 17+
- MySQL 8.0+
- Redis 7.0+

### Steps

1. **Build Frontend**
```bash
cd frontend
pnpm install
pnpm build
```

2. **Build Backend**
```bash
cd backend
mvn clean package -DskipTests
```

3. **Configure Database**
```bash
mysql -u root -p < docs/sql/init/schema.sql
```

4. **Start Services**
```bash
java -jar aihub-api.jar --spring.profiles.active=prod
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Backend port | 8080 |
| `MYSQL_HOST` | MySQL host | localhost |
| `MYSQL_PORT` | MySQL port | 3306 |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |

---

[Back to User Guide](../) · [Having Issues?](../../troubleshooting/)
