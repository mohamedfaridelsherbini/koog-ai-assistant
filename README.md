# 🤖 Koog AI Assistant - Local Version

A powerful AI chat system with modern web interface, running locally to avoid cloud costs.

## ✨ Features

- 🤖 **AI Chat** - Powered by multiple AI models (Llama, Gemma, Phi3, DeepSeek)
- 🌐 **Modern Web Interface** - Beautiful 2025 UI/UX with Light/Dark/System themes
- 💬 **Command Line** - Interactive terminal chat
- 🧠 **Memory** - Conversation context awareness
- 📁 **File Operations** - Read, write, list, and save files
- 📥 **Model Management** - Pull, delete, and manage AI models
- 🔄 **Multi-Model Switching** - Switch between models during conversations
- 📊 **Advanced Conversation Features** - Analytics, export/import, and session management
- 🔧 **System Monitoring** - Real-time CPU, memory, disk, and network metrics
- ⚡ **Performance Analytics** - Response times, throughput, error rates, and optimization recommendations
- 🏥 **Health Monitoring** - Comprehensive system health checks with actionable recommendations
- 💰 **Cost-Effective** - Runs locally, no cloud costs

## 🚀 Quick Start (Local Version)

### Prerequisites
- Docker installed and running
- Java 17+ installed

### Easy Run (Recommended)
```bash
./start-local.sh
```

This will:
1. ✅ Check Docker is running
2. 🐳 Start Ollama container if needed
3. 📥 Download AI models if needed
4. 🔨 Build the application if needed
5. 🌐 Start the web interface at http://localhost:8080

### Stop the Application
```bash
./stop-local.sh
```

### Manual Setup

1. **Start Ollama Docker container:**
```bash
docker run -d --name ollama -p 11434:11434 ollama/ollama
```

2. **Download the AI model:**
```bash
docker exec ollama ollama pull llama3.2:3b
```

3. **Build the application:**
```bash
./gradlew :app:jar
```

4. **Run the application:**
```bash
# Web Interface
java -jar app/build/libs/app.jar --web

# Command Line Chat
java -jar app/build/libs/app.jar
```

## 🎯 Usage Modes

### 1. Web Interface
- Open browser to `http://localhost:8080`
- Beautiful chat interface
- File operations sidebar
- Memory management
- Real-time responses

### 2. Command Line Chat
- Interactive terminal interface
- Commands: `help`, `clear`, `memory`, `save`, `read`, `write`, `list`
- Type `exit` to quit

### 3. Demo Mode
- Automated demonstration
- Tests all features
- Shows API endpoints

### 4. Model Management Demo
- Demonstrates model pulling and deletion
- Shows model management API endpoints
- Interactive model testing

### 5. Multi-Model Switching Demo
- Demonstrates switching between models during runtime
- Shows chat responses from different models
- Tests error handling for non-existent models

### 6. Advanced Conversation Demo
- Demonstrates conversation analytics and statistics
- Shows multi-format conversation export (JSON/TXT/CSV)
- Tests session reset and management features

### 7. System Monitoring Demo
- Demonstrates real-time system monitoring capabilities
- Shows performance metrics and health checks
- Tests system optimization recommendations

## 🔧 Available Commands

### Chat Commands
- `help` - Show available commands
- `exit` / `quit` - End the session
- `clear` - Clear conversation memory
- `memory` - Show memory status

### File Operations
- `list [directory]` - List files
- `read <filename>` - Read file content
- `write <filename> <content>` - Write to file
- `save [filename]` - Save conversation

### System Commands
- `models` - List available AI models
- `health` - Check system health
- `current` - Show current model

### Model Management Commands
- `pull <model_name>` - Download a new model
- `delete <model_name>` - Delete a model
- `switch <model_name>` - Switch to a different model

### Advanced Conversation Commands
- `stats` - Show conversation statistics
- `export [format]` - Export conversation (json/txt/csv)
- `analytics` - Show detailed conversation analytics
- `reset` - Reset session and clear all data
- `monitor` - Show system monitoring dashboard
- `performance` - Show performance metrics
- `health-check` - Comprehensive system health check

## 🌐 API Endpoints

When running in web mode, these endpoints are available:

- `POST /api/chat` - Send message to AI
- `GET /api/memory` - Get memory status
- `POST /api/memory/clear` - Clear memory
- `POST /api/files/operation` - File operations
- `GET /api/health` - Health check
- `GET /api/models` - List available models
- `POST /api/models/pull` - Pull a new model
- `POST /api/models/delete` - Delete a model
- `POST /api/models/switch` - Switch to a different model
- `GET /api/models/current` - Get current model
- `GET /api/conversation/stats` - Get conversation statistics
- `POST /api/conversation/export` - Export conversation
- `GET /api/conversation/analytics` - Get conversation analytics
- `POST /api/conversation/reset` - Reset session
- `GET /api/system/metrics` - Get system metrics (CPU, memory, disk, network)
- `GET /api/system/performance` - Get performance metrics (response times, throughput, error rates)
- `GET /api/system/health` - Get comprehensive system health check

## 📁 Project Structure

```
koog-agent-deep-research/
├── 🚀 LOCAL DEVELOPMENT
│   ├── start-local.sh              # Start local application
│   ├── stop-local.sh               # Stop local application
│   └── LOCAL_SETUP.md              # Local development guide
│
├── 📱 APPLICATION
│   ├── app/
│   │   ├── src/main/kotlin/        # Kotlin source code
│   │   ├── src/main/resources/     # Web interface files
│   │   └── build/libs/app.jar      # Compiled application
│   └── gradle/                     # Gradle build system
│
├── 📚 DOCUMENTATION
│   ├── README.md                   # Main documentation
│   ├── NEXT_STEPS.md               # Next steps guide
│   └── PROJECT_OVERVIEW.md         # Project overview
│
└── 📦 ARCHIVE
    ├── archive/digitalocean/       # Archived cloud files
    └── archive/unused/             # Unused files
```

## 🔍 Troubleshooting

### Docker Issues
- Make sure Docker is running: `docker info`
- Check Ollama container: `docker ps | grep ollama`
- Restart container: `docker restart ollama`

### Model Issues
- List models: `docker exec ollama ollama list`
- Pull model: `docker exec ollama ollama pull llama3.2:3b`

### Port Issues
- Check if port 8080 is free: `lsof -i :8080`
- Kill process: `kill -9 <PID>`

### Build Issues
- Clean build: `./gradlew clean :app:jar`
- Check Java version: `java -version`

## 🎨 Customization

### Change AI Model
Edit `app/src/main/kotlin/dev/craftmind/agent/Main.kt`:
```kotlin
model = "your-model-name"
```

### Change Port
Edit `app/src/main/kotlin/dev/craftmind/agent/Main.kt`:
```kotlin
webServer.start(8081) // Change port number
```

### Modify System Prompt
Edit the `systemPrompt` parameter in `Main.kt` to customize AI behavior.

## 📊 Performance

- **Memory**: Keeps last 10 messages for context
- **Timeout**: 5 minutes for complex responses
- **Retries**: 3 attempts with exponential backoff
- **Concurrent**: Single-user system (can be extended)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is open source and available under the MIT License.

---

**Happy chatting with your Docker AI Agent! 🚀**

