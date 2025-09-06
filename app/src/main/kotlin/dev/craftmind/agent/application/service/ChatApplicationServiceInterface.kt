package dev.craftmind.agent.application.service

import dev.craftmind.agent.application.dto.ChatRequest
import dev.craftmind.agent.application.dto.ChatResponse

interface ChatApplicationServiceInterface {
    suspend fun sendMessage(request: ChatRequest): ChatResponse
    fun clearMemory()
    fun getMemorySize(): Int
}
