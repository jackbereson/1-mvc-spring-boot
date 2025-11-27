# Docker Setup Guide

Complete Docker configuration for the MVC Spring Boot API with PostgreSQL.

## 📋 Prerequisites

- Docker Desktop installed ([Download](https://www.docker.com/products/docker-desktop))
- Docker Compose (included with Docker Desktop)
- 2GB+ available RAM
- macOS, Linux, or Windows (with WSL2)

### Check Installation

```bash
docker --version
docker-compose --version
```

## 🚀 Quick Start

### 1. Setup Environment Variables

```bash
# Copy the example environment file
cp .env.example .env

# Edit .env with your preferred settings (optional)
# Default values work fine for development
```

### 2. Build and Run with Docker Compose

```bash
# Build images and start containers
docker-compose up -d

# View logs
docker-compose logs -f

# Stop when ready (Ctrl+C to stop logs viewing)
```

### 3. Verify Application is Running

```bash
# Check container status
docker-compose ps

# Test API endpoint
curl http://localhost:8080/api/v1/health

# Expected response:
# {
#   "code": 200,
#   "message": "Application is running",
#   "data": { "status": "UP" },
#   "timestamp": "2025-11-27T..."
# }
```

## 📦 Docker Compose Services

### PostgreSQL Database
- **Container Name:** `mvc-postgres`
- **Port:** `5432`
- **Database:** `mvc_db` (default)
- **Username:** `postgres` (default)
- **Password:** `postgres123` (default)
- **Volume:** `postgres_data` (persistent storage)

### Spring Boot Application
- **Container Name:** `mvc-app`
- **Port:** `8080`
- **Depends On:** PostgreSQL (waits for health check)
- **Auto-restart:** Yes (unless stopped)

## 🛠️ Common Docker Commands

### View Logs

```bash
# View all logs
docker-compose logs

# View only app logs
docker-compose logs app

# View only database logs
docker-compose logs postgres

# Follow logs in real-time
docker-compose logs -f app

# Show last 50 lines
docker-compose logs --tail=50
```

### Manage Containers

```bash
# Start services
docker-compose up -d

# Stop services (preserves data)
docker-compose stop

# Start stopped services
docker-compose start

# Restart services
docker-compose restart

# Remove containers (preserves volumes)
docker-compose down

# Remove everything including volumes
docker-compose down -v
```

### Access Services

```bash
# Connect to PostgreSQL
docker exec -it mvc-postgres psql -U postgres -d mvc_db

# Execute command in app container
docker exec mvc-app ls -la /app

# Open shell in app container
docker exec -it mvc-app /bin/bash

# View container details
docker inspect mvc-app
```

### Database Management

```bash
# Connect to database inside container
docker exec -it mvc-postgres psql -U postgres

# Connect to specific database
docker exec -it mvc-postgres psql -U postgres -d mvc_db

# Run SQL command
docker exec mvc-postgres psql -U postgres -d mvc_db -c "SELECT * FROM users;"
```

## 🔧 Configuration Options

### Environment Variables (.env)

```properties
# Database
DB_NAME=mvc_db              # Database name
DB_USER=postgres            # Database user
DB_PASSWORD=postgres123     # Database password

# JWT
JWT_SECRET=your-secret-key  # Must be min 32 characters
JWT_EXPIRATION=86400000     # Token expiration in ms (24 hours)

# Spring Profile
SPRING_PROFILES_ACTIVE=docker
```

### Modify Configuration

Edit `.env` file to change settings:

```bash
# Example: Change database password
nano .env
# Change: DB_PASSWORD=your_new_password

# Restart services to apply changes
docker-compose restart
```

## 🐛 Troubleshooting

### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill process (macOS/Linux)
kill -9 <PID>

# Or change port in docker-compose.yml
# Change: "8080:8080" to "8081:8080"
```

### Database Connection Failed

```bash
# Check if postgres container is running
docker-compose ps

# View postgres logs
docker-compose logs postgres

# Verify database is healthy
docker-compose logs postgres | grep "ready to accept"

# Restart services
docker-compose restart
```

### Application Won't Start

```bash
# View full application logs
docker-compose logs app

# Check if waiting for database
docker-compose logs app | grep -i "database\|connection"

# Rebuild images
docker-compose build --no-cache
docker-compose up -d
```

### Out of Disk Space

```bash
# Clean up unused Docker resources
docker system prune

# Clean up everything including volumes
docker system prune -a --volumes
```

## 📊 Monitoring

### Check System Resources

```bash
# View container resource usage
docker stats

# View detailed container info
docker-compose ps

# Check volume status
docker volume ls

# Inspect specific container
docker inspect mvc-app | grep -A 10 "Resources"
```

### Database Monitoring

```bash
# Connect to database
docker exec -it mvc-postgres psql -U postgres -d mvc_db

# Inside psql, useful commands:
# \dt                    - List tables
# \l                     - List databases
# SELECT * FROM users;   - Query users table
# \q                     - Quit
```

## 🔐 Security Notes

### Production Considerations

1. **Change Default Password**
   ```bash
   # Edit .env
   DB_PASSWORD=strong_random_password_min_16_chars
   ```

2. **Update JWT Secret**
   ```bash
   # Edit .env
   JWT_SECRET=your_very_long_and_random_jwt_secret_min_32_characters
   ```

3. **Restrict Database Access**
   - Don't expose port 5432 in production
   - Use Docker network isolation
   - Implement firewall rules

4. **Use HTTPS**
   - Configure SSL/TLS for API
   - Use reverse proxy (nginx, traefik)

### Never Commit Sensitive Data

```bash
# Ensure .env is in .gitignore
echo ".env" >> .gitignore
git rm --cached .env
git commit -m "Remove .env from git"
```

## 📈 Performance Optimization

### Limit Resources

Edit `docker-compose.yml`:

```yaml
services:
  app:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M
        reservations:
          cpus: '0.5'
          memory: 256M
```

### Database Optimization

```bash
# Connect to database
docker exec -it mvc-postgres psql -U postgres -d mvc_db

# Create indexes for faster queries
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);

# Analyze performance
EXPLAIN ANALYZE SELECT * FROM users WHERE email = 'test@example.com';
```

## 🔄 CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Push Docker Image

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2
      
      - name: Build Docker image
        run: docker build -t mvc-api:latest .
      
      - name: Run tests in Docker
        run: docker-compose -f docker-compose.test.yml up --abort-on-container-exit
```

## 🧹 Cleanup

### Remove All Containers and Volumes

```bash
# Stop all services
docker-compose down

# Remove all data
docker-compose down -v

# Clean up Docker system
docker system prune -a

# Remove image
docker rmi mvc-core:latest
```

### Backup Database

```bash
# Export database dump
docker exec mvc-postgres pg_dump -U postgres mvc_db > mvc_db_backup.sql

# Restore from backup
docker exec -i mvc-postgres psql -U postgres mvc_db < mvc_db_backup.sql
```

## 📚 Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)

## ✅ Checklist

- [ ] Docker Desktop installed and running
- [ ] Created `.env` file from `.env.example`
- [ ] Run `docker-compose up -d`
- [ ] Verify app health: `curl http://localhost:8080/api/v1/health`
- [ ] Check PostgreSQL: `docker exec -it mvc-postgres psql -U postgres -d mvc_db`
- [ ] Application is running and ready to use!

---

**Last Updated:** November 27, 2025
