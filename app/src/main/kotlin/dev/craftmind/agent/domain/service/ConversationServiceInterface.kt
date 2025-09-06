package dev.craftmind.agent.domain.service

import dev.craftmind.agent.domain.model.ConversationEntry

interface ConversationServiceInterface {
    suspend fun sendMessage(userMessage: String, model: String = "llama3.1:8b"): String
    fun getConversationHistory(): List<ConversationEntry>
    fun clearConversation()
}
