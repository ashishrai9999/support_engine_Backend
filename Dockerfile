# Multi-stage Dockerfile for MCP Vertx Project
# Stage 1: Build stage
FROM gradle:8.5-jdk17 AS build

# Set working directory
WORKDIR /app

# Copy gradle files first for better caching
COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (this layer will be cached if dependencies don't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew build --no-daemon

# Stage 2: Runtime stage
FROM openjdk:17-jre-slim

# Set working directory
WORKDIR /app

# Install necessary packages for the application
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Copy resources directory
COPY --from=build /app/src/resources/ /app/resources/

# Create tokens directory for Gmail credentials
RUN mkdir -p /app/tokens && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose the port the app runs on
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS="-Xmx2g -Xms512m"
ENV GEMINI_API_KEY=""
ENV MONGODB_URI="mongodb://localhost:27017"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/mcp/sse || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 