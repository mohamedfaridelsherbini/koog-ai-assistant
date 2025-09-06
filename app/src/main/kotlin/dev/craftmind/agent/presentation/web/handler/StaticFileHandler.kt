package dev.craftmind.agent.presentation.web.handler

import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpExchange
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class StaticFileHandler : HttpHandler {
    
    override fun handle(exchange: HttpExchange) {
        val requestPath = exchange.requestURI.path
        val decodedPath = URLDecoder.decode(requestPath, StandardCharsets.UTF_8)
        
        // Default to index.html for root path
        val filePath = if (decodedPath == "/" || decodedPath.isEmpty()) {
            "static/index.html"
        } else if (decodedPath.startsWith("/static/")) {
            // Remove the leading /static/ since we're already in the static directory
            "static${decodedPath.substring(7)}"
        } else {
            "static$decodedPath"
        }
        
        try {
            val resource = javaClass.classLoader.getResourceAsStream(filePath)
            if (resource != null) {
                serveResource(exchange, resource, filePath)
            } else {
                // Try alternative paths
                val altPath = "/$filePath"
                val altResource = javaClass.getResourceAsStream(altPath)
                if (altResource != null) {
                    serveResource(exchange, altResource, filePath)
                } else {
                    send404(exchange)
                }
            }
        } catch (e: Exception) {
            send404(exchange)
        }
    }
    
    private fun serveResource(exchange: HttpExchange, resource: java.io.InputStream, filePath: String) {
        val fileName = filePath.substringAfterLast('/')
        val contentType = getContentType(fileName)
        
        exchange.responseHeaders.set("Content-Type", contentType)
        exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
        exchange.sendResponseHeaders(200, 0) // We don't know the length for resources
        
        resource.use { input ->
            exchange.responseBody.use { output ->
                input.copyTo(output)
            }
        }
    }
    
    private fun serveFile(exchange: HttpExchange, file: File) {
        val contentType = getContentType(file.name)
        
        exchange.responseHeaders.set("Content-Type", contentType)
        exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
        exchange.sendResponseHeaders(200, file.length())
        
        FileInputStream(file).use { input ->
            exchange.responseBody.use { output ->
                input.copyTo(output)
            }
        }
    }
    
    private fun send404(exchange: HttpExchange) {
        val response = "404 Not Found"
        exchange.responseHeaders.set("Content-Type", "text/plain")
        exchange.sendResponseHeaders(404, response.length.toLong())
        exchange.responseBody.use { os ->
            os.write(response.toByteArray())
        }
    }
    
    private fun getContentType(fileName: String): String {
        return when (fileName.substringAfterLast('.')) {
            "html" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "json" -> "application/json"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "svg" -> "image/svg+xml"
            "ico" -> "image/x-icon"
            else -> "application/octet-stream"
        }
    }
}
