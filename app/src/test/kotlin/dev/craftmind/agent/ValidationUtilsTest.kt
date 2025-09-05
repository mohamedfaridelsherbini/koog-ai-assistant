/*
 * Unit tests for ValidationUtils
 */
package dev.craftmind.agent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class ValidationUtilsTest {

    @Test
    fun testValidModelName() {
        val result = ValidationUtils.isValidModelName("llama3.1:8b")
        assertTrue(result.isValid)
        assertEquals("Valid model name", result.message)
    }

    @Test
    fun testInvalidModelNameEmpty() {
        val result = ValidationUtils.isValidModelName("")
        assertFalse(result.isValid)
        assertEquals("Model name cannot be empty", result.message)
    }

    @Test
    fun testInvalidModelNameNull() {
        val result = ValidationUtils.isValidModelName(null)
        assertFalse(result.isValid)
        assertEquals("Model name cannot be empty", result.message)
    }

    @Test
    fun testInvalidModelNameTooLong() {
        val longName = "a".repeat(101)
        val result = ValidationUtils.isValidModelName(longName)
        assertFalse(result.isValid)
        assertEquals("Model name is too long (max 100 characters)", result.message)
    }

    @Test
    fun testInvalidModelNameSpecialCharacters() {
        val result = ValidationUtils.isValidModelName("model@name#with$special%chars")
        assertFalse(result.isValid)
        assertEquals("Model name contains invalid characters", result.message)
    }

    @Test
    fun testValidUrl() {
        val result = ValidationUtils.isValidUrl("http://localhost:8080")
        assertTrue(result.isValid)
        assertEquals("Valid URL", result.message)
    }

    @Test
    fun testInvalidUrlEmpty() {
        val result = ValidationUtils.isValidUrl("")
        assertFalse(result.isValid)
        assertEquals("URL cannot be empty", result.message)
    }

    @Test
    fun testInvalidUrlFormat() {
        val result = ValidationUtils.isValidUrl("not-a-url")
        assertFalse(result.isValid)
        assertEquals("Invalid URL format", result.message)
    }

    @Test
    fun testValidPort() {
        val result = ValidationUtils.isValidPort(8080)
        assertTrue(result.isValid)
        assertEquals("Valid port", result.message)
    }

    @Test
    fun testInvalidPortTooLow() {
        val result = ValidationUtils.isValidPort(0)
        assertFalse(result.isValid)
        assertEquals("Port must be between 1 and 65535", result.message)
    }

    @Test
    fun testInvalidPortTooHigh() {
        val result = ValidationUtils.isValidPort(65536)
        assertFalse(result.isValid)
        assertEquals("Port must be between 1 and 65535", result.message)
    }

    @Test
    fun testValidInputText() {
        val result = ValidationUtils.isValidInputText("Hello, world!")
        assertTrue(result.isValid)
        assertEquals("Valid input text", result.message)
    }

    @Test
    fun testInvalidInputTextEmpty() {
        val result = ValidationUtils.isValidInputText("")
        assertFalse(result.isValid)
        assertEquals("Input text cannot be empty", result.message)
    }

    @Test
    fun testInvalidInputTextTooLong() {
        val longText = "a".repeat(10001)
        val result = ValidationUtils.isValidInputText(longText)
        assertFalse(result.isValid)
        assertEquals("Input text is too long (max 10000 characters)", result.message)
    }

    @Test
    fun testSuspiciousContent() {
        val result = ValidationUtils.isValidInputText("<script>alert('xss')</script>")
        assertFalse(result.isValid)
        assertEquals("Input contains potentially harmful content", result.message)
    }

    @Test
    fun testSanitizeInput() {
        val input = "<script>alert('xss')</script>"
        val sanitized = ValidationUtils.sanitizeInput(input)
        assertEquals("&lt;script&gt;alert(&#x27;xss&#x27;)&lt;/script&gt;", sanitized)
    }

    @Test
    fun testValidFile() {
        // Create a temporary file for testing
        val tempFile = Files.createTempFile("test", ".txt")
        tempFile.toFile().writeText("test content")
        
        try {
            val result = ValidationUtils.isValidFile(tempFile.toFile())
            assertTrue(result.isValid)
            assertEquals("Valid file", result.message)
        } finally {
            Files.deleteIfExists(tempFile)
        }
    }

    @Test
    fun testInvalidFileDoesNotExist() {
        val nonExistentFile = File("non-existent-file.txt")
        val result = ValidationUtils.isValidFile(nonExistentFile)
        assertFalse(result.isValid)
        assertEquals("File does not exist", result.message)
    }

    @Test
    fun testInvalidFileTooLarge() {
        // Create a temporary file that's too large
        val tempFile = Files.createTempFile("test", ".txt")
        val largeContent = "a".repeat(Config.MAX_FILE_SIZE_BYTES + 1)
        tempFile.toFile().writeText(largeContent)
        
        try {
            val result = ValidationUtils.isValidFile(tempFile.toFile())
            assertFalse(result.isValid)
            assertTrue(result.message.contains("File size exceeds maximum allowed size"))
        } finally {
            Files.deleteIfExists(tempFile)
        }
    }

    @Test
    fun testInvalidFileUnsupportedType() {
        val tempFile = Files.createTempFile("test", ".exe")
        tempFile.toFile().writeText("test content")
        
        try {
            val result = ValidationUtils.isValidFile(tempFile.toFile())
            assertFalse(result.isValid)
            assertTrue(result.message.contains("Unsupported file type"))
        } finally {
            Files.deleteIfExists(tempFile)
        }
    }
}
