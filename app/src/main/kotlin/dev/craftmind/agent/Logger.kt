/*
 * Logging utility for the Koog AI Agent
 */
package dev.craftmind.agent

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Simple logging utility with different log levels
 */
object Logger {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    private var logLevel = LogLevel.INFO
    private var enableDebugLogging = Config.ENABLE_DEBUG_LOGGING
    
    enum class LogLevel(val priority: Int) {
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3)
    }
    
    fun setLogLevel(level: LogLevel) {
        logLevel = level
    }
    
    fun setDebugLogging(enabled: Boolean) {
        enableDebugLogging = enabled
    }
    
    private fun log(level: LogLevel, message: String, throwable: Throwable? = null) {
        if (level.priority >= logLevel.priority) {
            val timestamp = LocalDateTime.now().format(dateTimeFormatter)
            val levelName = level.name.padEnd(5)
            val logMessage = "[$timestamp] [$levelName] $message"
            
            when (level) {
                LogLevel.DEBUG -> if (enableDebugLogging) println("🐛 $logMessage")
                LogLevel.INFO -> println("ℹ️  $logMessage")
                LogLevel.WARN -> println("⚠️  $logMessage")
                LogLevel.ERROR -> {
                    println("❌ $logMessage")
                    throwable?.let { 
                        println("   Stack trace: ${it.stackTraceToString()}")
                    }
                }
            }
        }
    }
    
    fun debug(message: String, throwable: Throwable? = null) {
        log(LogLevel.DEBUG, message, throwable)
    }
    
    fun info(message: String, throwable: Throwable? = null) {
        log(LogLevel.INFO, message, throwable)
    }
    
    fun warn(message: String, throwable: Throwable? = null) {
        log(LogLevel.WARN, message, throwable)
    }
    
    fun error(message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, message, throwable)
    }
    
    fun success(message: String) {
        println("✅ $message")
    }
    
    fun progress(message: String) {
        println("🔄 $message")
    }
    
    fun system(message: String) {
        println("🔧 $message")
    }
    
    fun performance(message: String) {
        if (Config.ENABLE_PERFORMANCE_METRICS) {
            println("📊 $message")
        }
    }
}
