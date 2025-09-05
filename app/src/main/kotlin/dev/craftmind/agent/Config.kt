/*
 * Configuration constants and settings for the Koog AI Agent
 */
package dev.craftmind.agent

import java.time.Duration

/**
 * Application configuration constants
 */
object Config {
    // Server Configuration
    const val DEFAULT_PORT = 8080
    const val DEFAULT_HOST = "localhost"
    const val OLLAMA_DEFAULT_PORT = 11434
    const val OLLAMA_DEFAULT_HOST = "localhost"
    
    // HTTP Configuration
    val DEFAULT_REQUEST_TIMEOUT = Duration.ofMinutes(5)
    val DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(30)
    val MODEL_DOWNLOAD_TIMEOUT = Duration.ofMinutes(30)
    val HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(10)
    
    // Retry Configuration
    const val DEFAULT_MAX_RETRIES = 3
    const val RETRY_DELAY_MS = 1000L
    const val MAX_RETRY_DELAY_MS = 10000L
    
    // Memory Configuration
    const val DEFAULT_MAX_MEMORY_SIZE = 10
    const val CACHE_DURATION_SECONDS = 30L
    
    // Model Configuration
    const val DEFAULT_MODEL = "llama3.1:8b"
    const val DEFAULT_SYSTEM_PROMPT = "You are a helpful, intelligent, and friendly AI assistant running in a Docker container. You provide clear, accurate, and engaging responses. You can help with various tasks including coding, writing, analysis, and general knowledge questions. Remember the conversation context and refer back to previous messages when relevant. You can also help with file operations and document processing."
    
    // File I/O Configuration
    const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024 // 10MB
    const val ALLOWED_FILE_EXTENSIONS = "txt,md,json,xml,csv,log,py,java,kt,js,ts,html,css"
    
    // Performance Configuration
    const val MAX_CONCURRENT_REQUESTS = 5
    const val REQUEST_QUEUE_SIZE = 100
    const val THREAD_POOL_SIZE = 4
    
    // Logging Configuration
    const val LOG_LEVEL = "INFO"
    const val ENABLE_DEBUG_LOGGING = false
    const val ENABLE_PERFORMANCE_METRICS = true
    
    // Security Configuration
    const val ENABLE_CORS = true
    const val ALLOWED_ORIGINS = "*"
    const val ENABLE_RATE_LIMITING = false
    const val RATE_LIMIT_REQUESTS_PER_MINUTE = 60
    
    // Model List Configuration
    val AVAILABLE_MODELS = listOf(
        ModelConfig("llama3.1:8b", "4.6GB", "8B", "Q4_K_M"),
        ModelConfig("llama3.1:70b", "39GB", "70B", "Q4_K_M"),
        ModelConfig("llama3.2:1b", "1.2GB", "1.2B", "Q8_0"),
        ModelConfig("llama3.2:3b", "1.9GB", "3.2B", "Q4_K_M"),
        ModelConfig("llama3.2:11b", "6.2GB", "11B", "Q4_K_M"),
        ModelConfig("llama3.2:90b", "50GB", "90B", "Q4_K_M"),
        ModelConfig("codellama:7b", "3.6GB", "7B", "Q4_0"),
        ModelConfig("codellama:13b", "7.3GB", "13B", "Q4_0"),
        ModelConfig("codellama:34b", "19GB", "34B", "Q4_0"),
        ModelConfig("mistral:7b", "4.1GB", "7B", "Q4_K_M"),
        ModelConfig("mixtral:8x7b", "45GB", "8x7B", "Q4_K_M"),
        ModelConfig("neural-chat:7b", "4.1GB", "7B", "Q4_K_M"),
        ModelConfig("starling-lm:7b", "4.1GB", "7B", "Q4_K_M"),
        ModelConfig("openchat:7b", "4.1GB", "7B", "Q4_K_M"),
        ModelConfig("gemma:2b", "1.6GB", "2B", "Q4_K_M"),
        ModelConfig("gemma:7b", "4.8GB", "7B", "Q4_K_M"),
        ModelConfig("phi3:mini", "2.3GB", "3.8B", "Q4_K_M"),
        ModelConfig("phi3:medium", "7.4GB", "14B", "Q4_K_M"),
        ModelConfig("qwen2.5:7b", "4.4GB", "7B", "Q4_K_M"),
        ModelConfig("qwen2.5:14b", "8.6GB", "14B", "Q4_K_M"),
        ModelConfig("qwen2.5:32b", "19GB", "32B", "Q4_K_M"),
        ModelConfig("deepseek-coder:6.7b", "3.8GB", "6.7B", "Q4_K_M"),
        ModelConfig("deepseek-coder:33b", "19GB", "33B", "Q4_K_M"),
        ModelConfig("wizardcoder:7b", "4.1GB", "7B", "Q4_K_M"),
        ModelConfig("wizardcoder:13b", "7.3GB", "13B", "Q4_K_M"),
        ModelConfig("wizardcoder:34b", "19GB", "34B", "Q4_K_M")
    )
    
    // Error Messages
    object ErrorMessages {
        const val MODEL_NOT_FOUND = "Model not found"
        const val DOWNLOAD_FAILED = "Model download failed"
        const val CONNECTION_FAILED = "Connection to Ollama failed"
        const val INVALID_INPUT = "Invalid input provided"
        const val FILE_TOO_LARGE = "File size exceeds maximum allowed size"
        const val UNSUPPORTED_FILE_TYPE = "Unsupported file type"
        const val RATE_LIMIT_EXCEEDED = "Rate limit exceeded"
        const val INTERNAL_ERROR = "Internal server error"
    }
    
    // Success Messages
    object SuccessMessages {
        const val MODEL_DOWNLOADED = "Model downloaded successfully"
        const val MODEL_DELETED = "Model deleted successfully"
        const val MODEL_SWITCHED = "Model switched successfully"
        const val FILE_SAVED = "File saved successfully"
        const val HEALTH_CHECK_PASSED = "Health check passed"
    }
}

/**
 * Model configuration data class
 */
data class ModelConfig(
    val name: String,
    val size: String,
    val parameters: String,
    val quantization: String
)
