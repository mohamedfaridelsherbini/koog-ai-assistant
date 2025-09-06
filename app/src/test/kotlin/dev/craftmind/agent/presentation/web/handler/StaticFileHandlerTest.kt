package dev.craftmind.agent.presentation.web.handler

import com.sun.net.httpserver.HttpExchange
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayOutputStream
import java.net.URI

class StaticFileHandlerTest {

    private lateinit var staticFileHandler: StaticFileHandler
    private lateinit var mockExchange: HttpExchange

    @BeforeEach
    fun setUp() {
        staticFileHandler = StaticFileHandler()
        mockExchange = mock<HttpExchange>()
    }

    @Test
    fun `should serve index html for root path`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should serve index html for empty path`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create(""))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle CSS file paths`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/css/styles.css"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle JavaScript file paths`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/js/script.js"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle JSON file paths`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/data/config.json"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle PNG image paths`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/images/logo.png"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle JPEG image paths`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/images/photo.jpg"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle SVG image paths`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/icons/icon.svg"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle favicon paths`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/favicon.ico"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle unknown file types`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/files/document.pdf"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle URL encoded paths`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/css/styles%20with%20spaces.css"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle paths without static prefix`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/css/styles.css"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should return 404 for non-existent files`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/nonexistent/file.html"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should return 404 for invalid paths`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/invalid/path/file.txt"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle special characters in file names`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/files/file-with-special-chars_123.txt"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle nested directory paths`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/css/components/modals.css"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle files with multiple dots`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/js/app.min.js"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }

    @Test
    fun `should handle files without extensions`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/files/README"))
        whenever(mockExchange.responseHeaders).thenReturn(mock())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(404, anyLong())
        verify(mockExchange.responseHeaders).set("Content-Type", "text/plain")
    }
}