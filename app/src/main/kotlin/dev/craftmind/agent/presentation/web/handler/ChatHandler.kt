package dev.craftmind.agent.presentation.web.handler

import dev.craftmind.agent.application.service.ChatApplicationServiceInterface
import dev.craftmind.agent.application.dto.ChatRequest
import dev.craftmind.agent.application.dto.ChatResponse
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpExchange
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import java.nio.charset.StandardCharsets
import java.time.Duration

class ChatHandler(
    private val chatService: ChatApplicationServiceInterface
) : HttpHandler {
    
    override fun handle(exchange: HttpExchange) {
        if (exchange.requestMethod != "POST") {
            sendError(exchange, 405, "Method not allowed")
            return
        }

        try {
            val requestBody = exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
            val request = Json.decodeFromString<ChatRequest>(requestBody)
            
            val response = runBlocking {
                withTimeout(300000L) { // 5 minutes in milliseconds
                    chatService.sendMessage(request)
                }
            }
            
            sendJsonResponse(exchange, response)
        } catch (e: TimeoutCancellationException) {
            sendError(exchange, 408, "Request timeout")
        } catch (e: Exception) {
            sendError(exchange, 500, "Error processing chat: ${e.message}")
        }
    }
    
    private fun sendJsonResponse(exchange: HttpExchange, data: ChatResponse) {
        exchange.responseHeaders.set("Content-Type", "application/json")
        exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
        
        val json = Json.encodeToString(data)
        exchange.sendResponseHeaders(200, json.length.toLong())
        exchange.responseBody.use { os ->
            os.write(json.toByteArray())
        }
    }
    
    private fun sendError(exchange: HttpExchange, statusCode: Int, message: String) {
        val errorResponse = ErrorResponse(message)
        exchange.responseHeaders.set("Content-Type", "application/json")
        exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
        exchange.sendResponseHeaders(statusCode, 0)
        exchange.responseBody.use { os ->
            os.write(Json.encodeToString(errorResponse).toByteArray())
        }
    }
}

@Serializable
data class ErrorResponse(
    val error: String
)