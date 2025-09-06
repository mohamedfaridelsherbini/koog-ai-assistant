package dev.craftmind.agent.application.service

import dev.craftmind.agent.domain.service.ConversationService
import dev.craftmind.agent.application.dto.ChatRequest
import dev.craftmind.agent.application.dto.ChatResponse
import java.time.LocalDateTime

class ChatApplicationService(
    private val conversationService: ConversationService
) {
    suspend fun sendMessage(request: ChatRequest): ChatResponse {
        val response = conversationService.sendMessage(
            userMessage = request.message,
            model = request.model ?: "llama3.1:8b"
        )
        return ChatResponse(
            response = response,
            model = request.model ?: "llama3.1:8b"
        )
    }
    
    fun clearMemory() {
        conversationService.clearConversation()
    }
    
    fun getMemorySize(): Int {
        return conversationService.getConversationHistory().size
    }
}