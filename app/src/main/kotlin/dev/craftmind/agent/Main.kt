package dev.craftmind.agent

import dev.craftmind.agent.config.ApplicationConfig
import dev.craftmind.agent.config.DependencyContainer
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {
    val config = ApplicationConfig(
        server = dev.craftmind.agent.config.ServerConfig(port = 8080),
        ollama = dev.craftmind.agent.config.OllamaConfig()
    )
    val container = DependencyContainer(config)
    val webServer = container.getWebServer()
    webServer.start()
}