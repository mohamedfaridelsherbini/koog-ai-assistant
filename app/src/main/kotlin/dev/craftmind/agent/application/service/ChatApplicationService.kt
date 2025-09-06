package dev.craftmind.agent.application.service

import dev.craftmind.agent.domain.service.ConversationServiceInterface
import dev.craftmind.agent.application.dto.ChatRequest
import dev.craftmind.agent.application.dto.ChatResponse
import java.time.LocalDateTime

class ChatApplicationService(
    private val conversationService: ConversationServiceInterface
) : ChatApplicationServiceInterface {
    override suspend fun sendMessage(request: ChatRequest): ChatResponse {
        val response = conversationService.sendMessage(
            userMessage = request.message,
            model = request.model ?: "llama3.1:8b"
        )
        return ChatResponse(
            response = response,
            model = request.model ?: "llama3.1:8b"
        )
    }
    
    override fun clearMemory() {
        conversationService.clearConversation()
    }
    
    override fun getMemorySize(): Int {
        return conversationService.getConversationHistory().size
    }
}