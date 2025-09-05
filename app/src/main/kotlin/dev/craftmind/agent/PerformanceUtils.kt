/*
 * Performance optimization utilities for the Koog AI Agent
 */
package dev.craftmind.agent

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.measureTimeMillis

/**
 * Performance monitoring and optimization utilities
 */
object PerformanceUtils {
    
    private val performanceMetrics = ConcurrentHashMap<String, PerformanceMetric>()
    private val scheduledExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
    private val gcCount = AtomicLong(0)
    private val lastGcTime = AtomicLong(0)
    
    init {
        // Start performance monitoring
        startPerformanceMonitoring()
    }
    
    /**
     * Performance metric data class
     */
    data class PerformanceMetric(
        val name: String,
        val totalCalls: AtomicLong = AtomicLong(0),
        val totalTime: AtomicLong = AtomicLong(0),
        val minTime: AtomicLong = AtomicLong(Long.MAX_VALUE),
        val maxTime: AtomicLong = AtomicLong(0),
        val lastCallTime: AtomicLong = AtomicLong(0),
        val errorCount: AtomicLong = AtomicLong(0)
    ) {
        fun getAverageTime(): Long {
            val calls = totalCalls.get()
            return if (calls > 0) totalTime.get() / calls else 0
        }
        
        fun getSuccessRate(): Double {
            val calls = totalCalls.get()
            val errors = errorCount.get()
            return if (calls > 0) (calls - errors).toDouble() / calls * 100 else 0.0
        }
    }
    
    /**
     * Measures execution time of a function and records metrics
     */
    inline fun <T> measurePerformance(
        operationName: String,
        recordErrors: Boolean = true,
        block: () -> T
    ): T {
        val metric = performanceMetrics.getOrPut(operationName) { PerformanceMetric(operationName) }
        val startTime = System.currentTimeMillis()
        var result: T? = null
        var error: Throwable? = null
        
        try {
            result = block()
            return result!!
        } catch (e: Throwable) {
            error = e
            if (recordErrors) {
                metric.errorCount.incrementAndGet()
            }
            throw e
        } finally {
            val executionTime = System.currentTimeMillis() - startTime
            recordMetric(metric, executionTime)
        }
    }
    
    /**
     * Records performance metric
     */
    private fun recordMetric(metric: PerformanceMetric, executionTime: Long) {
        metric.totalCalls.incrementAndGet()
        metric.totalTime.addAndGet(executionTime)
        metric.lastCallTime.set(System.currentTimeMillis())
        
        // Update min/max times atomically
        var currentMin = metric.minTime.get()
        while (executionTime < currentMin && !metric.minTime.compareAndSet(currentMin, executionTime)) {
            currentMin = metric.minTime.get()
        }
        
        var currentMax = metric.maxTime.get()
        while (executionTime > currentMax && !metric.maxTime.compareAndSet(currentMax, executionTime)) {
            currentMax = metric.maxTime.get()
        }
    }
    
    /**
     * Gets performance metrics for an operation
     */
    fun getPerformanceMetrics(operationName: String): PerformanceMetric? {
        return performanceMetrics[operationName]
    }
    
    /**
     * Gets all performance metrics
     */
    fun getAllPerformanceMetrics(): Map<String, PerformanceMetric> {
        return performanceMetrics.toMap()
    }
    
    /**
     * Clears performance metrics
     */
    fun clearPerformanceMetrics() {
        performanceMetrics.clear()
    }
    
    /**
     * Gets performance summary
     */
    fun getPerformanceSummary(): String {
        val summary = StringBuilder()
        summary.appendLine("📊 Performance Summary:")
        summary.appendLine("─".repeat(50))
        
        performanceMetrics.forEach { (name, metric) ->
            summary.appendLine("🔧 $name:")
            summary.appendLine("  • Total calls: ${metric.totalCalls.get()}")
            summary.appendLine("  • Average time: ${metric.getAverageTime()}ms")
            summary.appendLine("  • Min time: ${metric.minTime.get()}ms")
            summary.appendLine("  • Max time: ${metric.maxTime.get()}ms")
            summary.appendLine("  • Success rate: ${String.format("%.1f", metric.getSuccessRate())}%")
            summary.appendLine("  • Last call: ${java.time.Instant.ofEpochMilli(metric.lastCallTime.get())}")
            summary.appendLine()
        }
        
        return summary.toString()
    }
    
    /**
     * Memory optimization utilities
     */
    object MemoryOptimizer {
        
        /**
         * Forces garbage collection if memory usage is high
         */
        fun optimizeMemoryIfNeeded(): Boolean {
            val runtime = Runtime.getRuntime()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            val memoryUsagePercent = (usedMemory.toDouble() / totalMemory.toDouble()) * 100
            
            if (memoryUsagePercent > 80.0) {
                Logger.warn("High memory usage detected: ${String.format("%.1f", memoryUsagePercent)}%")
                forceGarbageCollection()
                return true
            }
            
            return false
        }
        
