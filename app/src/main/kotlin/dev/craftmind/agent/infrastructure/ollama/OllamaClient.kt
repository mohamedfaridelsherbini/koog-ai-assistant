package dev.craftmind.agent.infrastructure.ollama

import dev.craftmind.agent.config.OllamaConfig
import dev.craftmind.agent.domain.model.ConversationEntry
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
import java.time.Duration

class OllamaClient(
    private val config: OllamaConfig,
    private val httpClient: HttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun chat(
        model: String,
        messages: List<ConversationEntry>,
        systemPrompt: String? = null
    ): String {
        val requestBody = ChatRequest(
            model = model,
            messages = messages.map { entry ->
                ChatMessage(
                    role = entry.role,
                    content = entry.content
                )
            },
            stream = false,
            system = systemPrompt
        )

        val request = HttpRequest.newBuilder()
            .uri(URI.create("${config.baseUrl}/api/chat"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json.encodeToString(requestBody)))
            .timeout(config.readTimeout)
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() != 200) {
            throw RuntimeException("Ollama API error: ${response.statusCode()} - ${response.body()}")
        }

        val responseBody = response.body()
        println("🔍 Ollama response: $responseBody") // Debug log
        
        // Handle both single response and multiple responses
        val responses = responseBody.split("\n").filter { it.isNotBlank() }
        var fullContent = ""
        
        for (jsonLine in responses) {
            try {
                val chatResponse = json.decodeFromString<ChatResponse>(jsonLine)
                fullContent += chatResponse.message.content
                if (chatResponse.done) break
            } catch (e: Exception) {
                println("⚠️ Failed to parse JSON line: $jsonLine")
                // Try to extract content manually if JSON parsing fails
                if (jsonLine.contains("\"content\"")) {
                    val contentMatch = Regex("\"content\"\\s*:\\s*\"([^\"]*)\"").find(jsonLine)
                    if (contentMatch != null) {
                        fullContent += contentMatch.groupValues[1]
                    }
                }
            }
        }
        
        return fullContent.ifEmpty { "Sorry, I couldn't process that request." }
    }

    suspend fun listModels(): List<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${config.baseUrl}/api/tags"))
            .GET()
            .timeout(config.connectTimeout)
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() != 200) {
            throw RuntimeException("Failed to list models: ${response.statusCode()}")
        }

        val modelsResponse = json.decodeFromString<ModelsResponse>(response.body())
        return modelsResponse.models.map { it.name }
    }
}

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    val system: String? = null
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatResponse(
    val message: ChatMessage,
    val done: Boolean
)

@Serializable
data class ModelsResponse(
    val models: List<ModelInfo>
)

@Serializable
data class ModelInfo(
    val name: String,
    val model: String
)