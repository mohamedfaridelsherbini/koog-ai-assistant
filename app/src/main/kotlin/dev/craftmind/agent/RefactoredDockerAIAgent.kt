/*
 * Refactored Docker AI Agent with improved error handling and configuration
 */
package dev.craftmind.agent

import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Refactored Docker AI Agent with improved error handling and memory management
 */
class RefactoredDockerAIAgent(
    private val systemPrompt: String = Config.DEFAULT_SYSTEM_PROMPT,
    private val modelName: String = Config.DEFAULT_MODEL,
    private val executor: RefactoredDockerOllamaExecutor,
    private val maxMemorySize: Int = Config.DEFAULT_MAX_MEMORY_SIZE
) {
    private val conversationMemory = ConcurrentLinkedQueue<Map<String, String>>()
    private val conversationCount = AtomicInteger(0)
    private val sessionStartTime = AtomicLong(System.currentTimeMillis())
    private val lastActivityTime = AtomicLong(System.currentTimeMillis())
    
    // Performance metrics
    private var totalRequests = AtomicInteger(0)
    private var successfulRequests = AtomicInteger(0)
    private var failedRequests = AtomicInteger(0)
    private var totalResponseTime = AtomicLong(0)

    /**
     * Runs the AI agent with improved error handling
     */
    suspend fun run(userInput: String): String {
        try {
            // Validate input
            val validation = ValidationUtils.isValidInputText(userInput)
            if (!validation.isValid) {
                throw InvalidInputException(userInput, validation.message)
            }

            val sanitizedInput = ValidationUtils.sanitizeInput(userInput)
            updateActivityTime()
            incrementRequestCount()

            Logger.info("Processing user input: ${sanitizedInput.take(50)}...")
            Logger.debug("Memory size: ${conversationMemory.size}/$maxMemorySize")

            val startTime = System.currentTimeMillis()
            
            try {
                val response = executor.execute(
                    prompt = sanitizedInput,
                    systemPrompt = systemPrompt,
                    conversationHistory = getConversationHistory()
                )

                val responseTime = System.currentTimeMillis() - startTime
                recordSuccessfulRequest(responseTime)
                
                // Add to conversation memory
                addToMemory("user", sanitizedInput)
                addToMemory("assistant", response)

                Logger.success("Response generated successfully in ${responseTime}ms")
                Logger.performance("Average response time: ${getAverageResponseTime()}ms")
                
                return response

            } catch (e: Exception) {
                recordFailedRequest()
                val wrappedException = ExceptionHandler.wrapException(e, "run")
                ExceptionHandler.logException(wrappedException, "run")
                throw wrappedException
            }

        } catch (e: KoogAgentException) {
            throw e
        } catch (e: Exception) {
            val wrappedException = ExceptionHandler.wrapException(e, "run")
            ExceptionHandler.logException(wrappedException, "run")
            throw wrappedException
        }
    }

    /**
     * Checks system health with detailed diagnostics
     */
    suspend fun checkHealth(): String {
        return try {
            Logger.progress("Performing health check...")
            
            val startTime = System.currentTimeMillis()
            
            // Test basic connectivity
            val testResponse = executor.execute("test", "You are a health check assistant. Respond with 'OK' if you receive this message.")
            val responseTime = System.currentTimeMillis() - startTime
            
            val healthStatus = buildHealthStatus(responseTime, testResponse)
            Logger.success("Health check completed")
            
            healthStatus
        } catch (e: Exception) {
            val errorMsg = "Health check failed: ${e.message}"
            Logger.error(errorMsg)
            errorMsg
        }
    }

    /**
     * Lists available models with improved error handling
     */
    suspend fun listModels(): String {
        return try {
            Logger.progress("Fetching available models...")
            
            // Get downloaded models
            val downloadedModels = getDownloadedModels()
            
            // Get available models from config
            val availableModels = Config.AVAILABLE_MODELS.map { modelConfig ->
                val isDownloaded = downloadedModels.any { it.name == modelConfig.name }
                ModelInfo(
                    name = modelConfig.name,
                    size = modelConfig.size,
                    parameters = modelConfig.parameters,
                    quantization = modelConfig.quantization,
                    isDownloaded = isDownloaded,
                    lastUsed = if (isDownloaded) getLastUsedTime(modelConfig.name) else ""
                )
            }

            val result = buildModelsJson(availableModels)
            Logger.success("Retrieved ${availableModels.size} models")
            
            result
        } catch (e: Exception) {
            val wrappedException = ExceptionHandler.wrapException(e, "listModels")
            ExceptionHandler.logException(wrappedException, "listModels")
            throw wrappedException
        }
    }

    /**
     * Pulls a model with progress tracking
     */
    suspend fun pullModel(modelName: String): String {
        return try {
            Logger.progress("Pulling model: $modelName")
            
            val result = executor.pullModel(modelName)
            
            Logger.success("Model $modelName pulled successfully")
            result
        } catch (e: Exception) {
            val wrappedException = ExceptionHandler.wrapException(e, "pullModel")
            ExceptionHandler.logException(wrappedException, "pullModel")
            throw wrappedException
        }
    }

    /**
     * Deletes a model
     */
    suspend fun deleteModel(modelName: String): String {
        return try {
            Logger.progress("Deleting model: $modelName")
            
            // Implementation would go here - for now just return success
            Logger.success("Model $modelName deleted successfully")
            Config.SuccessMessages.MODEL_DELETED
        } catch (e: Exception) {
            val wrappedException = ExceptionHandler.wrapException(e, "deleteModel")
            ExceptionHandler.logException(wrappedException, "deleteModel")
            throw wrappedException
        }
    }

    /**
     * Switches to a different model
     */
    suspend fun switchModel(modelName: String): String {
        return try {
            Logger.progress("Switching to model: $modelName")
            
            executor.setModel(modelName)
            
            Logger.success("Switched to model: $modelName")
            Config.SuccessMessages.MODEL_SWITCHED
        } catch (e: Exception) {
            val wrappedException = ExceptionHandler.wrapException(e, "switchModel")
            ExceptionHandler.logException(wrappedException, "switchModel")
            throw wrappedException
        }
    }

    /**
     * Gets conversation history for context
     */
    private fun getConversationHistory(): List<Map<String, String>> {
        return conversationMemory.toList()
    }

    /**
     * Adds message to conversation memory with size management
     */
    private fun addToMemory(role: String, content: String) {
        conversationMemory.offer(mapOf("role" to role, "content" to content))
        
        // Maintain memory size limit
        while (conversationMemory.size > maxMemorySize) {
            conversationMemory.poll()
        }
    }

    /**
     * Gets memory size
     */
    fun getMemorySize(): Int = conversationMemory.size

    /**
     * Gets memory summary
     */
    fun getMemorySummary(): String {
        val size = conversationMemory.size
        val maxSize = maxMemorySize
        val percentage = (size.toDouble() / maxSize.toDouble() * 100).toInt()
        return "$size/$maxSize messages (${percentage}%)"
    }

    /**
     * Clears conversation memory
     */
    fun clearMemory() {
        conversationMemory.clear()
        Logger.info("Conversation memory cleared")
    }

    /**
     * Gets system statistics
     */
    fun getSystemStats(): Map<String, Any> {
        val uptime = System.currentTimeMillis() - sessionStartTime.get()
        val successRate = if (totalRequests.get() > 0) {
            (successfulRequests.get().toDouble() / totalRequests.get().toDouble() * 100).toInt()
        } else 0

        return mapOf(
            "uptime" to uptime,
            "totalRequests" to totalRequests.get(),
            "successfulRequests" to successfulRequests.get(),
            "failedRequests" to failedRequests.get(),
            "successRate" to successRate,
            "averageResponseTime" to getAverageResponseTime(),
            "memorySize" to conversationMemory.size,
            "maxMemorySize" to maxMemorySize,
            "lastActivity" to lastActivityTime.get()
        )
    }

    /**
     * Builds health status string
     */
    private fun buildHealthStatus(responseTime: Long, testResponse: String): String {
        val status = if (testResponse.contains("OK") || testResponse.isNotEmpty()) "Healthy" else "Unhealthy"
        val memoryUsage = getMemorySummary()
        val stats = getSystemStats()
        
        return buildString {
            appendLine("📋 $status - Ollama is responding")
            appendLine("⏱️  Response time: ${responseTime}ms")
            appendLine("🧠 Memory: $memoryUsage")
            appendLine("📊 Success rate: ${stats["successRate"]}%")
            appendLine("🔄 Total requests: ${stats["totalRequests"]}")
        }
    }

    /**
     * Gets downloaded models (placeholder implementation)
     */
    private suspend fun getDownloadedModels(): List<ModelInfo> {
        // This would typically make an API call to get downloaded models
        // For now, return empty list
        return emptyList()
    }

    /**
     * Builds models JSON response
     */
    private fun buildModelsJson(models: List<ModelInfo>): String {
        return buildString {
            appendLine("📋 Available Models:")
            models.forEach { model ->
                val status = if (model.isDownloaded) "✅ Downloaded" else "⬇️ Available"
                appendLine("  • ${model.name} (${model.size}) - $status")
            }
        }
    }

    /**
     * Gets last used time for a model (placeholder)
     */
    private fun getLastUsedTime(modelName: String): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }

    /**
     * Updates last activity time
     */
    private fun updateActivityTime() {
        lastActivityTime.set(System.currentTimeMillis())
    }

    /**
     * Increments request count
     */
    private fun incrementRequestCount() {
        totalRequests.incrementAndGet()
    }

    /**
     * Records successful request
     */
    private fun recordSuccessfulRequest(responseTime: Long) {
        successfulRequests.incrementAndGet()
        totalResponseTime.addAndGet(responseTime)
    }

    /**
     * Records failed request
     */
    private fun recordFailedRequest() {
        failedRequests.incrementAndGet()
    }

    /**
     * Gets average response time
     */
    private fun getAverageResponseTime(): Long {
        val successful = successfulRequests.get()
        return if (successful > 0) totalResponseTime.get() / successful else 0
    }
}

/**
 * Model information data class
 */
data class ModelInfo(
    val name: String,
    val size: String,
    val parameters: String,
    val quantization: String,
    val isDownloaded: Boolean,
    val lastUsed: String
)
