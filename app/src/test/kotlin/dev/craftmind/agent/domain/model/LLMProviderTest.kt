package dev.craftmind.agent.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class LLMProviderTest {

    @Test
    fun `should have all expected LLM providers`() {
        // Given & When
        val providers = LLMProvider.values()

        // Then
        assertEquals(3, providers.size)
        assertTrue(providers.contains(LLMProvider.OLLAMA))
        assertTrue(providers.contains(LLMProvider.OPENAI))
        assertTrue(providers.contains(LLMProvider.ANTHROPIC))
    }

    @Test
    fun `should have correct OLLAMA provider`() {
        // Given
        val provider = LLMProvider.OLLAMA

        // When & Then
        assertEquals("OLLAMA", provider.name)
    }

    @Test
    fun `should have correct OPENAI provider`() {
        // Given
        val provider = LLMProvider.OPENAI

        // When & Then
        assertEquals("OPENAI", provider.name)
    }

    @Test
    fun `should have correct ANTHROPIC provider`() {
        // Given
        val provider = LLMProvider.ANTHROPIC

        // When & Then
        assertEquals("ANTHROPIC", provider.name)
    }

    @Test
    fun `should be able to get provider by name`() {
        // Given & When
        val ollama = LLMProvider.valueOf("OLLAMA")
        val openai = LLMProvider.valueOf("OPENAI")
        val anthropic = LLMProvider.valueOf("ANTHROPIC")

        // Then
        assertEquals(LLMProvider.OLLAMA, ollama)
        assertEquals(LLMProvider.OPENAI, openai)
        assertEquals(LLMProvider.ANTHROPIC, anthropic)
    }

    @Test
    fun `should throw exception for invalid provider name`() {
        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            LLMProvider.valueOf("INVALID_PROVIDER")
        }
    }
}