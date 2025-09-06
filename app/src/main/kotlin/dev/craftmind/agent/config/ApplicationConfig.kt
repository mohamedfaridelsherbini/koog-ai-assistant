package dev.craftmind.agent.config

import java.time.Duration

data class ApplicationConfig(
    val server: ServerConfig,
    val ollama: OllamaConfig
)

data class ServerConfig(
    val port: Int = 8080,
    val host: String = "localhost"
)

data class OllamaConfig(
    val baseUrl: String = "http://localhost:11434",
    val connectTimeout: Duration = Duration.ofSeconds(30),
    val readTimeout: Duration = Duration.ofMinutes(5)
)