package dev.craftmind.agent.infrastructure.repository

import dev.craftmind.agent.domain.model.ConversationEntry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class InMemoryConversationRepositoryTest {

    private lateinit var repository: InMemoryConversationRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryConversationRepository()
    }

    @Test
    fun `should add conversation entry`() {
        // Given
        val entry = ConversationEntry(
            id = "test-id-1",
            role = "user",
            content = "Hello, AI!"
        )

        // When
        repository.addEntry(entry)

        // Then
        val allEntries = repository.getAllEntries()
        assertEquals(1, allEntries.size)
        assertEquals(entry, allEntries[0])
    }

    @Test
    fun `should add multiple conversation entries`() {
        // Given
        val entry1 = ConversationEntry(
            id = "test-id-1",
            role = "user",
            content = "Hello, AI!"
        )
        val entry2 = ConversationEntry(
            id = "test-id-2",
            role = "assistant",
            content = "Hello! How can I help you?"
        )

        // When
        repository.addEntry(entry1)
        repository.addEntry(entry2)

        // Then
        val allEntries = repository.getAllEntries()
        assertEquals(2, allEntries.size)
        assertTrue(allEntries.contains(entry1))
        assertTrue(allEntries.contains(entry2))
    }

    @Test
    fun `should get recent entries with limit`() {
        // Given
        val entry1 = ConversationEntry(
            id = "test-id-1",
            role = "user",
            content = "First message"
        )
        val entry2 = ConversationEntry(
            id = "test-id-2",
            role = "assistant",
            content = "Second message"
        )
        val entry3 = ConversationEntry(
            id = "test-id-3",
            role = "user",
            content = "Third message"
        )
        repository.addEntry(entry1)
        repository.addEntry(entry2)
        repository.addEntry(entry3)

        // When
        val recentEntries = repository.getRecentEntries(2)

        // Then
        assertEquals(2, recentEntries.size)
        assertEquals(entry2, recentEntries[0]) // Most recent
        assertEquals(entry3, recentEntries[1]) // Second most recent
    }

    @Test
    fun `should get all entries when limit is larger than total entries`() {
        // Given
        val entry1 = ConversationEntry(
            id = "test-id-1",
            role = "user",
            content = "First message"
        )
        val entry2 = ConversationEntry(
            id = "test-id-2",
            role = "assistant",
            content = "Second message"
        )
        repository.addEntry(entry1)
        repository.addEntry(entry2)

        // When
        val recentEntries = repository.getRecentEntries(10)

        // Then
        assertEquals(2, recentEntries.size)
        assertTrue(recentEntries.contains(entry1))
        assertTrue(recentEntries.contains(entry2))
    }

    @Test
    fun `should clear all conversation entries`() {
        // Given
        val entry1 = ConversationEntry(
            id = "test-id-1",
            role = "user",
            content = "Hello, AI!"
        )
        val entry2 = ConversationEntry(
            id = "test-id-2",
            role = "assistant",
            content = "Hello! How can I help you?"
        )
        repository.addEntry(entry1)
        repository.addEntry(entry2)

        // When
        repository.clear()

        // Then
        val allEntries = repository.getAllEntries()
        assertTrue(allEntries.isEmpty())
    }

    @Test
    fun `should return empty list when no entries exist`() {
        // When
        val allEntries = repository.getAllEntries()

        // Then
        assertTrue(allEntries.isEmpty())
    }

    @Test
    fun `should return empty list for recent entries when no entries exist`() {
        // When
        val recentEntries = repository.getRecentEntries(5)

        // Then
        assertTrue(recentEntries.isEmpty())
    }

    @Test
    fun `should maintain order of entries`() {
        // Given
        val entry1 = ConversationEntry(
            id = "test-id-1",
            role = "user",
            content = "First message"
        )
        val entry2 = ConversationEntry(
            id = "test-id-2",
            role = "assistant",
            content = "Second message"
        )
        val entry3 = ConversationEntry(
            id = "test-id-3",
            role = "user",
            content = "Third message"
        )

        // When
        repository.addEntry(entry1)
        repository.addEntry(entry2)
        repository.addEntry(entry3)

        // Then
        val allEntries = repository.getAllEntries()
        assertEquals(3, allEntries.size)
        assertEquals(entry1, allEntries[0])
        assertEquals(entry2, allEntries[1])
        assertEquals(entry3, allEntries[2])
    }
}