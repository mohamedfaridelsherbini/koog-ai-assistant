#!/bin/bash

# Koog AI Assistant - Local Development Setup
# This script starts the local version to avoid DigitalOcean costs

echo "🚀 Starting Koog AI Assistant - Local Version"
echo "=============================================="

# Check if Ollama is running
if ! docker ps | grep -q ollama; then
    echo "🐳 Starting Ollama Docker container..."
    docker run -d --name ollama -p 11434:11434 -v ollama:/root/.ollama ollama/ollama
    echo "⏳ Waiting for Ollama to start..."
    sleep 10
else
    echo "✅ Ollama is already running"
fi

# Check if Ollama is healthy
echo "🔍 Checking Ollama health..."
if curl -s http://localhost:11434/api/tags > /dev/null; then
    echo "✅ Ollama is healthy"
else
    echo "❌ Ollama is not responding. Please check Docker."
    exit 1
fi

# Build the application if needed
if [ ! -f "app/build/libs/app.jar" ]; then
    echo "🔨 Building application..."
    ./gradlew build -x test
fi

# Start the application
echo "🌐 Starting Koog AI Assistant..."
echo "📱 Web interface will be available at: http://localhost:8080"
echo "🔧 Ollama API: http://localhost:11434"
echo ""
echo "Press Ctrl+C to stop the application"
echo "=============================================="

java -jar app/build/libs/app.jar --web
