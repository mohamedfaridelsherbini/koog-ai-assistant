package dev.craftmind.agent.presentation.web.handler

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

class ModelsHandlerTest {

    private lateinit var modelsHandler: ModelsHandler
    private lateinit var mockExchange: HttpExchange

    @BeforeEach
    fun setUp() {
        modelsHandler = ModelsHandler()
        mockExchange = mock<HttpExchange>()
    }

    @Test
    fun `should handle GET api models request`() {
        // Given
        whenever(mockExchange.requestMethod).thenReturn("GET")
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/models"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        modelsHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(200, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
    }

    @Test
    fun `should handle GET api models all request`() {
        // Given
        whenever(mockExchange.requestMethod).thenReturn("GET")
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/models/all"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        modelsHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(200, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
    }

    @Test
    fun `should handle POST api models pull request`() {
        // Given
        val request = PullModelRequest("llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/models/pull"))
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        modelsHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(200, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
    }

    @Test
    fun `should handle POST api models delete request`() {
        // Given
        val request = DeleteModelRequest("llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/models/delete"))
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        modelsHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(200, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
    }

    @Test
    fun `should handle POST api models switch request`() {
        // Given
        val request = SwitchModelRequest("deepseek-coder:6.7b")
        val requestJson = Json.encodeToString(request)
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/models/switch"))
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        modelsHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(200, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
    }

    @Test
    fun `should reject unsupported methods`() {
        // Given
        whenever(mockExchange.requestMethod).thenReturn("PUT")
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/models"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        modelsHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(405, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
    }

    @Test
    fun `should reject unsupported paths`() {
        // Given
        whenever(mockExchange.requestMethod).thenReturn("GET")
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/unknown"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        modelsHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(405, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
    }

    @Test
    fun `should handle invalid JSON in pull request`() {
        // Given
        val invalidJson = "{ invalid json }"
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/models/pull"))
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(invalidJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        modelsHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(500, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
    }

    @Test
    fun `should handle invalid JSON in delete request`() {
        // Given
        val invalidJson = "{ invalid json }"
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/models/delete"))
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(invalidJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        modelsHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(500, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
    }

    @Test
    fun `should handle invalid JSON in switch request`() {
        // Given
        val invalidJson = "{ invalid json }"
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/models/switch"))
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(invalidJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        modelsHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(500, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
    }

    @Test
    fun `should handle empty request body in POST requests`() {
        // Given
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/models/pull"))
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(ByteArray(0)))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        modelsHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(500, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
    }

    @Test
    fun `should handle different model names in requests`() {
        // Given
        val models = listOf("llama3.1:8b", "deepseek-coder:6.7b", "gemma:2b", "phi3:mini")
        
        models.forEach { model ->
            val request = PullModelRequest(model)
            val requestJson = Json.encodeToString(request)
            whenever(mockExchange.requestMethod).thenReturn("POST")
            whenever(mockExchange.requestURI).thenReturn(URI.create("/api/models/pull"))
            whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
            whenever(mockExchange.responseHeaders).thenReturn(mock())
            whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

            // When
            modelsHandler.handle(mockExchange)

            // Then
            verify(mockExchange).sendResponseHeaders(200, anyLong())
            verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
            verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
            
            // Clear mocks for next iteration
            reset(mockExchange)
        }
    }

    @Test
    fun `should handle special characters in model names`() {
        // Given
        val specialModelName = "model-with-special-chars_123"
        val request = DeleteModelRequest(specialModelName)
        val requestJson = Json.encodeToString(request)
        whenever(mockExchange.requestMethod).thenReturn("POST")
        whenever(mockExchange.requestURI).thenReturn(URI.create("/api/models/delete"))
        whenever(mockExchange.requestBody).thenReturn(ByteArrayInputStream(requestJson.toByteArray()))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        modelsHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(200, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "application/json")
        verify(mockExchange.responseHeaders).set("Access-Control-Allow-Origin", "*")
    }
}