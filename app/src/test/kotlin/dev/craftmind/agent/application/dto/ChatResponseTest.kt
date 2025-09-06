package dev.craftmind.agent.application.dto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ChatResponseTest {

    @Test
    fun `should create ChatResponse with valid parameters`() {
        // Given
        val response = "Hello! How can I help you today?"
        val model = "llama3.1:8b"

        // When
        val chatResponse = ChatResponse(response, model)

        // Then
        assertEquals(response, chatResponse.response)
        assertEquals(model, chatResponse.model)
    }

    @Test
    fun `should create ChatResponse with empty response`() {
        // Given
        val response = ""
        val model = "llama3.1:8b"

        // When
        val chatResponse = ChatResponse(response, model)

        // Then
        assertEquals("", chatResponse.response)
        assertEquals(model, chatResponse.model)
    }

    @Test
    fun `should create ChatResponse with long response`() {
        // Given
        val response = "This is a very long response that contains multiple sentences and should be handled properly by the ChatResponse class. It includes various characters and symbols like @#$%^&*() and numbers 1234567890."
        val model = "deepseek-coder:6.7b"

        // When
        val chatResponse = ChatResponse(response, model)

        // Then
        assertEquals(response, chatResponse.response)
        assertEquals(model, chatResponse.model)
    }

    @Test
    fun `should create ChatResponse with special characters`() {
        // Given
        val response = "Special chars: \n\t\r\"'`~!@#$%^&*()_+-=[]{}|;':\",./<>?"
        val model = "gemma:2b"

        // When
        val chatResponse = ChatResponse(response, model)

        // Then
        assertEquals(response, chatResponse.response)
        assertEquals(model, chatResponse.model)
    }

    @Test
    fun `should handle different model names`() {
        // Given
        val response = "Test response"
        val models = listOf(
            "llama3.1:8b",
            "deepseek-coder:6.7b",
            "gemma:2b",
            "phi3:mini",
            "llama3.2:3b"
        )

        // When & Then
        models.forEach { model ->
            val chatResponse = ChatResponse(response, model)
            assertEquals(response, chatResponse.response)
            assertEquals(model, chatResponse.model)
        }
    }

    @Test
    fun `should handle markdown in response`() {
        // Given
        val response = "Here's some **bold text** and *italic text* and `code`."
        val model = "llama3.1:8b"

        // When
        val chatResponse = ChatResponse(response, model)

        // Then
        assertEquals(response, chatResponse.response)
        assertEquals(model, chatResponse.model)
    }

    @Test
    fun `should handle code blocks in response`() {
        // Given
        val response = """
            Here's some code:
            ```kotlin
            fun main() {
                println("Hello, World!")
            }
            ```
        """.trimIndent()
        val model = "deepseek-coder:6.7b"

        // When
        val chatResponse = ChatResponse(response, model)

        // Then
        assertEquals(response, chatResponse.response)
        assertEquals(model, chatResponse.model)
    }
}
