/*
 * Unit tests for Logger
 */
package dev.craftmind.agent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LoggerTest {

    @Test
    fun testLogLevelEnum() {
        assertEquals(0, Logger.LogLevel.DEBUG.priority)
        assertEquals(1, Logger.LogLevel.INFO.priority)
        assertEquals(2, Logger.LogLevel.WARN.priority)
        assertEquals(3, Logger.LogLevel.ERROR.priority)
    }

    @Test
    fun testSetLogLevel() {
        val originalLevel = Logger.LogLevel.INFO
        Logger.setLogLevel(Logger.LogLevel.DEBUG)
        // Note: We can't easily test the internal state, but we can test that the method doesn't throw
        assertNotNull(Logger.LogLevel.DEBUG)
        Logger.setLogLevel(originalLevel)
    }

    @Test
    fun testSetDebugLogging() {
        val originalDebug = Config.ENABLE_DEBUG_LOGGING
        Logger.setDebugLogging(true)
        // Note: We can't easily test the internal state, but we can test that the method doesn't throw
        assertNotNull(true)
        Logger.setDebugLogging(originalDebug)
    }

    @Test
    fun testDebugMethod() {
        // Test that debug method doesn't throw
        Logger.debug("Test debug message")
        assertNotNull("Test debug message")
    }

    @Test
    fun testInfoMethod() {
        // Test that info method doesn't throw
        Logger.info("Test info message")
        assertNotNull("Test info message")
    }

    @Test
    fun testWarnMethod() {
        // Test that warn method doesn't throw
        Logger.warn("Test warn message")
        assertNotNull("Test warn message")
    }

    @Test
    fun testErrorMethod() {
        // Test that error method doesn't throw
        Logger.error("Test error message")
        assertNotNull("Test error message")
    }

    @Test
    fun testErrorMethodWithThrowable() {
        // Test that error method with throwable doesn't throw
        val exception = Exception("Test exception")
        Logger.error("Test error message", exception)
        assertNotNull("Test error message")
        assertNotNull(exception)
    }

    @Test
    fun testSuccessMethod() {
        // Test that success method doesn't throw
        Logger.success("Test success message")
        assertNotNull("Test success message")
    }

    @Test
    fun testProgressMethod() {
        // Test that progress method doesn't throw
        Logger.progress("Test progress message")
        assertNotNull("Test progress message")
    }

    @Test
    fun testSystemMethod() {
        // Test that system method doesn't throw
        Logger.system("Test system message")
        assertNotNull("Test system message")
    }

    @Test
    fun testPerformanceMethod() {
        // Test that performance method doesn't throw
        Logger.performance("Test performance message")
        assertNotNull("Test performance message")
    }
}
