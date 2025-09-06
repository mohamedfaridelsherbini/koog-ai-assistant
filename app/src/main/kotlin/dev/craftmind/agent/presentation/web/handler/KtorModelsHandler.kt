package dev.craftmind.agent.presentation.web.handler

import dev.craftmind.agent.infrastructure.ollama.OllamaClientInterface
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

class KtorModelsHandler(
    private val ollamaClient: OllamaClientInterface
) {
    
    suspend fun handleList(call: ApplicationCall) {
        try {
            val models = runBlocking { ollamaClient.listModels() }
            call.respond(models)
        } catch (e: Exception) {
            call.application.log.error("Error listing models", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to list models: ${e.message}"))
        }
    }
    
    suspend fun handlePull(call: ApplicationCall) {
        try {
            val request = call.receive<Map<String, String>>()
            val modelName = request["model"] ?: throw IllegalArgumentException("Model name is required")
            
            runBlocking { ollamaClient.pullModel(modelName) }
            call.respond(HttpStatusCode.OK, mapOf("message" to "Model $modelName pulled successfully"))
        } catch (e: Exception) {
            call.application.log.error("Error pulling model", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to pull model: ${e.message}"))
        }
    }
    
    suspend fun handleDelete(call: ApplicationCall) {
        try {
            val modelName = call.parameters["modelName"] ?: throw IllegalArgumentException("Model name is required")
            
            runBlocking { ollamaClient.deleteModel(modelName) }
            call.respond(HttpStatusCode.OK, mapOf("message" to "Model $modelName deleted successfully"))
        } catch (e: Exception) {
            call.application.log.error("Error deleting model", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete model: ${e.message}"))
        }
    }
}
