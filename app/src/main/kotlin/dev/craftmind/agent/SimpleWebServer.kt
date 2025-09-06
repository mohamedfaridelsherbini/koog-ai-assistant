package dev.craftmind.agent

import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpExchange
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.net.InetSocketAddress
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.concurrent.Executors

@Serializable
data class ChatRequest(val message: String)

@Serializable
data class ChatResponse(val response: String, val timestamp: String, val memorySize: Int)

@Serializable
data class FileOperationRequest(val operation: String, val filename: String, val content: String = "")

@Serializable
data class FileOperationResponse(val success: Boolean, val message: String, val data: String = "")

@Serializable
data class ModelOperationRequest(val modelName: String)

@Serializable
data class ModelOperationResponse(val success: Boolean, val message: String)

@Serializable
data class ModelsResponse(val models: String, val status: String = "success")

@Serializable
data class ModelInfo(
    val name: String,
    val size: String,
    val parameterSize: String,
    val quantizationLevel: String,
    val isDownloaded: Boolean,
    val downloadSize: String = ""
)

@Serializable
data class AllModelsResponse(
    val downloadedModels: List<ModelInfo>,
    val availableModels: List<ModelInfo>,
    val status: String = "success"
)

@Serializable
data class CurrentModelResponse(val currentModel: String, val status: String = "success")

@Serializable
data class ConversationExportRequest(val format: String = "json")

@Serializable
data class ConversationExportResponse(val success: Boolean, val data: String, val format: String)

@Serializable
data class ConversationStatsResponse(
    val totalMessages: Int,
    val sessionDuration: Long,
    val averageResponseTime: Double,
    val currentModel: String,
    val memorySize: Int,
    val sessionStartTime: Long
)

@Serializable
data class ConversationAnalyticsResponse(
    val success: Boolean,
    val data: String?,
    val error: String? = null
)

@Serializable
data class SystemMetricsResponse(
    val success: Boolean,
    val data: DockerAIAgent.SystemMetrics?,
    val error: String? = null
)

@Serializable
data class PerformanceMetricsResponse(
    val success: Boolean,
    val data: DockerAIAgent.PerformanceMetrics?,
    val error: String? = null
)

@Serializable
data class SystemHealthResponse(
    val success: Boolean,
    val data: String?,
    val error: String? = null
)

class SimpleWebServer(private val dockerAgent: DockerAIAgent) {
    
    fun start(port: Int = 8080) {
        val server = HttpServer.create(InetSocketAddress(port), 0)
        
        // Serve static files
        server.createContext("/", StaticFileHandler())
        server.createContext("/static", StaticFileHandler())
        
        // Health check endpoint
        server.createContext("/health", HealthHandler(dockerAgent))
        
        // Enhanced API endpoints
        server.createContext("/api/conversations", ConversationHandler())
        server.createContext("/api/settings", SettingsHandler())
        server.createContext("/api/export", ExportHandler())
        
        // API endpoints
        server.createContext("/api/chat", ChatHandler(dockerAgent))
        server.createContext("/api/memory", MemoryHandler(dockerAgent))
        server.createContext("/api/memory/clear", MemoryClearHandler(dockerAgent))
        server.createContext("/api/files/operation", FileOperationHandler(dockerAgent))
        server.createContext("/api/health", HealthHandler(dockerAgent))
        server.createContext("/api/models", ModelsHandler(dockerAgent))
        server.createContext("/api/models/all", AllModelsHandler(dockerAgent))
        server.createContext("/api/models/pull", ModelPullHandler(dockerAgent))
        server.createContext("/api/models/delete", ModelDeleteHandler(dockerAgent))
        server.createContext("/api/models/switch", ModelSwitchHandler(dockerAgent))
        server.createContext("/api/models/current", ModelCurrentHandler(dockerAgent))
        server.createContext("/api/conversation/stats", ConversationStatsHandler(dockerAgent))
        server.createContext("/api/conversation/export", ConversationExportHandler(dockerAgent))
        server.createContext("/api/conversation/analytics", ConversationAnalyticsHandler(dockerAgent))
        server.createContext("/api/conversation/reset", ConversationResetHandler(dockerAgent))
        server.createContext("/api/system/metrics", SystemMetricsHandler(dockerAgent))
        server.createContext("/api/system/performance", PerformanceMetricsHandler(dockerAgent))
        server.createContext("/api/system/health", SystemHealthHandler(dockerAgent))
        
        server.executor = Executors.newCachedThreadPool()
        server.start()
        
        println("🌐 Simple web server started on port $port")
        println("📱 Open your browser and go to: http://localhost:$port")
    }
    
