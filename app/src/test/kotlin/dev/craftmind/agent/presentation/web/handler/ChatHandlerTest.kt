package dev.craftmind.agent.presentation.web.handler

import dev.craftmind.agent.application.service.ChatApplicationServiceInterface
import dev.craftmind.agent.application.dto.ChatRequest
import dev.craftmind.agent.application.dto.ChatResponse
import com.sun.net.httpserver.HttpExchange
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.charset.StandardCharsets

class ChatHandlerTest {

    private lateinit var chatHandler: ChatHandler
    private lateinit var mockChatService: MockChatApplicationService
    private lateinit var mockExchange: MockHttpExchange

    @BeforeEach
    fun setUp() {
        mockChatService = MockChatApplicationService()
        chatHandler = ChatHandler(mockChatService)
        mockExchange = MockHttpExchange()
    }

    @Test
    fun `should handle POST request successfully`() {
        // Given
        val request = ChatRequest("Hello, AI!", "llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        val expectedResponse = ChatResponse("Hello! How can I help you?", "llama3.1:8b")
        
        mockChatService.setResponse(expectedResponse)
        mockExchange.setupPostRequest(requestJson)

        // When
        chatHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
        
        val responseJson = mockExchange.responseBody.toString()
        val actualResponse = Json.decodeFromString<ChatResponse>(responseJson)
        assertEquals(expectedResponse.response, actualResponse.response)
        assertEquals(expectedResponse.model, actualResponse.model)
    }

    @Test
    fun `should reject non-POST requests`() {
        // Given
        mockExchange.setupGetRequest()

        // When
        chatHandler.handle(mockExchange)

        // Then
        assertEquals(405, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("Method not allowed"))
    }

    @Test
    fun `should handle invalid JSON request`() {
        // Given
        val invalidJson = "{ invalid json }"
        mockExchange.setupPostRequest(invalidJson)

        // When
        chatHandler.handle(mockExchange)

        // Then
        assertEquals(500, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("Error processing chat"))
    }

    @Test
    fun `should handle empty request body`() {
        // Given
        mockExchange.setupPostRequest("")

        // When
        chatHandler.handle(mockExchange)

        // Then
        assertEquals(500, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("Error processing chat"))
    }

    @Test
    fun `should handle service timeout`() {
        // Given
        val request = ChatRequest("Hello, AI!", "llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        
        mockChatService.setTimeoutException()
        mockExchange.setupPostRequest(requestJson)

        // When
        chatHandler.handle(mockExchange)

        // Then
        assertEquals(408, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("Request timeout"))
    }

    @Test
    fun `should handle service exception`() {
        // Given
        val request = ChatRequest("Hello, AI!", "llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        
        mockChatService.setException("Service unavailable")
        mockExchange.setupPostRequest(requestJson)

        // When
        chatHandler.handle(mockExchange)

        // Then
        assertEquals(500, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("Error processing chat"))
        assertTrue(responseJson.contains("Service unavailable"))
    }

    @Test
    fun `should handle different models`() {
        // Given
        val models = listOf("llama3.1:8b", "deepseek-coder:6.7b", "gemma:2b")
        
        models.forEach { model ->
            val request = ChatRequest("Test message", model)
            val requestJson = Json.encodeToString(request)
            val expectedResponse = ChatResponse("Response from $model", model)
            
            mockChatService.setResponse(expectedResponse)
            mockExchange.setupPostRequest(requestJson)

            // When
            chatHandler.handle(mockExchange)

            // Then
            assertEquals(200, mockExchange.responseCode)
            val responseJson = mockExchange.responseBody.toString()
            val actualResponse = Json.decodeFromString<ChatResponse>(responseJson)
            assertEquals(model, actualResponse.model)
        }
    }

    @Test
    fun `should handle empty message`() {
        // Given
        val request = ChatRequest("", "llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        val expectedResponse = ChatResponse("I received an empty message.", "llama3.1:8b")
        
        mockChatService.setResponse(expectedResponse)
        mockExchange.setupPostRequest(requestJson)

        // When
        chatHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        val responseJson = mockExchange.responseBody.toString()
        val actualResponse = Json.decodeFromString<ChatResponse>(responseJson)
        assertEquals("I received an empty message.", actualResponse.response)
    }

    @Test
    fun `should handle long message`() {
        // Given
        val longMessage = "This is a very long message that contains multiple sentences and should be handled properly by the ChatHandler class. It includes various characters and symbols like @#$%^&*() and numbers 1234567890."
        val request = ChatRequest(longMessage, "llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        val expectedResponse = ChatResponse("I received your long message.", "llama3.1:8b")
        
        mockChatService.setResponse(expectedResponse)
        mockExchange.setupPostRequest(requestJson)

        // When
        chatHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        val responseJson = mockExchange.responseBody.toString()
        val actualResponse = Json.decodeFromString<ChatResponse>(responseJson)
        assertEquals("I received your long message.", actualResponse.response)
    }

    // Mock implementations for testing
    private class MockChatApplicationService : ChatApplicationServiceInterface {
        private var response: ChatResponse? = null
        private var exception: Exception? = null
        private var timeoutException = false

        fun setResponse(response: ChatResponse) {
            this.response = response
            this.exception = null
            this.timeoutException = false
        }

        fun setException(message: String) {
            this.exception = RuntimeException(message)
            this.response = null
            this.timeoutException = false
        }

        fun setTimeoutException() {
            this.timeoutException = true
            this.response = null
            this.exception = null
        }

        override suspend fun sendMessage(request: ChatRequest): ChatResponse {
            if (timeoutException) {
                throw RuntimeException("Timeout")
            }
            exception?.let { throw it }
            return response ?: throw RuntimeException("No response set")
        }

        override fun clearMemory() {
            // Mock implementation
        }

        override fun getMemorySize(): Int {
            return 0
        }
    }

    private class MockConversationService : dev.craftmind.agent.domain.service.ConversationServiceInterface {
        override suspend fun sendMessage(userMessage: String, model: String): String {
            return "Mock response"
        }

        override fun getConversationHistory(): List<dev.craftmind.agent.domain.model.ConversationEntry> {
            return emptyList()
        }

        override fun clearConversation() {
            // Mock implementation
        }
    }

    private class MockHttpExchange : HttpExchange() {
        private var responseCode = 200
        private val responseHeaders = mutableMapOf<String, String>()
        private val responseBody = ByteArrayOutputStream()
        private var requestMethod = "GET"
        private var requestBody = ByteArrayInputStream(ByteArray(0))

        fun setupPostRequest(body: String) {
            requestMethod = "POST"
            requestBody = ByteArrayInputStream(body.toByteArray())
        }

        fun setupGetRequest() {
            requestMethod = "GET"
            requestBody = ByteArrayInputStream(ByteArray(0))
        }

        override fun getRequestMethod(): String = requestMethod

        override fun getRequestURI(): URI = URI.create("/api/chat")

        override fun getRequestHeaders() = com.sun.net.httpserver.Headers()

        override fun getRequestBody() = requestBody

        override fun getResponseHeaders() = com.sun.net.httpserver.Headers()

        override fun sendResponseHeaders(rCode: Int, responseLength: Long) {
            responseCode = rCode
        }

        override fun getResponseBody(): java.io.OutputStream = responseBody

        override fun getResponseCode(): Int = responseCode

        fun getResponseHeader(name: String): String? = responseHeaders[name]

        fun setResponseHeaders(name: String, value: String) {
            responseHeaders[name] = value
        }

        override fun close() {
            // Mock implementation
        }

        // Required abstract methods
        override fun getHttpContext() = throw UnsupportedOperationException()
        override fun getRemoteAddress() = throw UnsupportedOperationException()
        override fun getLocalAddress() = throw UnsupportedOperationException()
        override fun getProtocol() = throw UnsupportedOperationException()
        override fun getAttribute(name: String) = throw UnsupportedOperationException()
        override fun setAttribute(name: String, value: Any?) = throw UnsupportedOperationException()
        override fun setStreams(input: java.io.InputStream, output: java.io.OutputStream) = throw UnsupportedOperationException()
        override fun getPrincipal() = throw UnsupportedOperationException()
    }
}
