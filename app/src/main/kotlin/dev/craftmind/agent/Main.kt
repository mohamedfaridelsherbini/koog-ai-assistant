package dev.craftmind.agent

import dev.craftmind.agent.config.ApplicationConfig
import dev.craftmind.agent.presentation.web.KtorApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

fun main(args: Array<String>) {
    val config = ApplicationConfig(
        server = dev.craftmind.agent.config.ServerConfig(port = 8080),
        ollama = dev.craftmind.agent.config.OllamaConfig()
    )
    
    // Start Koin DI container
    startKoin {
        modules(dev.craftmind.agent.di.appModule)
    }
    
    try {
        // Get dependencies from Koin
        val chatHandler = org.koin.core.context.GlobalContext.get().get<dev.craftmind.agent.presentation.web.handler.KtorChatHandler>()
        val modelsHandler = org.koin.core.context.GlobalContext.get().get<dev.craftmind.agent.presentation.web.handler.KtorModelsHandler>()
        val staticFileHandler = org.koin.core.context.GlobalContext.get().get<dev.craftmind.agent.presentation.web.handler.KtorStaticFileHandler>()
        
        // Start Ktor application
        val ktorApp = KtorApplication(config, chatHandler, modelsHandler, staticFileHandler)
        ktorApp.start()
    } finally {
        stopKoin()
    }
}