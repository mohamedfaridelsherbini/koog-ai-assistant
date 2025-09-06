package dev.craftmind.agent.domain.service

import dev.craftmind.agent.domain.model.ConversationEntry
import dev.craftmind.agent.domain.repository.ConversationRepository
import dev.craftmind.agent.infrastructure.ollama.OllamaClientInterface
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class ConversationServiceTest {

    private lateinit var conversationService: ConversationService
    private lateinit var mockOllamaClient: MockOllamaClient
    private lateinit var mockRepository: MockConversationRepository

    @BeforeEach
    fun setUp() {
        mockOllamaClient = MockOllamaClient()
        mockRepository = MockConversationRepository()
        conversationService = ConversationService(
            ollamaClient = mockOllamaClient,
            conversationRepository = mockRepository,
            systemPrompt = "You are a helpful AI assistant.",
            maxMemorySize = 10
        )
    }

    @Test
    fun `should send message and get response`() = runBlocking {
        // Given
        val userMessage = "Hello, AI!"
        val model = "llama3.1:8b"
        val expectedResponse = "Hello! How can I help you today?"
        mockOllamaClient.setResponse(expectedResponse)

        // When
        val response = conversationService.sendMessage(userMessage, model)

        // Then
        assertEquals(expectedResponse, response)
        assertEquals(2, mockRepository.totalAddEntryCalls) // User + Assistant entries
        assertEquals(1, mockRepository.getRecentEntriesCallCount) // Called once to get history for LLM
    }

    @Test
    fun `should add user message to conversation`() = runBlocking {
        // Given
        val userMessage = "Test message"
        val model = "llama3.1:8b"
        mockOllamaClient.setResponse("Response")

        // When
        conversationService.sendMessage(userMessage, model)

        // Then
        val userEntry = mockRepository.addedEntries.first()
        assertEquals("user", userEntry.role)
        assertEquals(userMessage, userEntry.content)
        assertNotNull(userEntry.id)
    }

    @Test
    fun `should add assistant response to conversation`() = runBlocking {
        // Given
        val userMessage = "Test message"
        val model = "llama3.1:8b"
        val expectedResponse = "AI Response"
        mockOllamaClient.setResponse(expectedResponse)

        // When
        conversationService.sendMessage(userMessage, model)

        // Then
        val assistantEntry = mockRepository.addedEntries.last()
        assertEquals("assistant", assistantEntry.role)
        assertEquals(expectedResponse, assistantEntry.content)
        assertEquals(model, assistantEntry.model)
        assertNotNull(assistantEntry.id)
    }

    @Test
    fun `should call OllamaClient with correct parameters`() = runBlocking {
        // Given
        val userMessage = "Test message"
        val model = "deepseek-coder:6.7b"
        val expectedResponse = "Response from DeepSeek"
        mockOllamaClient.setResponse(expectedResponse)

        // When
        conversationService.sendMessage(userMessage, model)

        // Then
        assertEquals(model, mockOllamaClient.lastModel)
        assertEquals("You are a helpful AI assistant.", mockOllamaClient.lastSystemPrompt)
        assertTrue(mockOllamaClient.lastMessages.isNotEmpty())
    }

    @Test
    fun `should get conversation history`() {
        // Given
        val expectedHistory = listOf(
            ConversationEntry("id1", "user", "Hello"),
            ConversationEntry("id2", "assistant", "Hi there!")
        )
        mockRepository.setAllEntries(expectedHistory)

        // When
        val history = conversationService.getConversationHistory()

        // Then
        assertEquals(expectedHistory, history)
        assertEquals(1, mockRepository.getAllEntriesCallCount)
    }

    @Test
    fun `should clear conversation`() {
        // When
        conversationService.clearConversation()

        // Then
        assertTrue(mockRepository.clearCalled)
        assertEquals(1, mockRepository.clearCallCount)
    }

    @Test
    fun `should handle empty user message`() = runBlocking {
        // Given
        val userMessage = ""
        val model = "llama3.1:8b"
        val expectedResponse = "I received an empty message."
        mockOllamaClient.setResponse(expectedResponse)

        // When
        val response = conversationService.sendMessage(userMessage, model)

        // Then
        assertEquals(expectedResponse, response)
        val userEntry = mockRepository.addedEntries.first()
        assertEquals("", userEntry.content)
    }

    @Test
    fun `should handle long user message`() = runBlocking {
        // Given
        val longMessage = "This is a very long message that contains multiple sentences and should be handled properly by the ConversationService class. It includes various characters and symbols like @#$%^&*() and numbers 1234567890."
        val model = "llama3.1:8b"
        val expectedResponse = "I received your long message."
        mockOllamaClient.setResponse(expectedResponse)

        // When
        val response = conversationService.sendMessage(longMessage, model)

        // Then
        assertEquals(expectedResponse, response)
        val userEntry = mockRepository.addedEntries.first()
        assertEquals(longMessage, userEntry.content)
    }

    @Test
    fun `should handle different models`() = runBlocking {
        // Given
        val models = listOf("llama3.1:8b", "deepseek-coder:6.7b", "gemma:2b", "phi3:mini")
        val userMessage = "Test message"

        models.forEach { model ->
            val expectedResponse = "Response from $model"
            mockOllamaClient.setResponse(expectedResponse)

            // When
            val response = conversationService.sendMessage(userMessage, model)

            // Then
            assertEquals(expectedResponse, response)
            assertEquals(model, mockOllamaClient.lastModel)
        }
    }

    @Test
    fun `should use maxMemorySize for recent entries`() = runBlocking {
        // Given
        val userMessage = "Test message"
        val model = "llama3.1:8b"
        mockOllamaClient.setResponse("Response")

        // When
        conversationService.sendMessage(userMessage, model)

        // Then
        assertEquals(10, mockRepository.lastGetRecentEntriesLimit)
    }

    @Test
    fun `should handle special characters in message`() = runBlocking {
        // Given
        val specialMessage = "Special chars: \n\t\r\"'`~!@#$%^&*()_+-=[]{}|;':\",./<>?"
        val model = "llama3.1:8b"
        val expectedResponse = "I received your special message."
        mockOllamaClient.setResponse(expectedResponse)

        // When
        val response = conversationService.sendMessage(specialMessage, model)

        // Then
        assertEquals(expectedResponse, response)
        val userEntry = mockRepository.addedEntries.first()
        assertEquals(specialMessage, userEntry.content)
    }

    // Mock implementations for testing
    private class MockOllamaClient : OllamaClientInterface {
        private var response: String = ""
        var lastModel: String = ""
        var lastMessages: List<ConversationEntry> = emptyList()
        var lastSystemPrompt: String? = ""

        fun setResponse(response: String) {
            this.response = response
        }

        override suspend fun chat(
            model: String,
            messages: List<ConversationEntry>,
            systemPrompt: String?
        ): String {
            lastModel = model
            lastMessages = messages
            lastSystemPrompt = systemPrompt
            return response
        }

        override suspend fun listModels(): List<String> {
            return listOf("llama3.1:8b", "deepseek-coder:6.7b")
        }
    }

    private class MockConversationRepository : ConversationRepository {
        val addedEntries = mutableListOf<ConversationEntry>()
        private var allEntries: List<ConversationEntry> = emptyList()
        var addEntryCallCount = 0
        var getRecentEntriesCallCount = 0
        var getAllEntriesCallCount = 0
        var clearCallCount = 0
        var clearCalled = false
        var lastGetRecentEntriesLimit = 0
        var totalAddEntryCalls = 0

        fun setAllEntries(entries: List<ConversationEntry>) {
            this.allEntries = entries
        }

        override fun addEntry(entry: ConversationEntry) {
            addedEntries.add(entry)
            addEntryCallCount++
            totalAddEntryCalls++
        }

        override fun getRecentEntries(limit: Int): List<ConversationEntry> {
            getRecentEntriesCallCount++
            lastGetRecentEntriesLimit = limit
            return addedEntries.takeLast(limit)
        }

        override fun getAllEntries(): List<ConversationEntry> {
            getAllEntriesCallCount++
            return allEntries
        }

        override fun clear() {
            clearCalled = true
            clearCallCount++
            addedEntries.clear()
            allEntries = emptyList()
        }
    }
}
