package dev.craftmind.agent.presentation.web

import dev.craftmind.agent.presentation.web.handler.ChatHandler
import dev.craftmind.agent.presentation.web.handler.ModelsHandler
import dev.craftmind.agent.presentation.web.handler.StaticFileHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

class WebServer(
    private val chatHandler: ChatHandler,
    private val port: Int
) {
    private var server: HttpServer? = null
    
    fun start() {
        server = HttpServer.create(InetSocketAddress(port), 0)
        server?.createContext("/api/chat", chatHandler)
        server?.createContext("/api/models", ModelsHandler())
        server?.createContext("/api/models/all", ModelsHandler())
        server?.createContext("/api/models/pull", ModelsHandler())
        server?.createContext("/api/models/delete", ModelsHandler())
        server?.createContext("/api/models/switch", ModelsHandler())
        server?.createContext("/", StaticFileHandler())
        server?.start()
        println("🌐 Web server started on port $port")
    }
    
    fun stop() {
        server?.stop(0)
        println("🛑 Web server stopped")
    }
}