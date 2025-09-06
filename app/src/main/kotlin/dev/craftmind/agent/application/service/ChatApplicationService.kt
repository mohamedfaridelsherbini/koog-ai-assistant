package dev.craftmind.agent.application.service

import dev.craftmind.agent.domain.service.ConversationServiceInterface
import dev.craftmind.agent.application.dto.ChatRequest
import dev.craftmind.agent.application.dto.ChatResponse
import java.time.LocalDateTime

class ChatApplicationService(
    private val conversationService: ConversationServiceInterface
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