#!/bin/bash

echo "🚀 Docker AI Agent - Easy Launcher"
echo "=================================="
echo ""

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if Ollama container is running
if ! docker ps | grep -q ollama; then
    echo "🐳 Starting Ollama Docker container..."
    docker run -d --name ollama -p 11434:11434 ollama/ollama
    echo "⏳ Waiting for Ollama to start..."
    sleep 10
else
    echo "✅ Ollama container is already running"
fi

# Check if the model is available
echo "🔍 Checking for llama3.2:3b model..."
if ! docker exec ollama ollama list | grep -q "llama3.2:3b"; then
    echo "📥 Pulling llama3.2:3b model (this may take a few minutes)..."
    docker exec ollama ollama pull llama3.2:3b
else
    echo "✅ Model llama3.2:3b is available"
fi

# Build the application if needed
if [ ! -f "app/build/libs/app.jar" ]; then
    echo "🔨 Building application..."
    ./gradlew --no-daemon :app:jar
fi

echo ""
echo "🎯 Choose your mode:"
echo "1) Web Interface (Browser)"
echo "2) Command Line Chat"
echo "3) Demo Mode"
echo "4) Model Management Demo"
echo "5) Multi-Model Switching Demo"
echo "6) Advanced Conversation Demo"
echo "7) System Monitoring Demo"
echo ""
read -p "Enter your choice (1-7): " choice

case $choice in
    1)
        echo "🌐 Starting Web Interface..."
        echo "📱 Open your browser and go to: http://localhost:8080"
        echo "🛑 Press Ctrl+C to stop"
        java -jar app/build/libs/app.jar --web
        ;;
    2)
        echo "💬 Starting Command Line Chat..."
        echo "📝 Type 'help' for available commands"
        echo "🛑 Type 'exit' to quit"
        java -jar app/build/libs/app.jar
        ;;
            3)
            echo "🎬 Starting Demo Mode..."
            ./demo_web.sh
            ;;
        4)
            echo "🤖 Starting Model Management Demo..."
            ./demo_model_management.sh
            ;;
        5)
            echo "🔄 Starting Multi-Model Switching Demo..."
            ./demo_multi_model.sh
            ;;
        6)
            echo "📊 Starting Advanced Conversation Demo..."
            ./demo_advanced_conversation.sh
            ;;
        7)
            echo "🔧 Starting System Monitoring Demo..."
            ./demo_system_monitoring.sh
            ;;
        *)
            echo "❌ Invalid choice. Exiting."
            exit 1
            ;;
    esac

