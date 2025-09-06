package dev.craftmind.agent.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ApplicationConfigTest {

    @Test
    fun `should create ApplicationConfig with valid parameters`() {
        // Given
        val serverConfig = ServerConfig(port = 8080)
        val ollamaConfig = OllamaConfig()

        // When
        val config = ApplicationConfig(serverConfig, ollamaConfig)

        // Then
        assertEquals(serverConfig, config.server)
        assertEquals(ollamaConfig, config.ollama)
    }

    @Test
    fun `should create ApplicationConfig with custom port`() {
        // Given
        val serverConfig = ServerConfig(port = 9090)
        val ollamaConfig = OllamaConfig()

        // When
        val config = ApplicationConfig(serverConfig, ollamaConfig)

        // Then
        assertEquals(9090, config.server.port)
        assertEquals(ollamaConfig, config.ollama)
    }

    @Test
    fun `should create ApplicationConfig with custom Ollama settings`() {
        // Given
        val serverConfig = ServerConfig(port = 8080)
        val ollamaConfig = OllamaConfig(
            baseUrl = "http://localhost:11434",
            connectTimeout = java.time.Duration.ofSeconds(30)
        )

        // When
        val config = ApplicationConfig(serverConfig, ollamaConfig)

        // Then
        assertEquals(serverConfig, config.server)
        assertEquals("http://localhost:11434", config.ollama.baseUrl)
        assertEquals(java.time.Duration.ofSeconds(30), config.ollama.connectTimeout)
    }

    @Test
    fun `should handle default ServerConfig values`() {
        // Given
        val serverConfig = ServerConfig()
        val ollamaConfig = OllamaConfig()

        // When
        val config = ApplicationConfig(serverConfig, ollamaConfig)

        // Then
        assertEquals(8080, config.server.port) // Default port
        assertEquals(ollamaConfig, config.ollama)
    }

    @Test
    fun `should handle default OllamaConfig values`() {
        // Given
        val serverConfig = ServerConfig(port = 8080)
        val ollamaConfig = OllamaConfig()

        // When
        val config = ApplicationConfig(serverConfig, ollamaConfig)

        // Then
        assertEquals(serverConfig, config.server)
        assertEquals("http://localhost:11434", config.ollama.baseUrl)
        assertEquals(java.time.Duration.ofSeconds(30), config.ollama.connectTimeout)
    }

    @Test
    fun `should handle different port numbers`() {
        // Given
        val ports = listOf(3000, 8080, 9090, 3001, 5000)

        // When & Then
        ports.forEach { port ->
            val serverConfig = ServerConfig(port = port)
            val ollamaConfig = OllamaConfig()
            val config = ApplicationConfig(serverConfig, ollamaConfig)
            
            assertEquals(port, config.server.port)
        }
    }
}
