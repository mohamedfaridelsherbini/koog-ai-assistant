package dev.craftmind.agent.presentation.web.handler

import dev.craftmind.agent.application.service.ChatApplicationServiceInterface
import dev.craftmind.agent.application.dto.ChatRequest
import dev.craftmind.agent.application.dto.ChatResponse
import com.sun.net.httpserver.HttpExchange
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import kotlinx.coroutines.runBlocking

class ChatHandlerTest {

    private lateinit var chatHandler: ChatHandler
    private lateinit var mockChatService: ChatApplicationServiceInterface
    private lateinit var mockExchange: HttpExchange

    @BeforeEach
    fun setUp() {
        mockChatService = mock<ChatApplicationServiceInterface>()
        chatHandler = ChatHandler(mockChatService)
        mockExchange = mock<HttpExchange>()
    }

    @Test
    fun `should handle POST request successfully`() = runBlocking {
        // Given
        val request = ChatRequest("Hello, AI!", "llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        val expectedResponse = ChatResponse("Hello! How can I help you?", "llama3.1:8b")
        
        whenever(mockChatService.sendMessage(any())).thenReturn(expectedResponse)
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/chat"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(200, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
        runBlocking { verify(mockChatService).sendMessage(any()) }
    }

    @Test
    fun `should reject non-POST requests`() {
        // Given
        whenever(mockExchange.requestMethod).thenReturn("GET")
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(405, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
        runBlocking { verify(mockChatService, never()).sendMessage(any()) }
    }

    @Test
    fun `should handle invalid JSON request`() {
        // Given
        val invalidJson = "{ invalid json }"
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(invalidJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(500, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
        runBlocking { verify(mockChatService, never()).sendMessage(any()) }
    }

    @Test
    fun `should handle empty request body`() {
        // Given
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(ByteArray(0)))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(500, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
        runBlocking { verify(mockChatService, never()).sendMessage(any()) }
    }

    @Test
    fun `should handle service timeout`() = runBlocking {
        // Given
        val request = ChatRequest("Hello, AI!", "llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        
        whenever(mockChatService.sendMessage(any())).thenThrow(RuntimeException("Timeout"))
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(500, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
        runBlocking { verify(mockChatService).sendMessage(any()) }
    }

    @Test
    fun `should handle service exception`() = runBlocking {
        // Given
        val request = ChatRequest("Hello, AI!", "llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        
        whenever(mockChatService.sendMessage(any())).thenThrow(RuntimeException("Service unavailable"))
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(500, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
        runBlocking { verify(mockChatService).sendMessage(any()) }
    }

    @Test
    fun `should handle different models`() = runBlocking {
        // Given
        val models = listOf("llama3.1:8b", "deepseek-coder:6.7b", "gemma:2b")
        
        models.forEach { model ->
            val request = ChatRequest("Test message", model)
            val requestJson = Json.encodeToString(request)
            val expectedResponse = ChatResponse("Response from $model", model)
            
            whenever(mockChatService.sendMessage(any())).thenReturn(expectedResponse)
            whenever(mockExchange.requestMethod).thenReturn("POST")
            whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
            whenever(mockExchange.responseHeaders).thenReturn(mock())
            whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

            // When
            chatHandler.handle(mockExchange)

            // Then
            verify(mockExchange).sendResponseHeaders(200, anyLong())
            verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
            verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
            runBlocking { verify(mockChatService).sendMessage(any()) }
            
            // Clear mocks for next iteration
            reset(mockExchange, mockChatService)
        }
    }

    @Test
    fun `should handle empty message`() = runBlocking {
        // Given
        val request = ChatRequest("", "llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        val expectedResponse = ChatResponse("I received an empty message.", "llama3.1:8b")
        
        whenever(mockChatService.sendMessage(any())).thenReturn(expectedResponse)
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(200, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
        runBlocking { verify(mockChatService).sendMessage(any()) }
    }

    @Test
    fun `should handle long message`() = runBlocking {
        // Given
        val longMessage = "This is a very long message that contains multiple sentences and should be handled properly by the ChatHandler class. It includes various characters and symbols like @#$%^&*() and numbers 1234567890."
        val request = ChatRequest(longMessage, "llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        val expectedResponse = ChatResponse("I received your long message.", "llama3.1:8b")
        
        whenever(mockChatService.sendMessage(any())).thenReturn(expectedResponse)
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(200, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
        runBlocking { verify(mockChatService).sendMessage(any()) }
    }
}