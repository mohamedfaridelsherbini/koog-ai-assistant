package dev.craftmind.agent.domain.service

import dev.craftmind.agent.domain.model.ConversationEntry
import dev.craftmind.agent.domain.repository.ConversationRepository
import dev.craftmind.agent.infrastructure.ollama.OllamaClient
import java.util.UUID

class ConversationService(
    private val ollamaClient: OllamaClient,
    private val conversationRepository: ConversationRepository,
    private val systemPrompt: String,
    private val maxMemorySize: Int
) {
    
    suspend fun sendMessage(
        userMessage: String,
        model: String = "llama3.1:8b"
    ): String {
        // Add user message to conversation
        val userEntry = ConversationEntry(
            id = UUID.randomUUID().toString(),
            role = "user",
            content = userMessage
        )
        conversationRepository.addEntry(userEntry)
        
        // Get recent conversation history
        val recentHistory = conversationRepository.getRecentEntries(maxMemorySize)
        
        // Send to LLM
        val response = ollamaClient.chat(
            model = model,
            messages = recentHistory,
            systemPrompt = systemPrompt
        )
        
        // Add assistant response to conversation
        val assistantEntry = ConversationEntry(
            id = UUID.randomUUID().toString(),
            role = "assistant",
            content = response,
            model = model
        )
        conversationRepository.addEntry(assistantEntry)
        
        return response
    }
    
    fun getConversationHistory(): List<ConversationEntry> {
        return conversationRepository.getAllEntries()
    }
    
    fun clearConversation() {
        conversationRepository.clear()
    }
}