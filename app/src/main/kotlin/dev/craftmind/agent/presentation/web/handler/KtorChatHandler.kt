package dev.craftmind.agent.presentation.web.handler

import dev.craftmind.agent.application.service.ChatApplicationServiceInterface
import dev.craftmind.agent.application.dto.ChatRequest
import dev.craftmind.agent.application.dto.ChatResponse
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.Serializable

class KtorChatHandler(
    private val chatService: ChatApplicationServiceInterface
) {
    
    suspend fun handle(call: ApplicationCall) {
        try {
            val request = call.receive<ChatRequest>()
            
            val response = withTimeout(300000L) { // 5 minutes in milliseconds
                chatService.sendMessage(request)
            }
            
            call.respond(response)
        } catch (e: TimeoutCancellationException) {
            call.respond(HttpStatusCode.RequestTimeout, ErrorResponse("Request timeout"))
        } catch (e: Exception) {
            call.application.log.error("Error processing chat", e)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error processing chat: ${e.message}"))
        }
    }
}

@Serializable
data class ErrorResponse(
    val error: String
)
