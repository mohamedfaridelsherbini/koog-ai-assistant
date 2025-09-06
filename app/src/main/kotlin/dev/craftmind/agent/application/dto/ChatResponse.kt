package dev.craftmind.agent.application.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val response: String,
    val model: String
)