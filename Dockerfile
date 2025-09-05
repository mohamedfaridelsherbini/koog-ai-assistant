# Koog AI Assistant v1.0.0 - Production Docker Image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Install curl for Ollama fallback
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy application files
COPY app/build/libs/app.jar /app/app.jar
COPY run.sh /app/run.sh

# Make run script executable
RUN chmod +x /app/run.sh

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Set environment variables
ENV OLLAMA_HOST=host.docker.internal:11434
ENV WEB_PORT=8080
ENV MODEL_CACHE_TTL=30

# Run the application
CMD ["java", "-jar", "app.jar", "--web"]
