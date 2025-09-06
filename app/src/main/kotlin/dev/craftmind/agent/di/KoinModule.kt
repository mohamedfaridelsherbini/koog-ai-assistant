package dev.craftmind.agent.di

import dev.craftmind.agent.application.service.ChatApplicationService
import dev.craftmind.agent.application.service.ChatApplicationServiceInterface
import dev.craftmind.agent.config.ApplicationConfig
import dev.craftmind.agent.domain.repository.ConversationRepository
import dev.craftmind.agent.domain.service.ConversationService
import dev.craftmind.agent.domain.service.ConversationServiceInterface
import dev.craftmind.agent.infrastructure.ollama.OllamaClient
import dev.craftmind.agent.infrastructure.ollama.OllamaClientInterface
import dev.craftmind.agent.infrastructure.repository.InMemoryConversationRepository
import dev.craftmind.agent.presentation.web.handler.KtorChatHandler
import dev.craftmind.agent.presentation.web.handler.KtorModelsHandler
import dev.craftmind.agent.presentation.web.handler.KtorStaticFileHandler
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    // Configuration
    singleOf(::ApplicationConfig)
    
    // Domain Services
    singleOf(::ConversationService) { bind<ConversationServiceInterface>() }
    
    // Infrastructure
    singleOf(::OllamaClient) { bind<OllamaClientInterface>() }
    singleOf(::InMemoryConversationRepository) { bind<ConversationRepository>() }
    
    // Application Services
    singleOf(::ChatApplicationService) { bind<ChatApplicationServiceInterface>() }
    
    // Presentation Handlers
    singleOf(::KtorChatHandler)
    singleOf(::KtorModelsHandler)
    singleOf(::KtorStaticFileHandler)
}
