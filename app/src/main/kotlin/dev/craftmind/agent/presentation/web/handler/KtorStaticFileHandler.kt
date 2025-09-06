package dev.craftmind.agent.presentation.web.handler

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.http.*
import java.io.InputStream

class KtorStaticFileHandler {
    
    suspend fun handle(call: ApplicationCall) {
        val path = call.parameters.getAll("path")?.joinToString("/") ?: ""
        val filePath = if (path.isEmpty()) "static/index.html" else "static/$path"
        
        try {
            val resource = javaClass.classLoader.getResourceAsStream(filePath)
            if (resource != null) {
                serveResource(call, resource, filePath)
            } else {
                // Try alternative paths
                val altPath = "/$filePath"
                val altResource = javaClass.getResourceAsStream(altPath)
                if (altResource != null) {
                    serveResource(call, altResource, filePath)
                } else {
                    call.respond(HttpStatusCode.NotFound, "File not found")
                }
            }
        } catch (e: Exception) {
            call.application.log.error("Error serving static file: $filePath", e)
            call.respond(HttpStatusCode.InternalServerError, "Error serving file")
        }
    }
    
    private suspend fun serveResource(call: ApplicationCall, resource: InputStream, filePath: String) {
        val fileName = filePath.substringAfterLast('/')
        val contentType = getContentType(fileName)
        
        call.response.headers.append("Content-Type", contentType.toString())
        call.response.headers.append("Access-Control-Allow-Origin", "*")
        
        resource.use { input ->
            val bytes = input.readBytes()
            call.respondBytes(bytes, contentType)
        }
    }
    
    private fun getContentType(fileName: String): ContentType {
        return when (fileName.substringAfterLast('.').lowercase()) {
            "html" -> ContentType.Text.Html
            "css" -> ContentType.Text.CSS
            "js" -> ContentType.Application.JavaScript
            "json" -> ContentType.Application.Json
            "png" -> ContentType.Image.PNG
            "jpg", "jpeg" -> ContentType.Image.JPEG
            "gif" -> ContentType.Image.GIF
            "svg" -> ContentType.Image.SVG
            "ico" -> ContentType.Image.Any
            "txt" -> ContentType.Text.Plain
            else -> ContentType.Application.OctetStream
        }
    }
}
