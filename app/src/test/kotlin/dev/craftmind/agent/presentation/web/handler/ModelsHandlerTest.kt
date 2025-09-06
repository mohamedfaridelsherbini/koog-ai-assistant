package dev.craftmind.agent.presentation.web.handler

import com.sun.net.httpserver.HttpExchange
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.charset.StandardCharsets

class ModelsHandlerTest {

    private lateinit var modelsHandler: ModelsHandler
    private lateinit var mockExchange: MockHttpExchange

    @BeforeEach
    fun setUp() {
        modelsHandler = ModelsHandler()
        mockExchange = MockHttpExchange()
    }

    @Test
    fun `should handle GET api models request`() {
        // Given
        mockExchange.setupGetRequest("/api/models")

        // When
        modelsHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("models"))
    }

    @Test
    fun `should handle GET api models all request`() {
        // Given
        mockExchange.setupGetRequest("/api/models/all")

        // When
        modelsHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("downloadedModels"))
        assertTrue(responseJson.contains("availableModels"))
    }

    @Test
    fun `should handle POST api models pull request`() {
        // Given
        val request = PullModelRequest("llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        mockExchange.setupPostRequest("/api/models/pull", requestJson)

        // When
        modelsHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("success"))
        assertTrue(responseJson.contains("Model pull initiated"))
        assertTrue(responseJson.contains("llama3.1:8b"))
    }

    @Test
    fun `should handle POST api models delete request`() {
        // Given
        val request = DeleteModelRequest("llama3.1:8b")
        val requestJson = Json.encodeToString(request)
        mockExchange.setupPostRequest("/api/models/delete", requestJson)

        // When
        modelsHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("success"))
        assertTrue(responseJson.contains("deleted successfully"))
        assertTrue(responseJson.contains("llama3.1:8b"))
    }

    @Test
    fun `should handle POST api models switch request`() {
        // Given
        val request = SwitchModelRequest("deepseek-coder:6.7b")
        val requestJson = Json.encodeToString(request)
        mockExchange.setupPostRequest("/api/models/switch", requestJson)

        // When
        modelsHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("success"))
        assertTrue(responseJson.contains("Switched to"))
        assertTrue(responseJson.contains("deepseek-coder:6.7b"))
    }

    @Test
    fun `should reject unsupported methods`() {
        // Given
        mockExchange.setupPutRequest("/api/models")

        // When
        modelsHandler.handle(mockExchange)

        // Then
        assertEquals(405, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("Method not allowed"))
    }

    @Test
    fun `should reject unsupported paths`() {
        // Given
        mockExchange.setupGetRequest("/api/unknown")

        // When
        modelsHandler.handle(mockExchange)

        // Then
        assertEquals(405, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("Method not allowed"))
    }

    @Test
    fun `should handle invalid JSON in pull request`() {
        // Given
        val invalidJson = "{ invalid json }"
        mockExchange.setupPostRequest("/api/models/pull", invalidJson)

        // When
        modelsHandler.handle(mockExchange)

        // Then
        assertEquals(500, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("Error pulling model"))
    }

    @Test
    fun `should handle invalid JSON in delete request`() {
        // Given
        val invalidJson = "{ invalid json }"
        mockExchange.setupPostRequest("/api/models/delete", invalidJson)

        // When
        modelsHandler.handle(mockExchange)

        // Then
        assertEquals(500, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("Error deleting model"))
    }

    @Test
    fun `should handle invalid JSON in switch request`() {
        // Given
        val invalidJson = "{ invalid json }"
        mockExchange.setupPostRequest("/api/models/switch", invalidJson)

        // When
        modelsHandler.handle(mockExchange)

        // Then
        assertEquals(500, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("Error switching model"))
    }

    @Test
    fun `should handle empty request body in POST requests`() {
        // Given
        mockExchange.setupPostRequest("/api/models/pull", "")

        // When
        modelsHandler.handle(mockExchange)

        // Then
        assertEquals(500, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains("Error pulling model"))
    }

    @Test
    fun `should handle different model names in requests`() {
        // Given
        val models = listOf("llama3.1:8b", "deepseek-coder:6.7b", "gemma:2b", "phi3:mini")
        
        models.forEach { model ->
            val request = PullModelRequest(model)
            val requestJson = Json.encodeToString(request)
            mockExchange.setupPostRequest("/api/models/pull", requestJson)

            // When
            modelsHandler.handle(mockExchange)

            // Then
            assertEquals(200, mockExchange.responseCode)
            val responseJson = mockExchange.responseBody.toString()
            assertTrue(responseJson.contains(model))
        }
    }

    @Test
    fun `should handle special characters in model names`() {
        // Given
        val specialModelName = "model-with-special-chars_123"
        val request = DeleteModelRequest(specialModelName)
        val requestJson = Json.encodeToString(request)
        mockExchange.setupPostRequest("/api/models/delete", requestJson)

        // When
        modelsHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        val responseJson = mockExchange.responseBody.toString()
        assertTrue(responseJson.contains(specialModelName))
    }

    // Mock implementations for testing
    private class MockHttpExchange : HttpExchange() {
        private var responseCode = 200
        private val responseHeaders = mutableMapOf<String, String>()
        private val responseBody = ByteArrayOutputStream()
        private var requestMethod = "GET"
        private var requestBody = ByteArrayInputStream(ByteArray(0))
        private var requestPath = "/"

        fun setupGetRequest(path: String) {
            requestMethod = "GET"
            requestPath = path
            requestBody = ByteArrayInputStream(ByteArray(0))
        }

        fun setupPostRequest(path: String, body: String) {
            requestMethod = "POST"
            requestPath = path
            requestBody = ByteArrayInputStream(body.toByteArray())
        }

        fun setupPutRequest(path: String) {
            requestMethod = "PUT"
            requestPath = path
            requestBody = ByteArrayInputStream(ByteArray(0))
        }

        override fun getRequestMethod(): String = requestMethod

        override fun getRequestURI(): URI = URI.create(requestPath)

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