    inner class StaticFileHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            val path = exchange.requestURI.path
            val filePath = if (path == "/") "/index.html" else if (path.startsWith("/static/")) path.substring(7) else path
            
            try {
                val resource = javaClass.classLoader.getResourceAsStream("static$filePath")
                if (resource != null) {
                    val contentType = when {
                        filePath.endsWith(".html") -> "text/html"
                        filePath.endsWith(".css") -> "text/css"
                        filePath.endsWith(".js") -> "application/javascript"
                        else -> "text/plain"
                    }
                    
                    exchange.responseHeaders.set("Content-Type", contentType)
                    exchange.sendResponseHeaders(200, 0)
                    
                    val output = exchange.responseBody
                    resource.use { input ->
                        input.copyTo(output)
                    }
                    output.close()
                } else {
                    exchange.sendResponseHeaders(404, -1)
                }
            } catch (e: Exception) {
                exchange.sendResponseHeaders(500, -1)
            }
        }
    }
    
    inner class ChatHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "POST") {
                sendError(exchange, 405, "Method not allowed")
                return
            }

            try {
                val requestBody = exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
                val request = Json.decodeFromString<ChatRequest>(requestBody)
                
                // Add timeout to prevent hanging
                val response = runBlocking {
                    withTimeout(30000) { // 30 second timeout
                        agent.run(request.message)
                    }
                }
                val chatResponse = ChatResponse(
                    response = response,
                    timestamp = LocalDateTime.now().toString(),
                    memorySize = agent.getMemorySize()
                )
                
                sendJsonResponse(exchange, chatResponse)
            } catch (e: TimeoutCancellationException) {
                sendError(exchange, 408, "Request timeout - AI model took too long to respond")
            } catch (e: Exception) {
                sendError(exchange, 500, "Error processing chat: ${e.message}")
            }
        }
    }
    
    inner class MemoryHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "GET") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            val memoryInfo = mapOf(
                "size" to agent.getMemorySize(),
                "summary" to agent.getMemorySummary()
            )
            sendJsonResponse(exchange, memoryInfo)
        }
    }
    
    inner class MemoryClearHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "POST") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            agent.clearMemory()
            val response = mapOf("success" to true, "message" to "Memory cleared")
            sendJsonResponse(exchange, response)
        }
    }
    
    inner class FileOperationHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "POST") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            try {
                val requestBody = exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
                val request = Json.decodeFromString<FileOperationRequest>(requestBody)
                
                val result = when (request.operation) {
                    "read" -> agent.readFile(request.filename)
                    "write" -> agent.writeFile(request.filename, request.content)
                    "list" -> agent.listFiles(request.filename)
                    else -> "Unknown operation: ${request.operation}"
                }
                
                val response = FileOperationResponse(
                    success = !result.startsWith("❌"),
                    message = result,
                    data = if (request.operation == "read" && !result.startsWith("❌")) result else ""
                )
                sendJsonResponse(exchange, response)
            } catch (e: Exception) {
                val response = FileOperationResponse(false, "Error: ${e.message}")
                sendJsonResponse(exchange, response)
            }
        }
    }
    
    inner class HealthHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "GET") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            val healthStatus = runBlocking { agent.checkHealth() }
            val response = mapOf("status" to healthStatus)
            sendJsonResponse(exchange, response)
        }
    }
    
    inner class ModelsHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "GET") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            try {
                // Try Java HTTP client first
                var responseBody = ""
                try {
                    val httpClient = java.net.http.HttpClient.newBuilder()
                        .connectTimeout(java.time.Duration.ofSeconds(30))
                        .build()
                    
                    val request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:11434/api/tags"))
                        .GET()
                        .timeout(java.time.Duration.ofSeconds(30))
                        .build()

                    val response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                    
                    if (response.statusCode() == 200) {
                        responseBody = response.body()
                    }
                } catch (e: Exception) {
                    println("⚠️ Java HTTP client failed: ${e.message}")
                }
                
                // If Java HTTP client failed or returned empty models, try curl as fallback
                if (responseBody.isEmpty() || responseBody.contains("\"models\":[]") || !responseBody.contains("\"name\":")) {
                    println("🔄 Trying curl fallback for models...")
                    try {
                        val process = Runtime.getRuntime().exec(arrayOf("curl", "-s", "http://localhost:11434/api/tags"))
                        val inputStream = process.inputStream
                        responseBody = inputStream.bufferedReader().use { it.readText() }
                        process.waitFor()
                        
                        if (process.exitValue() != 0) {
                            throw Exception("Curl failed with exit code: ${process.exitValue()}")
                        }
                        
                        println("✅ Curl fallback successful: $responseBody")
                    } catch (e: Exception) {
                        println("❌ Curl fallback failed: ${e.message}")
                        // Return fallback data
                        responseBody = """{"models":[{"name":"llama3.2:3b","model":"llama3.2:3b"}]}"""
                    }
                }
                
                // Send the response
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, 0)
                exchange.responseBody.use { output ->
                    output.write(responseBody.toByteArray(StandardCharsets.UTF_8))
                }
            } catch (e: Exception) {
                sendError(exchange, 500, "Error fetching models: ${e.message}")
            }
        }
    }
    
    inner class ModelPullHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "POST") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            try {
                val requestBody = exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
                val request = Json.decodeFromString<ModelOperationRequest>(requestBody)
                
                val result = runBlocking { agent.pullModelWithProgress(request.modelName) }
                val response = ModelOperationResponse(
                    success = result.startsWith("Error").not(),
                    message = result
                )
                sendJsonResponse(exchange, response)
            } catch (e: Exception) {
                val response = ModelOperationResponse(false, "Error: ${e.message}")
                sendJsonResponse(exchange, response)
            }
        }
    }
    
    inner class ModelDeleteHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "POST") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            try {
                val requestBody = exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
                val request = Json.decodeFromString<ModelOperationRequest>(requestBody)
                
                val result = runBlocking { agent.deleteModel(request.modelName) }
                val response = ModelOperationResponse(
                    success = result.startsWith("Error").not(),
                    message = result
                )
                sendJsonResponse(exchange, response)
            } catch (e: Exception) {
                val response = ModelOperationResponse(false, "Error: ${e.message}")
                sendJsonResponse(exchange, response)
            }
        }
    }
    
    inner class ModelSwitchHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "POST") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            try {
                val requestBody = exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
                val request = Json.decodeFromString<ModelOperationRequest>(requestBody)
                
                val result = runBlocking { agent.switchModel(request.modelName) }
                val response = ModelOperationResponse(
                    success = result.startsWith("Error").not(),
                    message = result
                )
                sendJsonResponse(exchange, response)
            } catch (e: Exception) {
                val response = ModelOperationResponse(false, "Error: ${e.message}")
                sendJsonResponse(exchange, response)
            }
        }
    }
    
    inner class ModelCurrentHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "GET") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            val currentModel = agent.getCurrentModel()
            val response = CurrentModelResponse(currentModel)
            sendJsonResponse(exchange, response)
        }
    }
    
    inner class ConversationStatsHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "GET") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            try {
                val stats = agent.getConversationStats()
                val response = ConversationStatsResponse(
                    totalMessages = stats["totalMessages"] as Int,
                    sessionDuration = stats["sessionDuration"] as Long,
                    averageResponseTime = stats["averageResponseTime"] as Double,
                    currentModel = stats["currentModel"] as String,
                    memorySize = stats["memorySize"] as Int,
                    sessionStartTime = stats["sessionStartTime"] as Long
                )
                sendJsonResponse(exchange, response)
            } catch (e: Exception) {
                val response = mapOf("success" to false, "message" to "Error getting conversation stats: ${e.message}")
                sendJsonResponse(exchange, response)
            }
        }
    }
    
    inner class ConversationExportHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "POST") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            try {
                val requestBody = exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
                val request = Json.decodeFromString<ConversationExportRequest>(requestBody)
                
                val exportData = agent.exportConversation(request.format)
                val response = ConversationExportResponse(
                    success = !exportData.startsWith("Error"),
                    data = exportData,
                    format = request.format
                )
                sendJsonResponse(exchange, response)
            } catch (e: Exception) {
                val response = ConversationExportResponse(false, "Error: ${e.message}", "json")
                sendJsonResponse(exchange, response)
            }
        }
    }
    
    inner class ConversationAnalyticsHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "GET") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            try {
                val analytics = agent.getConversationAnalytics()
                val response = ConversationAnalyticsResponse(
                    success = true,
                    data = Json.encodeToString(analytics.toString())
                )
                sendJsonResponse(exchange, response)
            } catch (e: Exception) {
                val response = ConversationAnalyticsResponse(
                    success = false,
                    data = null,
                    error = e.message ?: "Unknown error"
                )
                sendJsonResponse(exchange, response)
            }
        }
    }
    
    inner class ConversationResetHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "POST") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            try {
                agent.resetSession()
                val response = mapOf("success" to true, "message" to "Session reset successfully")
                sendJsonResponse(exchange, response)
            } catch (e: Exception) {
                val response = mapOf("success" to false, "message" to "Error resetting session: ${e.message}")
                sendJsonResponse(exchange, response)
            }
        }
    }
    
    inner class SystemMetricsHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "GET") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            try {
                val metrics = agent.getSystemMetrics()
                val response = SystemMetricsResponse(
                    success = true,
                    data = metrics
                )
                sendJsonResponse(exchange, response)
            } catch (e: Exception) {
                val response = SystemMetricsResponse(
                    success = false,
                    data = null,
                    error = e.message ?: "Unknown error"
                )
                sendJsonResponse(exchange, response)
            }
        }
    }
    
    inner class PerformanceMetricsHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "GET") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            try {
                val performance = agent.getPerformanceMetrics()
                val response = PerformanceMetricsResponse(
                    success = true,
                    data = performance
                )
                sendJsonResponse(exchange, response)
            } catch (e: Exception) {
                val response = PerformanceMetricsResponse(
                    success = false,
                    data = null,
                    error = e.message ?: "Unknown error"
                )
                sendJsonResponse(exchange, response)
            }
        }
    }
    
    inner class SystemHealthHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "GET") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            try {
                val health = agent.getSystemHealth()
                val response = SystemHealthResponse(
                    success = true,
                    data = Json.encodeToString(health.toString())
                )
                sendJsonResponse(exchange, response)
            } catch (e: Exception) {
                val response = SystemHealthResponse(
                    success = false,
                    data = null,
                    error = e.message ?: "Unknown error"
                )
                sendJsonResponse(exchange, response)
            }
        }
    }
    
    inner class AllModelsHandler(private val agent: DockerAIAgent) : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod != "GET") {
                sendError(exchange, 405, "Method not allowed")
                return
            }
            
            try {
                val result = runBlocking { agent.getAllModelsWithStatus() }
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
                exchange.sendResponseHeaders(200, result.length.toLong())
                exchange.responseBody.use { os ->
                    os.write(result.toByteArray())
                }
            } catch (e: Exception) {
                val errorResponse = """{"downloadedModels":[],"availableModels":[],"status":"error","message":"${e.message}"}"""
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.sendResponseHeaders(500, errorResponse.length.toLong())
                exchange.responseBody.use { os ->
                    os.write(errorResponse.toByteArray())
                }
            }
        }
    }
    
    private fun sendJsonResponse(exchange: HttpExchange, data: Any) {
        exchange.responseHeaders.set("Content-Type", "application/json")
        exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
        exchange.responseHeaders.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        exchange.responseHeaders.set("Access-Control-Allow-Headers", "Content-Type")
        
        val json = when (data) {
            is ChatResponse -> Json.encodeToString(ChatResponse.serializer(), data)
            is FileOperationResponse -> Json.encodeToString(FileOperationResponse.serializer(), data)
            is ModelOperationResponse -> Json.encodeToString(ModelOperationResponse.serializer(), data)
            is ModelsResponse -> Json.encodeToString(ModelsResponse.serializer(), data)
            is CurrentModelResponse -> Json.encodeToString(CurrentModelResponse.serializer(), data)
            is ConversationExportResponse -> Json.encodeToString(ConversationExportResponse.serializer(), data)
            is ConversationStatsResponse -> Json.encodeToString(ConversationStatsResponse.serializer(), data)
            is ConversationAnalyticsResponse -> Json.encodeToString(ConversationAnalyticsResponse.serializer(), data)
            is SystemMetricsResponse -> Json.encodeToString(SystemMetricsResponse.serializer(), data)
            is PerformanceMetricsResponse -> Json.encodeToString(PerformanceMetricsResponse.serializer(), data)
            is SystemHealthResponse -> Json.encodeToString(SystemHealthResponse.serializer(), data)
            is Map<*, *> -> {
                val map = data as Map<String, Any>
                buildJsonObject {
                    map.forEach { (key, value) ->
                        put(key, JsonPrimitive(value.toString()))
                    }
                }.toString()
            }
            else -> Json.encodeToString(data.toString())
        }
        
        exchange.sendResponseHeaders(200, json.length.toLong())
        exchange.responseBody.use { os ->
            os.write(json.toByteArray())
        }
    }
    
    private fun sendError(exchange: HttpExchange, statusCode: Int, message: String) {
        val errorResponse = mapOf("error" to message)
        exchange.responseHeaders.set("Content-Type", "application/json")
        exchange.sendResponseHeaders(statusCode, 0)
        exchange.responseBody.use { os ->
            os.write(Json.encodeToString(errorResponse).toByteArray())
        }
    }
}

