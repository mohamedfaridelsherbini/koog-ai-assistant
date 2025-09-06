package dev.craftmind.agent.presentation.web

import dev.craftmind.agent.config.ApplicationConfig
import dev.craftmind.agent.presentation.web.handler.KtorChatHandler
import dev.craftmind.agent.presentation.web.handler.KtorModelsHandler
import dev.craftmind.agent.presentation.web.handler.KtorStaticFileHandler
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import org.slf4j.event.Level

class KtorApplication(
    private val config: ApplicationConfig,
    private val chatHandler: KtorChatHandler,
    private val modelsHandler: KtorModelsHandler,
    private val staticFileHandler: KtorStaticFileHandler
) {
    
    fun start() {
        embeddedServer(Netty, port = config.server.port) {
            configureApplication()
        }.start(wait = true)
    }
    
    private fun Application.configureApplication() {
        // Install Koin
        install(Koin) {
            modules(dev.craftmind.agent.di.appModule)
        }
        
        // Install CORS
        install(CORS) {
            anyHost()
            allowHeader("Content-Type")
            allowHeader("Authorization")
            allowMethod(io.ktor.http.HttpMethod.Options)
            allowMethod(io.ktor.http.HttpMethod.Post)
            allowMethod(io.ktor.http.HttpMethod.Get)
            allowMethod(io.ktor.http.HttpMethod.Delete)
        }
        
        // Install Content Negotiation
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        
        // Install Call Logging
        install(CallLogging) {
            level = Level.INFO
            format { call ->
                val status = call.response.status()
                val httpMethod = call.request.httpMethod.value
                val uri = call.request.uri
                "$httpMethod $uri - $status"
            }
        }
        
        // Install Status Pages
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                call.application.log.error("Unhandled exception", cause)
                call.respond(io.ktor.http.HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
            }
        }
        
        // Configure routing
        configureRouting()
    }
    
    private fun Application.configureRouting() {
        routing {
            // API routes
            route("/api") {
                // Chat endpoint
                post("/chat") {
                    chatHandler.handle(call)
                }
                
                // Models endpoint
                route("/models") {
                    get {
                        modelsHandler.handleList(call)
                    }
                    post("/pull") {
                        modelsHandler.handlePull(call)
                    }
                    delete("/{modelName}") {
                        modelsHandler.handleDelete(call)
                    }
                }
            }
            
            // Static files
            get("/{path...}") {
                staticFileHandler.handle(call)
            }
        }
    }
}
