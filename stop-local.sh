#!/bin/bash

# Koog AI Assistant - Stop Local Services
echo "🛑 Stopping Koog AI Assistant - Local Version"
echo "============================================="

# Stop Java application
echo "🔄 Stopping Koog AI application..."
pkill -f "java -jar app/build/libs/app.jar"

# Optionally stop Ollama (uncomment if you want to stop it)
# echo "🔄 Stopping Ollama..."
# docker stop ollama
# docker rm ollama

echo "✅ Local services stopped"
echo "💡 Ollama is still running in Docker (use 'docker stop ollama' to stop it)"
