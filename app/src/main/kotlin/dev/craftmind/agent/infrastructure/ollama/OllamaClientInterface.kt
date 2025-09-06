package dev.craftmind.agent.infrastructure.ollama

import dev.craftmind.agent.domain.model.ConversationEntry

interface OllamaClientInterface {
    suspend fun chat(
        model: String,
        messages: List<ConversationEntry>,
        systemPrompt: String? = null
    ): String
    
    suspend fun listModels(): List<String>
}