        /**
         * Forces garbage collection
         */
        fun forceGarbageCollection() {
            val beforeMemory = getUsedMemory()
            System.gc()
            Thread.sleep(100) // Give GC time to run
            val afterMemory = getUsedMemory()
            val freedMemory = beforeMemory - afterMemory
            
            gcCount.incrementAndGet()
            lastGcTime.set(System.currentTimeMillis())
            
            Logger.debug("Garbage collection completed. Freed: ${formatBytes(freedMemory)}")
        }
        
        /**
         * Gets current memory usage
         */
        fun getMemoryUsage(): MemoryUsage {
            val runtime = Runtime.getRuntime()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            val maxMemory = runtime.maxMemory()
            
            return MemoryUsage(
                used = usedMemory,
                total = totalMemory,
                free = freeMemory,
                max = maxMemory,
                usagePercent = (usedMemory.toDouble() / totalMemory.toDouble()) * 100
            )
        }
        
        /**
         * Gets used memory in bytes
         */
        private fun getUsedMemory(): Long {
            val runtime = Runtime.getRuntime()
            return runtime.totalMemory() - runtime.freeMemory()
        }
        
        /**
         * Formats bytes to human readable format
         */
        fun formatBytes(bytes: Long): String {
            return when {
                bytes >= 1_000_000_000 -> "${bytes / 1_000_000_000}GB"
                bytes >= 1_000_000 -> "${bytes / 1_000_000}MB"
                bytes >= 1_000 -> "${bytes / 1_000}KB"
                else -> "${bytes}B"
            }
        }
    }
    
    /**
     * Cache optimization utilities
     */
    object CacheOptimizer {
        private val cacheMetrics = ConcurrentHashMap<String, CacheMetric>()
        
        data class CacheMetric(
            val name: String,
            val hits: AtomicLong = AtomicLong(0),
            val misses: AtomicLong = AtomicLong(0),
            val evictions: AtomicLong = AtomicLong(0),
            val size: AtomicLong = AtomicLong(0)
        ) {
            fun getHitRate(): Double {
                val total = hits.get() + misses.get()
                return if (total > 0) hits.get().toDouble() / total * 100 else 0.0
            }
        }
        
        /**
         * Records cache hit
         */
        fun recordCacheHit(cacheName: String) {
            val metric = cacheMetrics.getOrPut(cacheName) { CacheMetric(cacheName) }
            metric.hits.incrementAndGet()
        }
        
        /**
         * Records cache miss
         */
        fun recordCacheMiss(cacheName: String) {
            val metric = cacheMetrics.getOrPut(cacheName) { CacheMetric(cacheName) }
            metric.misses.incrementAndGet()
        }
        
        /**
         * Records cache eviction
         */
        fun recordCacheEviction(cacheName: String) {
            val metric = cacheMetrics.getOrPut(cacheName) { CacheMetric(cacheName) }
            metric.evictions.incrementAndGet()
        }
        
        /**
         * Updates cache size
         */
        fun updateCacheSize(cacheName: String, size: Long) {
            val metric = cacheMetrics.getOrPut(cacheName) { CacheMetric(cacheName) }
            metric.size.set(size)
        }
        
        /**
         * Gets cache metrics
         */
        fun getCacheMetrics(cacheName: String): CacheMetric? {
            return cacheMetrics[cacheName]
        }
        
        /**
         * Gets all cache metrics
         */
        fun getAllCacheMetrics(): Map<String, CacheMetric> {
            return cacheMetrics.toMap()
        }
    }
    
    /**
     * Starts performance monitoring
     */
    private fun startPerformanceMonitoring() {
        // Monitor memory usage every 30 seconds
        scheduledExecutor.scheduleAtFixedRate({
            try {
                val memoryUsage = MemoryOptimizer.getMemoryUsage()
                if (Config.ENABLE_PERFORMANCE_METRICS) {
                    Logger.performance("Memory usage: ${String.format("%.1f", memoryUsage.usagePercent)}% (${MemoryOptimizer.formatBytes(memoryUsage.used)}/${MemoryOptimizer.formatBytes(memoryUsage.total)})")
                }
                
                // Optimize memory if needed
                MemoryOptimizer.optimizeMemoryIfNeeded()
            } catch (e: Exception) {
                Logger.error("Error in performance monitoring", e)
            }
        }, 30, 30, TimeUnit.SECONDS)
        
        // Log performance summary every 5 minutes
        scheduledExecutor.scheduleAtFixedRate({
            try {
                if (Config.ENABLE_PERFORMANCE_METRICS && performanceMetrics.isNotEmpty()) {
                    Logger.performance("Performance metrics updated")
                }
            } catch (e: Exception) {
                Logger.error("Error in performance summary", e)
            }
        }, 300, 300, TimeUnit.SECONDS)
    }
    
    /**
     * Shuts down performance monitoring
     */
    fun shutdown() {
        scheduledExecutor.shutdown()
        try {
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            scheduledExecutor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}

/**
 * Memory usage data class
 */
data class MemoryUsage(
    val used: Long,
    val total: Long,
    val free: Long,
    val max: Long,
    val usagePercent: Double
)
