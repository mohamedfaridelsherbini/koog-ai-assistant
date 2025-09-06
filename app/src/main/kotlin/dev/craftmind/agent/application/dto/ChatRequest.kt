package dev.craftmind.agent.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val message: String,
    val model: String? = null
)