# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Git workflow documentation
- Automated Git helper script
- Comprehensive changelog tracking

## [1.0.0] - 2024-12-19

### Added
- Initial release of Koog AI Assistant
- Ollama integration for local AI model execution
- Web-based chat interface
- Model management system (list, pull, delete, switch models)
- RESTful API endpoints for chat and model operations
- Clean architecture with separation of concerns
- Comprehensive unit test coverage (100% success rate)
- Docker support for Ollama deployment
- Static file serving for web interface
- Conversation memory management
- Input validation and sanitization
- Error handling and logging
- Configuration management
- Dependency injection container

### Features
- **Chat Interface**: Real-time chat with AI models
- **Model Management**: Download, switch, and manage Ollama models
- **Web UI**: Modern, responsive web interface
- **API**: RESTful API for programmatic access
- **Memory**: Conversation history management
- **Security**: Input validation and sanitization
- **Testing**: Comprehensive unit test suite
- **Documentation**: Complete API and setup documentation

### Technical Details
- **Language**: Kotlin 2.2.0
- **Runtime**: Java 17
- **Build Tool**: Gradle
- **AI Framework**: Koog Agents 0.4.1
- **HTTP Client**: Java HttpClient
- **Web Server**: Built-in HTTP server
- **Testing**: JUnit 5, Mockito, MockK

### API Endpoints
- `POST /api/chat` - Send message to AI
- `GET /api/models` - List available models
- `GET /api/models/all` - Get detailed model information
- `POST /api/models/pull` - Download a new model
- `POST /api/models/delete` - Delete a model
- `POST /api/models/switch` - Switch to a different model

### Supported Models
- deepseek-coder:6.7b
- gemma:2b
- llama3.2:1b
- phi3:mini
- llama3.1:8b
- llama3.2:3b

### Installation
1. Clone the repository
2. Ensure Java 17+ is installed
3. Start Ollama Docker container
4. Run `./gradlew build`
5. Execute `java -jar app/build/libs/app.jar --web`
6. Access web interface at http://localhost:8080

---

## Release Notes

### Version 1.0.0
This is the initial release of the Koog AI Assistant, providing a complete solution for local AI model management and chat functionality. The application integrates seamlessly with Ollama for running various open-source language models locally.

**Key Highlights:**
- ✅ 100% unit test coverage
- ✅ Clean architecture implementation
- ✅ Comprehensive error handling
- ✅ Modern web interface
- ✅ RESTful API design
- ✅ Docker integration
- ✅ Security best practices

**Getting Started:**
1. Follow the installation instructions in README.md
2. Use the web interface at http://localhost:8080
3. Or integrate via the REST API
4. Refer to GIT_WORKFLOW.md for development guidelines

---

## Development Workflow

### Branch Strategy
- `main` - Production releases only
- `develop` - Integration branch for ongoing development
- `feature/*` - New features and enhancements
- `bugfix/*` - Bug fixes
- `hotfix/*` - Critical production fixes

### Release Process
1. Develop features in `feature/*` branches
2. Merge to `develop` when ready
3. Create `release/*` branch for final preparation
4. Merge to `main` and tag release
5. Merge back to `develop`

### Versioning
Following Semantic Versioning (SemVer):
- **MAJOR**: Breaking changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

---

## Contributing

Please refer to GIT_WORKFLOW.md for detailed contribution guidelines.

### Commit Convention
- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation changes
- `style:` - Code style changes
- `refactor:` - Code refactoring
- `test:` - Adding or updating tests
- `chore:` - Maintenance tasks

---

## Support

For issues, questions, or contributions, please refer to the project documentation or create an issue in the repository.
