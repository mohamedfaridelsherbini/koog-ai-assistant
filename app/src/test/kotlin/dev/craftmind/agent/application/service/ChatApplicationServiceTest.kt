package dev.craftmind.agent.application.service

import dev.craftmind.agent.application.dto.ChatRequest
import dev.craftmind.agent.application.dto.ChatResponse
import dev.craftmind.agent.domain.model.ConversationEntry
import dev.craftmind.agent.domain.service.ConversationServiceInterface
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class ChatApplicationServiceTest {

    private lateinit var chatApplicationService: ChatApplicationService
    private lateinit var mockConversationService: MockConversationService

    @BeforeEach
    fun setUp() {
        mockConversationService = MockConversationService()
        chatApplicationService = ChatApplicationService(mockConversationService)
    }

    @Test
    fun `should send message with default model`() = runBlocking {
        // Given
        val request = ChatRequest("Hello, AI!")
        val expectedResponse = "Hello! How can I help you today?"
        mockConversationService.setResponse(expectedResponse)

        // When
        val response = chatApplicationService.sendMessage(request)

        // Then
        assertEquals(expectedResponse, response.response)
        assertEquals("llama3.1:8b", response.model)
        assertEquals("Hello, AI!", mockConversationService.lastUserMessage)
        assertEquals("llama3.1:8b", mockConversationService.lastModel)
    }

    @Test
    fun `should send message with custom model`() = runBlocking {
        // Given
        val request = ChatRequest("Hello, AI!", "deepseek-coder:6.7b")
        val expectedResponse = "Hello! I'm DeepSeek Coder."
        mockConversationService.setResponse(expectedResponse)

        // When
        val response = chatApplicationService.sendMessage(request)

        // Then
        assertEquals(expectedResponse, response.response)
        assertEquals("deepseek-coder:6.7b", response.model)
        assertEquals("Hello, AI!", mockConversationService.lastUserMessage)
        assertEquals("deepseek-coder:6.7b", mockConversationService.lastModel)
    }

    @Test
    fun `should handle empty message`() = runBlocking {
        // Given
        val request = ChatRequest("")
        val expectedResponse = "I received an empty message."
        mockConversationService.setResponse(expectedResponse)

        // When
        val response = chatApplicationService.sendMessage(request)

        // Then
        assertEquals(expectedResponse, response.response)
        assertEquals("llama3.1:8b", response.model)
        assertEquals("", mockConversationService.lastUserMessage)
    }

    @Test
    fun `should clear memory`() {
        // When
        chatApplicationService.clearMemory()

        // Then
        assertTrue(mockConversationService.clearCalled)
    }

    @Test
    fun `should get memory size`() {
        // Given
        val conversationHistory = listOf(
            ConversationEntry("id1", "user", "Hello"),
            ConversationEntry("id2", "assistant", "Hi there!")
        )
        mockConversationService.setConversationHistory(conversationHistory)

        // When
        val memorySize = chatApplicationService.getMemorySize()

        // Then
        assertEquals(2, memorySize)
    }

    @Test
    fun `should return zero memory size when conversation is empty`() {
        // Given
        mockConversationService.setConversationHistory(emptyList())

        // When
        val memorySize = chatApplicationService.getMemorySize()

        // Then
        assertEquals(0, memorySize)
    }

    // Mock implementation for testing
    private class MockConversationService : ConversationServiceInterface {
        private var response: String = ""
        private var conversationHistory: List<ConversationEntry> = emptyList()
        var lastUserMessage: String = ""
        var lastModel: String = ""
        var clearCalled: Boolean = false

        fun setResponse(response: String) {
            this.response = response
        }

        fun setConversationHistory(history: List<ConversationEntry>) {
            this.conversationHistory = history
        }

        override suspend fun sendMessage(userMessage: String, model: String): String {
            lastUserMessage = userMessage
            lastModel = model
            return response
        }

        override fun getConversationHistory(): List<ConversationEntry> {
            return conversationHistory
        }

        override fun clearConversation() {
            clearCalled = true
            conversationHistory = emptyList()
        }
    }
}