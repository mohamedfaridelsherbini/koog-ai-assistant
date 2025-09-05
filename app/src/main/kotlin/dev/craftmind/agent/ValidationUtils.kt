/*
 * Input validation utilities for the Koog AI Agent
 */
package dev.craftmind.agent

import java.io.File
import java.util.regex.Pattern

/**
 * Input validation utilities
 */
object ValidationUtils {
    
    // Model name validation
    private val MODEL_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._:-]+$")
    private val MAX_MODEL_NAME_LENGTH = 100
    
    // File validation
    private val ALLOWED_EXTENSIONS = Config.ALLOWED_FILE_EXTENSIONS.split(",").map { it.trim() }
    private val MAX_FILE_SIZE = Config.MAX_FILE_SIZE_BYTES
    
    // URL validation
    private val URL_PATTERN = Pattern.compile("^https?://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?$")
    
    /**
     * Validates model name
     */
    fun isValidModelName(modelName: String?): ValidationResult {
        if (modelName.isNullOrBlank()) {
            return ValidationResult(false, "Model name cannot be empty")
        }
        
        if (modelName.length > MAX_MODEL_NAME_LENGTH) {
            return ValidationResult(false, "Model name is too long (max $MAX_MODEL_NAME_LENGTH characters)")
        }
        
        if (!MODEL_NAME_PATTERN.matcher(modelName).matches()) {
            return ValidationResult(false, "Model name contains invalid characters")
        }
        
        return ValidationResult(true, "Valid model name")
    }
    
    /**
     * Validates file for upload/processing
     */
    fun isValidFile(file: File): ValidationResult {
        if (!file.exists()) {
            return ValidationResult(false, "File does not exist")
        }
        
        if (!file.isFile()) {
            return ValidationResult(false, "Path is not a file")
        }
        
        if (file.length() > MAX_FILE_SIZE) {
            return ValidationResult(false, "File size exceeds maximum allowed size (${MAX_FILE_SIZE / (1024 * 1024)}MB)")
        }
        
        val extension = getFileExtension(file.name)
        if (extension.isBlank() || !ALLOWED_EXTENSIONS.contains(extension.lowercase())) {
            return ValidationResult(false, "Unsupported file type. Allowed: ${ALLOWED_EXTENSIONS.joinToString(", ")}")
        }
        
        return ValidationResult(true, "Valid file")
    }
    
    /**
     * Validates file path
     */
    fun isValidFilePath(filePath: String?): ValidationResult {
        if (filePath.isNullOrBlank()) {
            return ValidationResult(false, "File path cannot be empty")
        }
        
        val file = File(filePath)
        return isValidFile(file)
    }
    
    /**
     * Validates URL
     */
    fun isValidUrl(url: String?): ValidationResult {
        if (url.isNullOrBlank()) {
            return ValidationResult(false, "URL cannot be empty")
        }
        
        if (!URL_PATTERN.matcher(url).matches()) {
            return ValidationResult(false, "Invalid URL format")
        }
        
        return ValidationResult(true, "Valid URL")
    }
    
    /**
     * Validates port number
     */
    fun isValidPort(port: Int): ValidationResult {
        if (port < 1 || port > 65535) {
            return ValidationResult(false, "Port must be between 1 and 65535")
        }
        
        return ValidationResult(true, "Valid port")
    }
    
    /**
     * Validates input text (for chat messages, etc.)
     */
    fun isValidInputText(text: String?, maxLength: Int = 10000): ValidationResult {
        if (text.isNullOrBlank()) {
            return ValidationResult(false, "Input text cannot be empty")
        }
        
        if (text.length > maxLength) {
            return ValidationResult(false, "Input text is too long (max $maxLength characters)")
        }
        
        // Check for potentially malicious content
        if (containsSuspiciousContent(text)) {
            return ValidationResult(false, "Input contains potentially harmful content")
        }
        
        return ValidationResult(true, "Valid input text")
    }
    
    /**
     * Sanitizes input text
     */
    fun sanitizeInput(text: String): String {
        return text.trim()
            .replace(Regex("[<>\"'&]")) { matchResult ->
                when (matchResult.value) {
                    "<" -> "&lt;"
                    ">" -> "&gt;"
                    "\"" -> "&quot;"
                    "'" -> "&#x27;"
                    "&" -> "&amp;"
                    else -> matchResult.value
                }
            }
    }
    
    /**
     * Gets file extension
     */
    private fun getFileExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex > 0 && lastDotIndex < fileName.length - 1) {
            fileName.substring(lastDotIndex + 1)
        } else {
            ""
        }
    }
    
    /**
     * Checks for suspicious content
     */
    private fun containsSuspiciousContent(text: String): Boolean {
        val suspiciousPatterns = listOf(
            "<script",
            "javascript:",
            "data:",
            "vbscript:",
            "onload=",
            "onerror=",
            "onclick=",
            "eval(",
            "document.cookie",
            "document.write"
        )
        
        val lowerText = text.lowercase()
        return suspiciousPatterns.any { pattern -> lowerText.contains(pattern) }
    }
}

/**
 * Validation result data class
 */
data class ValidationResult(
    val isValid: Boolean,
    val message: String
)
