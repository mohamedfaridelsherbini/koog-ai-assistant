package dev.craftmind.agent.domain.model

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ConversationEntry(
    val id: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String? = null
)

@Serializable
data class SystemMetrics(
    val memoryUsage: Long,
    val cpuUsage: Double,
    val diskUsage: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class PerformanceMetrics(
    val responseTime: Long,
    val tokensPerSecond: Double,
    val memoryUsed: Long,
    val timestamp: Long = System.currentTimeMillis()
)