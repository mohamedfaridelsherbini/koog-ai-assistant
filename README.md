# 🤖 Koog AI Assistant

A modern, production-ready AI chat application built with clean architecture, featuring comprehensive model management and a beautiful web interface.

## ✨ Features

- 🤖 **AI Chat** - Powered by Ollama with multiple model support
- 🌐 **Modern Web Interface** - Beautiful dark theme with purple accents
- 📦 **Model Management** - Download, delete, switch, and manage AI models
- 🎨 **Syntax Highlighting** - Code blocks with Prism.js support
- 🧠 **Conversation Memory** - Context-aware conversations
- 🏗️ **Clean Architecture** - Domain-driven design with proper separation of concerns
- 📱 **Responsive Design** - Works on desktop and mobile devices
- 🔧 **Easy Setup** - One-command launcher with Docker integration

## 🚀 Quick Start

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
3. 📥 Download AI model if needed
4. 🔨 Build the application
5. 🌐 Start the web server on `http://localhost:8080`

### Manual Setup

1. **Start Ollama Docker container:**
```bash
docker run -d --name ollama -p 11434:11434 ollama/ollama
```

2. **Download the AI model:**
```bash
docker exec ollama ollama pull llama3.1:8b
```

3. **Build the application:**
```bash
./gradlew build
```

4. **Run the application:**
```bash
java -jar app/build/libs/app.jar
```

## 🎯 Usage

### Web Interface
- Open browser to `http://localhost:8080`
- Modern chat interface with dark theme
- Model management modal
- Syntax highlighting for code blocks
- Real-time responses with proper formatting

### Model Management
- Click "📦 Models" button to open model management
- Download new models from the available list
- Switch between downloaded models
- Delete unused models to free up space

## 🏗️ Architecture

The application follows clean architecture principles with clear separation of concerns:

```
app/src/main/kotlin/dev/craftmind/agent/
├── application/           # Application layer
│   ├── dto/              # Data Transfer Objects
│   └── service/          # Application services
├── config/               # Configuration and dependency injection
├── domain/               # Domain layer
│   ├── model/            # Domain models
│   ├── repository/       # Repository interfaces
│   └── service/          # Domain services
├── infrastructure/       # Infrastructure layer
│   ├── ollama/           # Ollama API client
│   └── repository/       # Repository implementations
├── presentation/         # Presentation layer
│   └── web/              # Web interface
│       ├── handler/      # HTTP handlers
│       └── WebServer.kt  # Web server setup
└── Main.kt               # Application entry point
```

## 🌐 API Endpoints

- `POST /api/chat` - Send message to AI
- `GET /api/models` - List available models
- `GET /api/models/all` - Get detailed model information
- `POST /api/models/pull` - Download a new model
- `POST /api/models/delete` - Delete a model
- `POST /api/models/switch` - Switch to a different model

## 🎨 Frontend Architecture

The frontend is built with modular JavaScript and CSS:

```
app/src/main/resources/static/
├── css/
│   ├── base.css          # Base styles and variables
│   ├── components.css    # Component styles
│   ├── layout.css        # Layout styles
│   ├── syntax.css        # Syntax highlighting
│   └── themes.css        # Theme styles
├── js/
│   ├── app.js            # Main application logic
│   ├── chat.js           # Chat functionality
│   ├── config.js         # Configuration
│   ├── models.js         # Model management
│   └── theme.js          # Theme switching
└── index.html            # Main HTML file
```

## 🔧 Configuration

### Change AI Model
Edit `app/src/main/kotlin/dev/craftmind/agent/config/ApplicationConfig.kt`:
```kotlin
ollama = OllamaConfig(
    defaultModel = "your-model-name"
)
```

### Change Port
Edit `app/src/main/kotlin/dev/craftmind/agent/Main.kt`:
```kotlin
val config = ApplicationConfig(
    server = ServerConfig(port = 8081), // Change port number
    ollama = OllamaConfig()
)
```

### Modify System Prompt
Edit the `systemPrompt` parameter in `ConversationService.kt`:
```kotlin
private val systemPrompt = "Your custom system prompt here"
```

## 🔍 Troubleshooting

### Docker Issues
- Make sure Docker is running: `docker info`
- Check Ollama container: `docker ps | grep ollama`
- Restart container: `docker restart ollama`

### Model Issues
- List models: `docker exec ollama ollama list`
- Pull model: `docker exec ollama ollama pull llama3.1:8b`

### Port Issues
- Check if port 8080 is free: `lsof -i :8080`
- Kill process: `pkill -f "java.*app.jar"`

### Build Issues
- Clean build: `./gradlew clean build`
- Check Java version: `java -version`

## 📊 Performance

- **Memory**: Keeps conversation history for context
- **Timeout**: 5 minutes for complex responses
- **Concurrent**: Single-user system (can be extended)
- **Caching**: In-memory conversation storage

## 🎨 Themes

The application supports both light and dark themes:

- **Dark Theme**: Default with purple accents (`#33005d`)
- **Light Theme**: Light gray background with subtle styling
- **Toggle**: Click the moon icon in the header to switch themes

## 🚀 Development

### Building
```bash
./gradlew build
```

### Running Tests
```bash
./gradlew test
```

### Clean Build
```bash
./gradlew clean build
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Test thoroughly
5. Commit your changes: `git commit -m 'Add amazing feature'`
6. Push to the branch: `git push origin feature/amazing-feature`
7. Submit a pull request

## 📄 License

This project is open source and available under the MIT License.

---

**Happy chatting with Koog AI! 🚀**