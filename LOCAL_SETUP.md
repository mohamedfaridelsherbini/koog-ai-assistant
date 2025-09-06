# 🏠 Koog AI Assistant - Local Development Guide

## 🎯 **Quick Start**

### 1. Start the Application
```bash
./start-local.sh
```

### 2. Access the Web Interface
Open your browser to: **http://localhost:8080**

### 3. Stop the Application
```bash
./stop-local.sh
```

## 🔧 **What's Running**

### Services
- **Koog AI Assistant**: Java application on port 8080
- **Ollama**: Docker container on port 11434
- **6 AI Models**: Ready to use

### Available Models
- `deepseek-coder:6.7b` (7B parameters) - Best for coding
- `gemma:2b` (3B parameters) - Fast and efficient
- `llama3.2:1b` (1.2B parameters) - Lightweight
- `phi3:mini` (3.8B parameters) - Microsoft's model
- `llama3.1:8b` (8B parameters) - High quality
- `llama3.2:3b` (3.2B parameters) - Balanced

## 🌐 **Web Interface Features**

### Modern UI/UX (2025 Design)
- **Light/Dark/System** theme support
- **Responsive design** for all devices
- **Smooth animations** and transitions
- **Glass-morphism** effects

### Chat Features
- **Real-time AI responses**
- **Model switching** during conversations
- **Conversation memory** (last 10 messages)
- **Export conversations** (JSON/TXT/CSV)

### File Operations
- **Read files** from your system
- **Write files** with AI assistance
- **List directories** and files
- **Save conversations** to files

### Model Management
- **Download new models** from Ollama
- **Delete unused models** to save space
- **Switch between models** instantly
- **View model details** and sizes

## 🚀 **Development Commands**

### Build the Application
```bash
./gradlew build -x test
```

### Run Tests
```bash
./gradlew test
```

### Clean Build
```bash
./gradlew clean build
```

### Check Application Status
```bash
curl http://localhost:8080/health
```

## 🔍 **Troubleshooting**

### Port Already in Use
```bash
# Stop any running instances
./stop-local.sh

# Or kill manually
pkill -f "java -jar app/build/libs/app.jar"

# Then restart
./start-local.sh
```

### Ollama Issues
```bash
# Check if Ollama is running
docker ps | grep ollama

# Restart Ollama
docker restart ollama

# Check Ollama logs
docker logs ollama
```

### Memory Issues
```bash
# Check Java memory usage
ps aux | grep java

# Increase memory if needed (edit start-local.sh)
java -Xmx2g -jar app/build/libs/app.jar --web
```

## 📊 **Performance Tips**

### For Better Performance
1. **Use smaller models** for faster responses (llama3.2:1b, gemma:2b)
2. **Close unused applications** to free up memory
3. **Use SSD storage** for better I/O performance
4. **Ensure adequate RAM** (8GB+ recommended)

### For Better Quality
1. **Use larger models** for complex tasks (llama3.1:8b, deepseek-coder:6.7b)
2. **Provide clear context** in your messages
3. **Use specific prompts** for better results

## 🎨 **Customization**

### Change Default Model
Edit `app/src/main/kotlin/dev/craftmind/agent/Main.kt`:
```kotlin
model = "your-preferred-model"
```

### Change Port
Edit `start-local.sh`:
```bash
java -jar app/build/libs/app.jar --web --port 8081
```

### Add New Models
```bash
# Download a new model
docker exec ollama ollama pull model-name

# The model will appear in the web interface
```

## 💡 **Pro Tips**

1. **Start with gemma:2b** - Good balance of speed and quality
2. **Use deepseek-coder:6.7b** for coding tasks
3. **Switch models** based on your current task
4. **Export conversations** for future reference
5. **Use file operations** to work with documents

## 🆘 **Need Help?**

### Check Logs
```bash
# Application logs are shown in the terminal
# Look for error messages or warnings
```

### Restart Everything
```bash
./stop-local.sh
docker stop ollama
docker rm ollama
./start-local.sh
```

### Reset to Default
```bash
# Clean everything
./gradlew clean
docker system prune -f
./start-local.sh
```

---

**Happy coding with your local AI assistant! 🚀**
