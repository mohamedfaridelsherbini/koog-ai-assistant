package dev.craftmind.agent.presentation.web.handler

import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpExchange
import dev.craftmind.agent.infrastructure.ollama.OllamaClient
import dev.craftmind.agent.presentation.web.handler.ErrorResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking
import java.net.http.HttpClient
import java.time.Duration

class ModelsHandler : HttpHandler {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()
    
    override fun handle(exchange: HttpExchange) {
        try {
            val path = exchange.requestURI.path
            
            when {
                path == "/api/models" && exchange.requestMethod == "GET" -> handleGetModels(exchange)
                path == "/api/models/all" && exchange.requestMethod == "GET" -> handleGetAllModels(exchange)
                path == "/api/models/pull" && exchange.requestMethod == "POST" -> handlePullModel(exchange)
                path == "/api/models/delete" && exchange.requestMethod == "POST" -> handleDeleteModel(exchange)
                path == "/api/models/switch" && exchange.requestMethod == "POST" -> handleSwitchModel(exchange)
                else -> sendError(exchange, 405, "Method not allowed")
            }
        } catch (e: Exception) {
            sendError(exchange, 500, "Error processing request: ${e.message}")
        }
    }
    
    private fun handleGetModels(exchange: HttpExchange) {
        try {
            val ollamaClient = OllamaClient(
                dev.craftmind.agent.config.OllamaConfig(),
                httpClient
            )
            
            val models = runBlocking {
                ollamaClient.listModels()
            }
            val response = ModelsResponse(models = models)
            
            val responseBody = json.encodeToString(response)
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            exchange.sendResponseHeaders(200, responseBody.length.toLong())
            exchange.responseBody.use { os ->
                os.write(responseBody.toByteArray())
            }
        } catch (e: Exception) {
            sendError(exchange, 500, "Error listing models: ${e.message}")
        }
    }
    
    private fun handlePullModel(exchange: HttpExchange) {
        try {
            val requestBody = String(exchange.requestBody.readAllBytes())
            val request = json.decodeFromString<PullModelRequest>(requestBody)
            
            // For now, just return success - in a real implementation,
            // you would call ollama pull API
            val response = PullModelResponse(
                success = true,
                message = "Model pull initiated for ${request.name}"
            )
            
            val responseBody = json.encodeToString(response)
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            exchange.sendResponseHeaders(200, responseBody.length.toLong())
            exchange.responseBody.use { os ->
                os.write(responseBody.toByteArray())
            }
        } catch (e: Exception) {
            sendError(exchange, 500, "Error pulling model: ${e.message}")
        }
    }
    
    private fun handleGetAllModels(exchange: HttpExchange) {
        try {
            val ollamaClient = OllamaClient(
                dev.craftmind.agent.config.OllamaConfig(),
                httpClient
            )
            
            val downloadedModels = runBlocking {
                ollamaClient.listModels()
            }
            
            // For now, return the same models as both downloaded and available
            // In a real implementation, you would differentiate between downloaded and available models
            val response = AllModelsResponse(
                downloadedModels = downloadedModels.map { modelName ->
                    ModelInfo(
                        name = modelName,
                        parameterSize = "8B", // Placeholder
                        size = "4.7GB", // Placeholder
                        quantizationLevel = "Q4_0" // Placeholder
                    )
                },
                availableModels = downloadedModels.map { modelName ->
                    AvailableModelInfo(
                        name = modelName,
                        parameterSize = "8B", // Placeholder
                        downloadSize = "4.7GB" // Placeholder
                    )
                }
            )
            
            val responseBody = json.encodeToString(response)
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            exchange.sendResponseHeaders(200, responseBody.length.toLong())
            exchange.responseBody.use { os ->
                os.write(responseBody.toByteArray())
            }
        } catch (e: Exception) {
            sendError(exchange, 500, "Error listing all models: ${e.message}")
        }
    }
    
    private fun handleDeleteModel(exchange: HttpExchange) {
        try {
            val requestBody = String(exchange.requestBody.readAllBytes())
            val request = json.decodeFromString<DeleteModelRequest>(requestBody)
            
            // For now, just return success - in a real implementation,
            // you would call ollama delete API
            val response = DeleteModelResponse(
                success = true,
                message = "Model ${request.name} deleted successfully"
            )
            
            val responseBody = json.encodeToString(response)
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            exchange.sendResponseHeaders(200, responseBody.length.toLong())
            exchange.responseBody.use { os ->
                os.write(responseBody.toByteArray())
            }
        } catch (e: Exception) {
            sendError(exchange, 500, "Error deleting model: ${e.message}")
        }
    }
    
    private fun handleSwitchModel(exchange: HttpExchange) {
        try {
            val requestBody = String(exchange.requestBody.readAllBytes())
            val request = json.decodeFromString<SwitchModelRequest>(requestBody)
            
            // For now, just return success - in a real implementation,
            // you would switch the active model
            val response = SwitchModelResponse(
                success = true,
                message = "Switched to ${request.modelName} successfully"
            )
            
            val responseBody = json.encodeToString(response)
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            exchange.sendResponseHeaders(200, responseBody.length.toLong())
            exchange.responseBody.use { os ->
                os.write(responseBody.toByteArray())
            }
        } catch (e: Exception) {
            sendError(exchange, 500, "Error switching model: ${e.message}")
        }
    }
    
    private fun sendError(exchange: HttpExchange, statusCode: Int, message: String) {
        val response = ErrorResponse(message)
        val responseBody = json.encodeToString(response)
        exchange.responseHeaders.set("Content-Type", "application/json")
        exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
        exchange.sendResponseHeaders(statusCode, responseBody.length.toLong())
        exchange.responseBody.use { os ->
            os.write(responseBody.toByteArray())
        }
    }
}

@Serializable
data class ModelsResponse(
    val models: List<String>
)

@Serializable
data class PullModelRequest(
    val name: String
)

@Serializable
data class PullModelResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class DeleteModelRequest(
    val name: String
)

@Serializable
data class DeleteModelResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class SwitchModelRequest(
    val modelName: String
)

@Serializable
data class SwitchModelResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class AllModelsResponse(
    val downloadedModels: List<ModelInfo>,
    val availableModels: List<AvailableModelInfo>
)

@Serializable
data class ModelInfo(
    val name: String,
    val parameterSize: String,
    val size: String,
    val quantizationLevel: String
)

@Serializable
data class AvailableModelInfo(
    val name: String,
    val parameterSize: String,
    val downloadSize: String
)