// Enhanced API Handlers

@Serializable
data class ConversationData(
    val id: String,
    val title: String,
    val preview: String,
    val timestamp: String,
    val messageCount: Int,
    val messages: List<MessageData>
)

@Serializable
data class MessageData(
    val type: String,
    val content: String,
    val timestamp: String
)

@Serializable
data class SettingsData(
    val autoSave: Boolean,
    val showTimestamps: Boolean,
    val maxMemorySize: Int,
    val responseSpeed: String
)

class ConversationHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        when (exchange.requestMethod) {
            "GET" -> {
                // Return conversation list (placeholder - in real implementation, load from database)
                val conversations = listOf<ConversationData>()
                val response = mapOf("conversations" to conversations)
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, 0)
                exchange.responseBody.use { os ->
                    os.write(Json.encodeToString(response).toByteArray())
                }
            }
            "POST" -> {
                // Save conversation (placeholder)
                val response = mapOf("success" to true, "message" to "Conversation saved")
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, 0)
                exchange.responseBody.use { os ->
                    os.write(Json.encodeToString(response).toByteArray())
                }
            }
            else -> {
                exchange.sendResponseHeaders(405, 0)
            }
        }
    }
}

class SettingsHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        when (exchange.requestMethod) {
            "GET" -> {
                // Return default settings
                val settings = SettingsData(
                    autoSave = true,
                    showTimestamps = true,
                    maxMemorySize = 25,
                    responseSpeed = "balanced"
                )
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, 0)
                exchange.responseBody.use { os ->
                    os.write(Json.encodeToString(settings).toByteArray())
                }
            }
            "POST" -> {
                // Save settings (placeholder)
                val response = mapOf("success" to true, "message" to "Settings saved")
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, 0)
                exchange.responseBody.use { os ->
                    os.write(Json.encodeToString(response).toByteArray())
                }
            }
            else -> {
                exchange.sendResponseHeaders(405, 0)
            }
        }
    }
}

class ExportHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        when (exchange.requestMethod) {
            "GET" -> {
                val format = exchange.requestURI.query?.split("=")?.getOrNull(1) ?: "json"
                val response = when (format) {
                    "txt" -> "Text export not implemented yet"
                    "csv" -> "CSV export not implemented yet"
                    else -> "JSON export not implemented yet"
                }
                exchange.responseHeaders.set("Content-Type", "text/plain")
                exchange.sendResponseHeaders(200, 0)
                exchange.responseBody.use { os ->
                    os.write(response.toByteArray())
                }
            }
            else -> {
                exchange.sendResponseHeaders(405, 0)
            }
        }
    }
}
