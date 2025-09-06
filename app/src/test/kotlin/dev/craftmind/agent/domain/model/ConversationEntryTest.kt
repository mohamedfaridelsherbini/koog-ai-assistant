package dev.craftmind.agent.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ConversationEntryTest {

    @Test
    fun `should create ConversationEntry with valid parameters`() {
        // Given
        val id = "test-id-123"
        val role = "user"
        val content = "Hello, AI!"

        // When
        val entry = ConversationEntry(id, role, content)

        // Then
        assertEquals(id, entry.id)
        assertEquals(role, entry.role)
        assertEquals(content, entry.content)
        assertTrue(entry.timestamp > 0)
        assertNull(entry.model)
    }

    @Test
    fun `should create ConversationEntry with assistant role`() {
        // Given
        val id = "assistant-id-456"
        val role = "assistant"
        val content = "Hello! How can I help you today?"

        // When
        val entry = ConversationEntry(id, role, content)

        // Then
        assertEquals(id, entry.id)
        assertEquals(role, entry.role)
        assertEquals(content, entry.content)
        assertTrue(entry.timestamp > 0)
    }

    @Test
    fun `should create ConversationEntry with system role`() {
        // Given
        val id = "system-id-789"
        val role = "system"
        val content = "You are a helpful AI assistant."

        // When
        val entry = ConversationEntry(id, role, content)

        // Then
        assertEquals(id, entry.id)
        assertEquals(role, entry.role)
        assertEquals(content, entry.content)
        assertTrue(entry.timestamp > 0)
    }

    @Test
    fun `should create ConversationEntry with custom timestamp`() {
        // Given
        val id = "custom-timestamp-id"
        val role = "user"
        val content = "Test message"
        val timestamp = 1234567890L

        // When
        val entry = ConversationEntry(id, role, content, timestamp)

        // Then
        assertEquals(id, entry.id)
        assertEquals(role, entry.role)
        assertEquals(content, entry.content)
        assertEquals(timestamp, entry.timestamp)
    }

    @Test
    fun `should create ConversationEntry with model`() {
        // Given
        val id = "model-id"
        val role = "assistant"
        val content = "Response from specific model"
        val timestamp = System.currentTimeMillis()
        val model = "llama3.1:8b"

        // When
        val entry = ConversationEntry(id, role, content, timestamp, model)

        // Then
        assertEquals(id, entry.id)
        assertEquals(role, entry.role)
        assertEquals(content, entry.content)
        assertEquals(timestamp, entry.timestamp)
        assertEquals(model, entry.model)
    }

    @Test
    fun `should handle empty content`() {
        // Given
        val id = "empty-content-id"
        val role = "user"
        val content = ""

        // When
        val entry = ConversationEntry(id, role, content)

        // Then
        assertEquals(id, entry.id)
        assertEquals(role, entry.role)
        assertEquals("", entry.content)
        assertTrue(entry.timestamp > 0)
    }

    @Test
    fun `should handle long content`() {
        // Given
        val id = "long-content-id"
        val role = "assistant"
        val content = "This is a very long message that contains multiple sentences and should be handled properly by the ConversationEntry class. It includes various characters and symbols like @#$%^&*() and numbers 1234567890."

        // When
        val entry = ConversationEntry(id, role, content)

        // Then
        assertEquals(id, entry.id)
        assertEquals(role, entry.role)
        assertEquals(content, entry.content)
        assertTrue(entry.timestamp > 0)
    }

    @Test
    fun `should handle special characters in content`() {
        // Given
        val id = "special-chars-id"
        val role = "user"
        val content = "Special chars: \n\t\r\"'`~!@#$%^&*()_+-=[]{}|;':\",./<>?"

        // When
        val entry = ConversationEntry(id, role, content)

        // Then
        assertEquals(id, entry.id)
        assertEquals(role, entry.role)
        assertEquals(content, entry.content)
        assertTrue(entry.timestamp > 0)
    }
}