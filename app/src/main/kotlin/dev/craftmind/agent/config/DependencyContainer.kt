package dev.craftmind.agent.config

import dev.craftmind.agent.application.service.ChatApplicationService
import dev.craftmind.agent.domain.service.ConversationService
import dev.craftmind.agent.infrastructure.ollama.OllamaClient
import dev.craftmind.agent.infrastructure.repository.InMemoryConversationRepository
import dev.craftmind.agent.presentation.web.handler.ChatHandler
import dev.craftmind.agent.presentation.web.WebServer
import java.net.http.HttpClient
import java.time.Duration

class DependencyContainer(private val config: ApplicationConfig) {
    
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(config.ollama.connectTimeout)
        .build()
    
    private val ollamaClient = OllamaClient(config.ollama, httpClient)
    private val conversationRepository = InMemoryConversationRepository()
    private val conversationService = ConversationService(
        ollamaClient = ollamaClient,
        conversationRepository = conversationRepository,
        systemPrompt = "You are a helpful AI assistant.",
        maxMemorySize = 10
    )
    private val chatService = ChatApplicationService(conversationService)
    private val chatHandler = ChatHandler(chatService)
    
    fun getWebServer(): WebServer {
        return WebServer(chatHandler, config.server.port)
    }
}