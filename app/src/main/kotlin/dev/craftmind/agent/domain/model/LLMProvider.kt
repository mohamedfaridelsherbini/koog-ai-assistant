package dev.craftmind.agent.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class LLMProvider {
    OLLAMA,
    OPENAI,
    ANTHROPIC
}

@Serializable
enum class LLMCapability {
    CHAT,
    CODE_GENERATION,
    TEXT_ANALYSIS,
    FILE_PROCESSING
}