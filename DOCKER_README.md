# Docker Setup for MCP Vertx Project

This document provides instructions for running the MCP Vertx application using Docker and Docker Compose.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 4GB of available RAM
- 10GB of available disk space

## Quick Start

### 1. Clone and Navigate to Project
```bash
cd /path/to/mcp-vertx
```

### 2. Set Environment Variables
Create a `.env` file in the project root:
```bash
# Required for Gemini AI functionality
GEMINI_API_KEY=your_gemini_api_key_here

# Optional: Override default MongoDB credentials
MONGO_USERNAME=admin
MONGO_PASSWORD=password
MONGO_DATABASE=mcp_vertx
```

### 3. Build and Run with Docker Compose
```bash
# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up --build -d
```

### 4. Access the Application
- **Application**: http://localhost:8080
- **MongoDB**: localhost:27017
- **Health Check**: http://localhost:8080/mcp/sse

## Manual Docker Build

If you prefer to build manually:

```bash
# Build the Docker image
docker build -t mcp-vertx:latest .

# Run the container
docker run -d \
  --name mcp-vertx-app \
  -p 8080:8080 \
  -e GEMINI_API_KEY=your_api_key \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/mcp_vertx \
  -v $(pwd)/tokens:/app/tokens:ro \
  -v $(pwd)/src/resources:/app/resources:ro \
  mcp-vertx:latest
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `GEMINI_API_KEY` | - | Required for Gemini AI functionality |
| `MONGODB_URI` | `mongodb://admin:password@mongodb:27017/mcp_vertx?authSource=admin` | MongoDB connection string |
| `JAVA_OPTS` | `-Xmx2g -Xms512m` | JVM memory settings |

### Volume Mounts

- `./tokens:/app/tokens:ro` - Gmail authentication tokens (read-only)
- `./src/resources:/app/resources:ro` - Application resources like PDFs (read-only)

## Development Setup

### 1. Development Mode
For development with hot reload:

```bash
# Run with volume mount for source code
docker-compose -f docker-compose.dev.yml up --build
```

### 2. Debug Mode
```bash
# Run with debug port exposed
docker run -d \
  --name mcp-vertx-debug \
  -p 8080:8080 \
  -p 5005:5005 \
  -e JAVA_OPTS="-Xmx2g -Xms512m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
  mcp-vertx:latest
```

## Management Commands

### View Logs
```bash
# All services
docker-compose logs

# Specific service
docker-compose logs mcp-vertx-app

# Follow logs
docker-compose logs -f mcp-vertx-app
```

### Stop Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### Restart Services
```bash
# Restart specific service
docker-compose restart mcp-vertx-app

# Rebuild and restart
docker-compose up --build -d
```

### Access Container Shell
```bash
# Access application container
docker exec -it mcp-vertx-app /bin/bash

# Access MongoDB container
docker exec -it mcp-mongodb mongosh
```

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Check what's using port 8080
   lsof -i :8080
   
   # Kill the process or change port in docker-compose.yml
   ```

2. **Memory Issues**
   ```bash
   # Increase memory allocation
   docker run -e JAVA_OPTS="-Xmx4g -Xms1g" ...
   ```

3. **MongoDB Connection Issues**
   ```bash
   # Check MongoDB status
   docker-compose ps mongodb
   
   # View MongoDB logs
   docker-compose logs mongodb
   ```

4. **Permission Issues**
   ```bash
   # Fix file permissions
   sudo chown -R $USER:$USER ./tokens ./src/resources
   ```

### Health Checks

The application includes health checks. Check status:
```bash
docker ps
# Look for "healthy" status in the STATUS column
```

### Performance Monitoring

```bash
# Monitor container resources
docker stats mcp-vertx-app

# Check container logs for errors
docker logs mcp-vertx-app --tail 100
```

## Production Deployment

### 1. Production Docker Compose
Create `docker-compose.prod.yml`:
```yaml
version: '3.8'
services:
  mcp-vertx-app:
    image: mcp-vertx:latest
    restart: always
    environment:
      - NODE_ENV=production
    deploy:
      resources:
        limits:
          memory: 4G
          cpus: '2'
```

### 2. Security Considerations
- Use secrets management for API keys
- Enable TLS/SSL for production
- Use proper firewall rules
- Regular security updates

### 3. Scaling
```bash
# Scale the application
docker-compose up --scale mcp-vertx-app=3
```

## Cleanup

```bash
# Remove all containers and images
docker-compose down --rmi all --volumes --remove-orphans

# Clean up unused Docker resources
docker system prune -a
```

## Support

For issues related to:
- **Docker setup**: Check this README and Docker logs
- **Application issues**: Check application logs and GitHub issues
- **MongoDB issues**: Check MongoDB documentation and logs 