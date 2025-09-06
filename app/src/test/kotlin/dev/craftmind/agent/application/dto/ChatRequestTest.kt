package dev.craftmind.agent.application.dto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ChatRequestTest {

    @Test
    fun `should create ChatRequest with message only`() {
        // Given
        val message = "Hello, AI!"

        // When
        val request = ChatRequest(message)

        // Then
        assertEquals(message, request.message)
        assertNull(request.model)
    }

    @Test
    fun `should create ChatRequest with message and model`() {
        // Given
        val message = "Hello, AI!"
        val model = "llama3.1:8b"

        // When
        val request = ChatRequest(message, model)

        // Then
        assertEquals(message, request.message)
        assertEquals(model, request.model)
    }

    @Test
    fun `should handle empty message`() {
        // Given
        val message = ""

        // When
        val request = ChatRequest(message)

        // Then
        assertEquals("", request.message)
        assertNull(request.model)
    }

    @Test
    fun `should handle long message`() {
        // Given
        val message = "This is a very long message that contains multiple sentences and should be handled properly by the ChatRequest class. It includes various characters and symbols like @#$%^&*() and numbers 1234567890."

        // When
        val request = ChatRequest(message)

        // Then
        assertEquals(message, request.message)
    }

    @Test
    fun `should handle special characters in message`() {
        // Given
        val message = "Special chars: \n\t\r\"'`~!@#$%^&*()_+-=[]{}|;':\",./<>?"

        // When
        val request = ChatRequest(message)

        // Then
        assertEquals(message, request.message)
    }

    @Test
    fun `should handle different model names`() {
        // Given
        val message = "Test message"
        val models = listOf(
            "llama3.1:8b",
            "deepseek-coder:6.7b",
            "gemma:2b",
            "phi3:mini",
            "llama3.2:3b"
        )

        // When & Then
        models.forEach { model ->
            val request = ChatRequest(message, model)
            assertEquals(message, request.message)
            assertEquals(model, request.model)
        }
    }

    @Test
    fun `should handle null model`() {
        // Given
        val message = "Test message"
        val model: String? = null

        // When
        val request = ChatRequest(message, model)

        // Then
        assertEquals(message, request.message)
        assertNull(request.model)
    }
}
