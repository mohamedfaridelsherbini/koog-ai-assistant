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
    fun `should handle root path request`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/"))
        whenever(mockExchange.responseHeaders).thenReturn(mock<com.sun.net.httpserver.Headers>())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(eq(404), anyLong())
        verify(mockExchange.responseHeaders).set(eq("Content-Type"), eq("text/plain"))
    }

    @Test
    fun `should handle empty path request`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create(""))
        whenever(mockExchange.responseHeaders).thenReturn(mock<com.sun.net.httpserver.Headers>())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(eq(404), anyLong())
        verify(mockExchange.responseHeaders).set(eq("Content-Type"), eq("text/plain"))
    }

    @Test
    fun `should handle CSS file path request`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/css/styles.css"))
        whenever(mockExchange.responseHeaders).thenReturn(mock<com.sun.net.httpserver.Headers>())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(eq(404), anyLong())
        verify(mockExchange.responseHeaders).set(eq("Content-Type"), eq("text/plain"))
    }

    @Test
    fun `should handle JavaScript file path request`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/static/js/script.js"))
        whenever(mockExchange.responseHeaders).thenReturn(mock<com.sun.net.httpserver.Headers>())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        // Since we don't have actual static files in test, expect 404
        verify(mockExchange).sendResponseHeaders(eq(404), anyLong())
        verify(mockExchange.responseHeaders).set(eq("Content-Type"), eq("text/plain"))
    }

    @Test
    fun `should handle invalid path request`() {
        // Given
        whenever(mockExchange.requestURI).thenReturn(URI.create("/invalid/path/file.txt"))
        whenever(mockExchange.responseHeaders).thenReturn(mock<com.sun.net.httpserver.Headers>())
        whenever(mockExchange.responseBody).thenReturn(ByteArrayOutputStream())

        // When
        staticFileHandler.handle(mockExchange)

        // Then
        verify(mockExchange).sendResponseHeaders(eq(404), anyLong())
        verify(mockExchange.responseHeaders).set(eq("Content-Type"), eq("text/plain"))
    }
}