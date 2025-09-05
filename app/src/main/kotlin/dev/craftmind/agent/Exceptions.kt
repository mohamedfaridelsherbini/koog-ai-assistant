/*
 * Custom exceptions for the Koog AI Agent
 */
package dev.craftmind.agent

/**
 * Base exception for all Koog AI Agent related errors
 */
open class KoogAgentException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Model related exceptions
 */
class ModelNotFoundException(modelName: String) : KoogAgentException("Model '$modelName' not found")
class ModelDownloadException(modelName: String, cause: Throwable? = null) : KoogAgentException("Failed to download model '$modelName'", cause)
class ModelDeleteException(modelName: String, cause: Throwable? = null) : KoogAgentException("Failed to delete model '$modelName'", cause)
class ModelSwitchException(modelName: String, cause: Throwable? = null) : KoogAgentException("Failed to switch to model '$modelName'", cause)

/**
 * Network related exceptions
 */
class NetworkException(message: String, cause: Throwable? = null) : KoogAgentException("Network error: $message", cause)
class ConnectionTimeoutException(timeout: Long) : KoogAgentException("Connection timeout after ${timeout}ms")
class RequestTimeoutException(timeout: Long) : KoogAgentException("Request timeout after ${timeout}ms")

/**
 * Validation related exceptions
 */
class ValidationException(message: String) : KoogAgentException("Validation error: $message")
class InvalidInputException(input: String, reason: String) : KoogAgentException("Invalid input '$input': $reason")

/**
 * File I/O related exceptions
 */
class FileOperationException(message: String, cause: Throwable? = null) : KoogAgentException("File operation error: $message", cause)
class FileTooLargeException(fileSize: Long, maxSize: Long) : KoogAgentException("File size $fileSize bytes exceeds maximum allowed size $maxSize bytes")
class UnsupportedFileTypeException(fileType: String) : KoogAgentException("Unsupported file type: $fileType")

/**
 * Configuration related exceptions
 */
class ConfigurationException(message: String, cause: Throwable? = null) : KoogAgentException("Configuration error: $message", cause)

/**
 * Rate limiting exceptions
 */
class RateLimitExceededException(limit: Int, timeWindow: String) : KoogAgentException("Rate limit exceeded: $limit requests per $timeWindow")

/**
 * Memory related exceptions
 */
class MemoryException(message: String, cause: Throwable? = null) : KoogAgentException("Memory error: $message", cause)
class MemoryLimitExceededException(currentSize: Int, maxSize: Int) : KoogAgentException("Memory limit exceeded: $currentSize/$maxSize messages")

/**
 * System related exceptions
 */
class SystemException(message: String, cause: Throwable? = null) : KoogAgentException("System error: $message", cause)
class HealthCheckException(message: String, cause: Throwable? = null) : KoogAgentException("Health check failed: $message", cause)

/**
 * Web server related exceptions
 */
class WebServerException(message: String, cause: Throwable? = null) : KoogAgentException("Web server error: $message", cause)
class PortAlreadyInUseException(port: Int) : KoogAgentException("Port $port is already in use")

/**
 * Utility functions for exception handling
 */
object ExceptionHandler {
    
    /**
     * Handles exceptions and returns appropriate error messages
     */
    fun handleException(exception: Throwable): String {
        return when (exception) {
            is ModelNotFoundException -> Config.ErrorMessages.MODEL_NOT_FOUND
            is ModelDownloadException -> Config.ErrorMessages.DOWNLOAD_FAILED
            is NetworkException -> Config.ErrorMessages.CONNECTION_FAILED
            is ValidationException -> Config.ErrorMessages.INVALID_INPUT
            is FileTooLargeException -> Config.ErrorMessages.FILE_TOO_LARGE
            is UnsupportedFileTypeException -> Config.ErrorMessages.UNSUPPORTED_FILE_TYPE
            is RateLimitExceededException -> Config.ErrorMessages.RATE_LIMIT_EXCEEDED
            else -> Config.ErrorMessages.INTERNAL_ERROR
        }
    }
    
    /**
     * Logs exception with appropriate level
     */
    fun logException(exception: Throwable, context: String = "") {
        when (exception) {
            is ValidationException -> Logger.warn("Validation error in $context: ${exception.message}")
            is NetworkException -> Logger.error("Network error in $context: ${exception.message}", exception)
            is ModelNotFoundException -> Logger.warn("Model not found in $context: ${exception.message}")
            is FileOperationException -> Logger.error("File operation error in $context: ${exception.message}", exception)
            is SystemException -> Logger.error("System error in $context: ${exception.message}", exception)
            else -> Logger.error("Unexpected error in $context: ${exception.message}", exception)
        }
    }
    
    /**
     * Wraps exceptions in appropriate KoogAgentException
     */
    fun wrapException(exception: Throwable, context: String): KoogAgentException {
        return when (exception) {
            is KoogAgentException -> exception
            is java.net.ConnectException -> NetworkException("Connection failed: ${exception.message}", exception)
            is java.net.SocketTimeoutException -> ConnectionTimeoutException(Config.DEFAULT_CONNECT_TIMEOUT.toMillis())
            is java.util.concurrent.TimeoutException -> RequestTimeoutException(Config.DEFAULT_REQUEST_TIMEOUT.toMillis())
            is java.io.FileNotFoundException -> FileOperationException("File not found: ${exception.message}", exception)
            is java.io.IOException -> FileOperationException("I/O error: ${exception.message}", exception)
            is IllegalArgumentException -> ValidationException("Invalid argument: ${exception.message}")
            is IllegalStateException -> SystemException("Invalid state: ${exception.message}", exception)
            else -> KoogAgentException("Unexpected error in $context: ${exception.message}", exception)
        }
    }
}
