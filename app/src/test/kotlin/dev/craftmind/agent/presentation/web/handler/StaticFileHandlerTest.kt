package dev.craftmind.agent.presentation.web.handler

import com.sun.net.httpserver.HttpExchange
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.charset.StandardCharsets

class StaticFileHandlerTest {

    private lateinit var staticFileHandler: StaticFileHandler
    private lateinit var mockExchange: MockHttpExchange

    @BeforeEach
    fun setUp() {
        staticFileHandler = StaticFileHandler()
        mockExchange = MockHttpExchange()
    }

    @Test
    fun `should serve index html for root path`() {
        // Given
        mockExchange.setupRequest("/")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("text/html", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should serve index html for empty path`() {
        // Given
        mockExchange.setupRequest("")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("text/html", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should serve CSS files with correct content type`() {
        // Given
        mockExchange.setupRequest("/static/css/styles.css")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("text/css", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should serve JavaScript files with correct content type`() {
        // Given
        mockExchange.setupRequest("/static/js/script.js")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("application/javascript", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should serve JSON files with correct content type`() {
        // Given
        mockExchange.setupRequest("/static/data/config.json")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("application/json", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should serve PNG images with correct content type`() {
        // Given
        mockExchange.setupRequest("/static/images/logo.png")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("image/png", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should serve JPEG images with correct content type`() {
        // Given
        mockExchange.setupRequest("/static/images/photo.jpg")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("image/jpeg", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should serve SVG images with correct content type`() {
        // Given
        mockExchange.setupRequest("/static/icons/icon.svg")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("image/svg+xml", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should serve favicon with correct content type`() {
        // Given
        mockExchange.setupRequest("/static/favicon.ico")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("image/x-icon", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should serve unknown file types with octet-stream content type`() {
        // Given
        mockExchange.setupRequest("/static/files/document.pdf")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("application/octet-stream", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should handle URL encoded paths`() {
        // Given
        mockExchange.setupRequest("/static/css/styles%20with%20spaces.css")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("text/css", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should handle paths without static prefix`() {
        // Given
        mockExchange.setupRequest("/css/styles.css")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("text/css", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should return 404 for non-existent files`() {
        // Given
        mockExchange.setupRequest("/static/nonexistent/file.html")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(404, mockExchange.responseCode)
        assertEquals("text/plain", mockExchange.getResponseHeader("Content-Type"))
        
        val responseBody = mockExchange.responseBody.toString()
        assertEquals("404 Not Found", responseBody)
    }

    @Test
    fun `should return 404 for invalid paths`() {
        // Given
        mockExchange.setupRequest("/invalid/path/file.txt")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(404, mockExchange.responseCode)
        assertEquals("text/plain", mockExchange.getResponseHeader("Content-Type"))
        
        val responseBody = mockExchange.responseBody.toString()
        assertEquals("404 Not Found", responseBody)
    }

    @Test
    fun `should handle special characters in file names`() {
        // Given
        mockExchange.setupRequest("/static/files/file-with-special-chars_123.txt")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("application/octet-stream", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should handle nested directory paths`() {
        // Given
        mockExchange.setupRequest("/static/css/components/modals.css")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("text/css", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should handle files with multiple dots`() {
        // Given
        mockExchange.setupRequest("/static/js/app.min.js")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("application/javascript", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    @Test
    fun `should handle files without extensions`() {
        // Given
        mockExchange.setupRequest("/static/files/README")

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        assertEquals(200, mockExchange.responseCode)
        assertEquals("application/octet-stream", mockExchange.getResponseHeader("Content-Type"))
        assertEquals("*", mockExchange.getResponseHeader("Access-Control-Allow-Origin"))
    }

    // Mock implementations for testing
    private class MockHttpExchange : HttpExchange() {
        private var responseCode = 200
        private val responseHeaders = mutableMapOf<String, String>()
        private val responseBody = ByteArrayOutputStream()
        private var requestPath = "/"

        fun setupRequest(path: String) {
            requestPath = path
        }

        override fun getRequestMethod(): String = "GET"

        override fun getRequestURI(): URI = URI.create(requestPath)

        override fun getRequestHeaders() = com.sun.net.httpserver.Headers()

        override fun getRequestBody() = ByteArrayInputStream(ByteArray(0))

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
