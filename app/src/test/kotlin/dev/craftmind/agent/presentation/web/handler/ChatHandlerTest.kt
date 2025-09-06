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
        
        whenever(mockChatService.sendMessage(any<ChatRequest>())).thenReturn(expectedResponse)
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/chat"))
        whenever(mockExchange.responseHeaders).thenReturn(mock<com.sun.net.httpserver.Headers>())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(eq(200), anyLong())
        verify(mockExchange.responseHeaders).set(eq("Content-Type"), eq("application/json"))
        verify(mockExchange.responseHeaders).set(eq("Access-Control-Allow-Origin"), eq("*"))
        runBlocking { verify(mockChatService).sendMessage(any<ChatRequest>()) }
    }

    @Test
    fun `should reject non-POST requests`() {
        // Given
        whenever(mockExchange.requestMethod).thenReturn("GET")
        whenever(mockExchange.responseHeaders).thenReturn(mock<com.sun.net.httpserver.Headers>())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(eq(405), anyLong())
        verify(mockExchange.responseHeaders).set(eq("Content-Type"), eq("application/json"))
        verify(mockExchange.responseHeaders).set(eq("Access-Control-Allow-Origin"), eq("*"))
        runBlocking { verify(mockChatService, never()).sendMessage(any<ChatRequest>()) }
    }

    @Test
    fun `should handle invalid JSON request`() {
        // Given
        val invalidJson = "{ invalid json }"
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(invalidJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock<com.sun.net.httpserver.Headers>())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(eq(500), anyLong())
        verify(mockExchange.responseHeaders).set(eq("Content-Type"), eq("application/json"))
        verify(mockExchange.responseHeaders).set(eq("Access-Control-Allow-Origin"), eq("*"))
        runBlocking { verify(mockChatService, never()).sendMessage(any<ChatRequest>()) }
    }

    @Test
    fun `should handle empty request body`() {
        // Given
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(ByteArray(0)))
        whenever(mockExchange.responseHeaders).thenReturn(mock<com.sun.net.httpserver.Headers>())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(eq(500), anyLong())
        verify(mockExchange.responseHeaders).set(eq("Content-Type"), eq("application/json"))
        verify(mockExchange.responseHeaders).set(eq("Access-Control-Allow-Origin"), eq("*"))
        runBlocking { verify(mockChatService, never()).sendMessage(any<ChatRequest>()) }
    }

    @Test
    fun `should handle service timeout`() = runBlocking {
        // Given
        val request = ChatRequest("Hello, AI!", "llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        
        whenever(mockChatService.sendMessage(any<ChatRequest>())).thenThrow(RuntimeException("Timeout"))
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock<com.sun.net.httpserver.Headers>())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(eq(500), anyLong())
        verify(mockExchange.responseHeaders).set(eq("Content-Type"), eq("application/json"))
        verify(mockExchange.responseHeaders).set(eq("Access-Control-Allow-Origin"), eq("*"))
        runBlocking { verify(mockChatService).sendMessage(any<ChatRequest>()) }
    }

    @Test
    fun `should handle service exception`() = runBlocking {
        // Given
        val request = ChatRequest("Hello, AI!", "llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        
        whenever(mockChatService.sendMessage(any<ChatRequest>())).thenThrow(RuntimeException("Service unavailable"))
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock<com.sun.net.httpserver.Headers>())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        chatHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(eq(500), anyLong())
        verify(mockExchange.responseHeaders).set(eq("Content-Type"), eq("application/json"))
        verify(mockExchange.responseHeaders).set(eq("Access-Control-Allow-Origin"), eq("*"))
        runBlocking { verify(mockChatService).sendMessage(any<ChatRequest>()) }
    }
}