# 🚀 Koog AI Assistant v1.0.0 - Quick Start Guide

## **Welcome to Koog AI Assistant!**

A sophisticated AI chat system with Docker-based Ollama LLM integration and modern web interface.

## **⚡ Quick Start (5 Minutes)**

### **Prerequisites**
- **Docker** (for Ollama)
- **Java 17+** (for the application)
- **Git** (for cloning)

### **1. Clone & Setup**
```bash
# Clone the repository
git clone https://github.com/mohamedfaridelsherbini/koog-ai-assistant.git
cd koog-ai-assistant

# Checkout the latest release
git checkout v1.0.0
```

### **2. Start Ollama (Docker)**
```bash
# Start Ollama in Docker
docker run -d -p 11434:11434 --name ollama ollama/ollama

# Download a model (optional - you can do this via the web interface)
docker exec ollama ollama pull llama3.2:1b
```

### **3. Build & Run**
```bash
# Build the application
./gradlew build

# Run the application
./run.sh
```

### **4. Access the Interface**
Open your browser and go to: **http://localhost:8080**

## **🎯 What You'll See**

### **Modern Web Interface**
- **Clean Design**: 2025 UI/UX with Light/Dark/System themes
- **Model Management**: Download, switch, and manage AI models
- **Real-time Chat**: Interactive conversation with AI
- **Active LLM Indicator**: Shows current model and timestamp

### **Available Models**
The interface shows 26+ available models including:
- **llama3.2:1b** - Fast, lightweight (1.2B parameters)
- **llama3.1:8b** - Balanced performance (8B parameters)
- **phi3:mini** - Microsoft's efficient model (3.8B parameters)
- **gemma:2b** - Google's compact model (3B parameters)
- **deepseek-coder:6.7b** - Code-focused model (7B parameters)

## **🛠 Advanced Usage**

### **Docker Compose (Recommended)**
```bash
# Start complete stack (Ollama + Koog AI)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop everything
docker-compose down
```

### **Production Deployment**
```bash
# Deploy to production
./deploy.sh production deploy

# Check status
./deploy.sh production status

# View logs
./deploy.sh production logs
```

### **Environment Variables**
```bash
# Customize settings
export OLLAMA_HOST=localhost:11434
export WEB_PORT=8080
export MODEL_CACHE_TTL=30

# Run with custom settings
java -jar app/build/libs/app.jar --web
```

## **📱 Web Interface Features**

### **Theme System**
- **Light Mode**: Clean, bright interface
- **Dark Mode**: Easy on the eyes
- **System Mode**: Automatically follows OS preference

### **Model Management**
- **Download Models**: Click the download button on any model
- **Switch Models**: Click on model names to switch
- **Delete Models**: Use the trash icon to remove models
- **View Details**: See model sizes, parameters, and quantization

### **Chat Features**
- **Context Memory**: Remembers conversation history
- **Model Switching**: Change models mid-conversation
- **Real-time Responses**: Fast, streaming responses
- **Active LLM Indicator**: Shows current model and time

## **🔧 Troubleshooting**

### **Common Issues**

#### **"Ollama not responding"**
```bash
# Check if Ollama is running
docker ps | grep ollama

# Restart Ollama
docker restart ollama

# Check Ollama logs
docker logs ollama
```

#### **"Port 8080 already in use"**
```bash
# Find what's using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or use a different port
export WEB_PORT=8081
./run.sh
```

#### **"Models not loading"**
```bash
# Check Ollama API
curl http://localhost:11434/api/tags

# Restart the application
./run.sh
```

### **Performance Tips**

#### **For Better Performance**
- Use smaller models (1B-3B parameters) for faster responses
- Ensure adequate RAM (8GB+ recommended for larger models)
- Use SSD storage for better I/O performance

#### **For Development**
```bash
# Enable debug logging
export DEBUG=true
./run.sh

# Run in development mode
./deploy.sh development start
```

## **📊 Monitoring**

### **Health Checks**
```bash
# Check application health
curl http://localhost:8080/health

# Check Ollama health
curl http://localhost:11434/api/tags
```

### **Logs**
```bash
# View application logs
tail -f logs/koog-ai.log

# View Docker logs
docker logs koog-ai-assistant
```

## **🚀 Next Steps**

### **Explore Features**
1. **Download Models**: Try different AI models
2. **Customize Themes**: Switch between Light/Dark modes
3. **File Operations**: Use the file management features
4. **Model Switching**: Change models during conversations

### **Advanced Configuration**
1. **Custom Models**: Add your own Ollama models
2. **API Integration**: Use the REST API endpoints
3. **Docker Deployment**: Deploy with Docker Compose
4. **Production Setup**: Configure for production use

### **Get Help**
- **GitHub Issues**: [Report bugs or request features](https://github.com/mohamedfaridelsherbini/koog-ai-assistant/issues)
- **Documentation**: Check the full [README.md](README.md)
- **Release Notes**: See [RELEASE_NOTES_v1.0.0.md](RELEASE_NOTES_v1.0.0.md)

## **🎉 You're Ready!**

Your Koog AI Assistant is now running! Start chatting with AI models and explore the modern interface.

**Happy AI Chatting! 🤖✨**
