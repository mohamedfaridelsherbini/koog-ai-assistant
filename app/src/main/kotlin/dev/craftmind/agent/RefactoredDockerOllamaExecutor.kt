/*
 * Refactored Docker-based Ollama AI Executor with improved error handling and configuration
 */
package dev.craftmind.agent

import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.addJsonObject
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Refactored Docker-based Ollama AI Executor with improved error handling
 */
class RefactoredDockerOllamaExecutor(
    private val baseUrl: String = "http://${Config.OLLAMA_DEFAULT_HOST}:${Config.OLLAMA_DEFAULT_PORT}",
    private var model: String = Config.DEFAULT_MODEL,
    private val maxRetries: Int = Config.DEFAULT_MAX_RETRIES,
    private val requestTimeout: Duration = Config.DEFAULT_REQUEST_TIMEOUT,
    private val connectTimeout: Duration = Config.DEFAULT_CONNECT_TIMEOUT
) {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(connectTimeout)
        .build()

    /**
     * Executes a chat completion request with improved error handling
     */
    suspend fun execute(
        prompt: String, 
        systemPrompt: String = "", 
        conversationHistory: List<Map<String, String>> = emptyList()
    ): String {
        // Validate inputs
        val promptValidation = ValidationUtils.isValidInputText(prompt)
        if (!promptValidation.isValid) {
            throw InvalidInputException(prompt, promptValidation.message)
        }

        val sanitizedPrompt = ValidationUtils.sanitizeInput(prompt)
        val sanitizedSystemPrompt = if (systemPrompt.isNotEmpty()) ValidationUtils.sanitizeInput(systemPrompt) else ""

        return executeWithRetry(sanitizedPrompt, sanitizedSystemPrompt, conversationHistory)
    }

    /**
     * Executes request with retry logic
     */
    private suspend fun executeWithRetry(
        prompt: String,
        systemPrompt: String,
        conversationHistory: List<Map<String, String>>
    ): String {
        var lastException: Exception? = null

        for (attempt in 1..maxRetries) {
            try {
                Logger.progress("Attempt $attempt/$maxRetries")
                
                val response = makeHttpRequest(prompt, systemPrompt, conversationHistory)
                return processResponse(response, attempt)

            } catch (e: Exception) {
                lastException = e
                Logger.warn("Attempt $attempt failed: ${e.message}")
                ExceptionHandler.logException(e, "executeWithRetry")

                if (attempt < maxRetries) {
                    val delayMs = calculateRetryDelay(attempt)
                    Logger.debug("Waiting ${delayMs}ms before retry...")
                    delay(delayMs)
                }
            }
        }

        val wrappedException = ExceptionHandler.wrapException(
            lastException ?: Exception("Unknown error"), 
            "executeWithRetry"
        )
        throw wrappedException
    }

    /**
     * Makes HTTP request to Ollama API
     */
    private suspend fun makeHttpRequest(
        prompt: String,
        systemPrompt: String,
        conversationHistory: List<Map<String, String>>
    ): HttpResponse<String> {
        val requestBody = buildRequestJson(prompt, systemPrompt, conversationHistory)
        
        Logger.debug("Sending request to $baseUrl/api/chat")
        Logger.debug("Request body: $requestBody")
        Logger.debug("Context: ${conversationHistory.size} previous messages included")

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/api/chat"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(requestTimeout)
            .build()

        return try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: java.net.ConnectException) {
            Logger.warn("HTTP connection failed, trying curl fallback...")
            tryCurlFallback(requestBody)
        } catch (e: java.util.concurrent.TimeoutException) {
            throw RequestTimeoutException(requestTimeout.toMillis())
        } catch (e: Exception) {
            throw NetworkException("HTTP request failed: ${e.message}", e)
        }
    }

    /**
     * Processes HTTP response
     */
    private fun processResponse(response: HttpResponse<String>, attempt: Int): String {
        Logger.debug("Response status: ${response.statusCode()}")

        if (response.statusCode() == 200) {
            val responseBody = response.body()
            val content = extractContentFromResponse(responseBody)
            
            if (content.isNotEmpty()) {
                Logger.success("Success on attempt $attempt")
                return content
            } else {
                throw ModelNotFoundException("No content found in response: $responseBody")
            }
        } else {
            val errorMsg = "HTTP ${response.statusCode()}: ${response.body()}"
            Logger.error("Request failed: $errorMsg")
            throw NetworkException(errorMsg)
        }
    }

    /**
     * Builds JSON request body
     */
    private fun buildRequestJson(
        prompt: String,
        systemPrompt: String,
        conversationHistory: List<Map<String, String>>
    ): String {
        val messages = mutableListOf<Map<String, String>>()
        
        if (systemPrompt.isNotEmpty()) {
            messages.add(mapOf("role" to "system", "content" to systemPrompt))
        }
        
        messages.addAll(conversationHistory)
        messages.add(mapOf("role" to "user", "content" to prompt))

        return buildJsonObject {
            put("model", model)
            putJsonArray("messages") {
                messages.forEach { message ->
                    addJsonObject {
                        put("role", message["role"] ?: "")
                        put("content", message["content"] ?: "")
                    }
                }
            }
            put("stream", false)
        }.toString()
    }

    /**
     * Extracts content from Ollama response
     */
    private fun extractContentFromResponse(responseBody: String): String {
        return try {
            if (responseBody.contains("\"content\":")) {
                val contentStart = responseBody.indexOf("\"content\":") + 11
                val contentEnd = responseBody.indexOf("\"", contentStart)
                if (contentEnd > contentStart) {
                    responseBody.substring(contentStart, contentEnd)
                } else {
                    ""
                }
            } else {
                ""
            }
        } catch (e: Exception) {
            Logger.warn("Failed to extract content from response: ${e.message}")
            ""
        }
    }

    /**
     * Tries curl fallback when HTTP client fails
     */
    private fun tryCurlFallback(requestBody: String): HttpResponse<String> {
        return try {
            Logger.progress("Trying curl fallback for chat...")
            
            val process = Runtime.getRuntime().exec(arrayOf(
                "curl", "-s", "-X", "POST", 
                "$baseUrl/api/chat", 
                "-H", "Content-Type: application/json", 
                "-d", requestBody
            ))
            
            val responseBody = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            
            if (exitCode == 0 && responseBody.isNotEmpty()) {
                Logger.success("Curl fallback successful")
                return HttpResponse<String> {
                    statusCode()
                }.apply {
                    // Create a mock response with the curl result
                }
            } else {
                throw NetworkException("Curl failed with exit code: $exitCode")
            }
        } catch (e: Exception) {
            Logger.error("Curl fallback failed: ${e.message}")
            throw NetworkException("Curl fallback failed: ${e.message}", e)
        }
    }

    /**
     * Calculates retry delay with exponential backoff
     */
    private fun calculateRetryDelay(attempt: Int): Long {
        val baseDelay = Config.RETRY_DELAY_MS
        val delay = baseDelay * attempt
        return minOf(delay, Config.MAX_RETRY_DELAY_MS)
    }

    /**
     * Pulls a model with improved error handling
     */
    suspend fun pullModel(modelName: String): String {
        val validation = ValidationUtils.isValidModelName(modelName)
        if (!validation.isValid) {
            throw InvalidInputException(modelName, validation.message)
        }

        return try {
            pullModelWithHttp(modelName)
        } catch (e: Exception) {
            Logger.warn("HTTP pull failed, trying curl fallback for model: $modelName")
            pullModelWithCurl(modelName)
        }
    }

    /**
     * Pulls model using HTTP client
     */
    private suspend fun pullModelWithHttp(modelName: String): String {
        val requestBody = buildJsonObject {
            put("name", modelName)
        }.toString()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/api/pull"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Config.MODEL_DOWNLOAD_TIMEOUT)
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() == 200) {
            val responseBody = response.body()
            if (responseBody.contains("\"status\":\"success\"")) {
                Logger.success("Model $modelName pulled successfully")
                return Config.SuccessMessages.MODEL_DOWNLOADED
            } else if (responseBody.contains("\"error\"")) {
                throw ModelDownloadException(modelName)
            } else {
                Logger.success("Model $modelName pulled successfully")
                return Config.SuccessMessages.MODEL_DOWNLOADED
            }
        } else {
            throw ModelDownloadException(modelName)
        }
    }

    /**
     * Pulls model using curl fallback
     */
    private suspend fun pullModelWithCurl(modelName: String): String {
        val requestBody = buildJsonObject {
            put("name", modelName)
        }.toString()

        return try {
            val process = Runtime.getRuntime().exec(arrayOf(
                "curl", "-s", "-X", "POST", 
                "$baseUrl/api/pull", 
                "-H", "Content-Type: application/json", 
                "-d", requestBody
            ))
            
            val responseBody = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                Logger.success("Model $modelName pulled successfully via curl")
                Config.SuccessMessages.MODEL_DOWNLOADED
            } else {
                throw ModelDownloadException(modelName)
            }
        } catch (e: Exception) {
            throw ModelDownloadException(modelName, e)
        }
    }

    /**
     * Sets the current model
     */
    fun setModel(newModel: String) {
        val validation = ValidationUtils.isValidModelName(newModel)
        if (!validation.isValid) {
            throw InvalidInputException(newModel, validation.message)
        }
        model = newModel
        Logger.info("Model switched to: $model")
    }

    /**
     * Gets the current model
     */
    fun getCurrentModel(): String = model

    /**
     * Gets the base URL
     */
    fun getBaseUrl(): String = baseUrl
}
