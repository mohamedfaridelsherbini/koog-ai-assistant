/*
 * Unit tests for Config
 */
package dev.craftmind.agent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConfigTest {

    @Test
    fun testDefaultPort() {
        assertEquals(8080, Config.DEFAULT_PORT)
    }

    @Test
    fun testDefaultHost() {
        assertEquals("localhost", Config.DEFAULT_HOST)
    }

    @Test
    fun testOllamaDefaultPort() {
        assertEquals(11434, Config.OLLAMA_DEFAULT_PORT)
    }

    @Test
    fun testOllamaDefaultHost() {
        assertEquals("localhost", Config.OLLAMA_DEFAULT_HOST)
    }

    @Test
    fun testDefaultRequestTimeout() {
        assertNotNull(Config.DEFAULT_REQUEST_TIMEOUT)
    }

    @Test
    fun testDefaultConnectTimeout() {
        assertNotNull(Config.DEFAULT_CONNECT_TIMEOUT)
    }

    @Test
    fun testModelDownloadTimeout() {
        assertNotNull(Config.MODEL_DOWNLOAD_TIMEOUT)
    }

    @Test
    fun testDefaultMaxRetries() {
        assertEquals(3, Config.DEFAULT_MAX_RETRIES)
    }

    @Test
    fun testRetryDelayMs() {
        assertEquals(1000L, Config.RETRY_DELAY_MS)
    }

    @Test
    fun testMaxRetryDelayMs() {
        assertEquals(10000L, Config.MAX_RETRY_DELAY_MS)
    }

    @Test
    fun testDefaultMaxMemorySize() {
        assertEquals(10, Config.DEFAULT_MAX_MEMORY_SIZE)
    }

    @Test
    fun testCacheDurationSeconds() {
        assertEquals(30L, Config.CACHE_DURATION_SECONDS)
    }

    @Test
    fun testDefaultModel() {
        assertEquals("llama3.1:8b", Config.DEFAULT_MODEL)
    }

    @Test
    fun testDefaultSystemPrompt() {
        assertTrue(Config.DEFAULT_SYSTEM_PROMPT.isNotEmpty())
        assertTrue(Config.DEFAULT_SYSTEM_PROMPT.contains("helpful"))
    }

    @Test
    fun testMaxFileSizeBytes() {
        assertEquals(10 * 1024 * 1024, Config.MAX_FILE_SIZE_BYTES)
    }

    @Test
    fun testAllowedFileExtensions() {
        assertTrue(Config.ALLOWED_FILE_EXTENSIONS.contains("txt"))
        assertTrue(Config.ALLOWED_FILE_EXTENSIONS.contains("json"))
        assertTrue(Config.ALLOWED_FILE_EXTENSIONS.contains("py"))
    }

    @Test
    fun testMaxConcurrentRequests() {
        assertEquals(5, Config.MAX_CONCURRENT_REQUESTS)
    }

    @Test
    fun testRequestQueueSize() {
        assertEquals(100, Config.REQUEST_QUEUE_SIZE)
    }

    @Test
    fun testThreadPoolSize() {
        assertEquals(4, Config.THREAD_POOL_SIZE)
    }

    @Test
    fun testLogLevel() {
        assertEquals("INFO", Config.LOG_LEVEL)
    }

    @Test
    fun testEnableDebugLogging() {
        assertTrue(Config.ENABLE_DEBUG_LOGGING == false)
    }

    @Test
    fun testEnablePerformanceMetrics() {
        assertTrue(Config.ENABLE_PERFORMANCE_METRICS == true)
    }

    @Test
    fun testEnableCors() {
        assertTrue(Config.ENABLE_CORS == true)
    }

    @Test
    fun testAllowedOrigins() {
        assertEquals("*", Config.ALLOWED_ORIGINS)
    }

    @Test
    fun testEnableRateLimiting() {
        assertTrue(Config.ENABLE_RATE_LIMITING == false)
    }

    @Test
    fun testRateLimitRequestsPerMinute() {
        assertEquals(60, Config.RATE_LIMIT_REQUESTS_PER_MINUTE)
    }

    @Test
    fun testAvailableModels() {
        assertTrue(Config.AVAILABLE_MODELS.isNotEmpty())
        assertTrue(Config.AVAILABLE_MODELS.any { it.name == "llama3.1:8b" })
        assertTrue(Config.AVAILABLE_MODELS.any { it.name == "llama3.2:3b" })
    }

    @Test
    fun testErrorMessages() {
        assertEquals("Model not found", Config.ErrorMessages.MODEL_NOT_FOUND)
        assertEquals("Model download failed", Config.ErrorMessages.DOWNLOAD_FAILED)
        assertEquals("Connection to Ollama failed", Config.ErrorMessages.CONNECTION_FAILED)
        assertEquals("Invalid input provided", Config.ErrorMessages.INVALID_INPUT)
        assertEquals("File size exceeds maximum allowed size", Config.ErrorMessages.FILE_TOO_LARGE)
        assertEquals("Unsupported file type", Config.ErrorMessages.UNSUPPORTED_FILE_TYPE)
        assertEquals("Rate limit exceeded", Config.ErrorMessages.RATE_LIMIT_EXCEEDED)
        assertEquals("Internal server error", Config.ErrorMessages.INTERNAL_ERROR)
    }

    @Test
    fun testSuccessMessages() {
        assertEquals("Model downloaded successfully", Config.SuccessMessages.MODEL_DOWNLOADED)
        assertEquals("Model deleted successfully", Config.SuccessMessages.MODEL_DELETED)
        assertEquals("Model switched successfully", Config.SuccessMessages.MODEL_SWITCHED)
        assertEquals("File saved successfully", Config.SuccessMessages.FILE_SAVED)
        assertEquals("Health check passed", Config.SuccessMessages.HEALTH_CHECK_PASSED)
    }

    @Test
    fun testModelConfig() {
        val modelConfig = ModelConfig("test-model", "1GB", "7B", "Q4_K_M")
        assertEquals("test-model", modelConfig.name)
        assertEquals("1GB", modelConfig.size)
        assertEquals("7B", modelConfig.parameters)
        assertEquals("Q4_K_M", modelConfig.quantization)
    }
}
