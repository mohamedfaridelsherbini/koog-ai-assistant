/*
 * Security Architecture for Koog Agent Deep Research
 * Comprehensive security measures and authentication/authorization
 */
package dev.craftmind.agent.architecture

import kotlinx.coroutines.*
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.util.Base64

/**
 * Security Architecture Implementation
 * 
 * Security Features:
 * - JWT tokens with refresh mechanism
 * - RBAC with fine-grained permissions
 * - Input validation and sanitization
 * - Rate limiting per-user and per-endpoint
 * - Encryption (TLS 1.3, AES-256 for data at rest)
 * - Audit logging for compliance
 */

// ============================================================================
// AUTHENTICATION & AUTHORIZATION
// ============================================================================

/**
 * JWT Token Manager
 */
class JWTTokenManager(
    private val secretKey: String,
    private val issuer: String,
    private val audience: String
) {
    private val accessTokenExpiry = 15 * 60 * 1000L // 15 minutes
    private val refreshTokenExpiry = 7 * 24 * 60 * 60 * 1000L // 7 days
    
    suspend fun generateAccessToken(user: User): String {
        val now = Instant.now()
        val claims = mapOf(
            "sub" to user.id,
            "iss" to issuer,
            "aud" to audience,
            "iat" to now.epochSecond,
            "exp" to now.plusSeconds(accessTokenExpiry / 1000).epochSecond,
            "type" to "access",
            "permissions" to user.permissions
        )
        
        return createJWT(claims)
    }
    
    suspend fun generateRefreshToken(user: User): String {
        val now = Instant.now()
        val claims = mapOf(
            "sub" to user.id,
            "iss" to issuer,
            "aud" to audience,
            "iat" to now.epochSecond,
            "exp" to now.plusSeconds(refreshTokenExpiry / 1000).epochSecond,
            "type" to "refresh"
        )
        
        return createJWT(claims)
    }
    
    suspend fun validateToken(token: String): TokenValidationResult {
        return try {
            val claims = parseJWT(token)
            val now = Instant.now().epochSecond
            
            // Check expiration
            val exp = claims["exp"] as? Long ?: return TokenValidationResult.Invalid("Missing expiration")
            if (exp < now) {
                return TokenValidationResult.Expired
            }
            
            // Check issuer and audience
            if (claims["iss"] != issuer || claims["aud"] != audience) {
                return TokenValidationResult.Invalid("Invalid issuer or audience")
            }
            
            TokenValidationResult.Valid(claims)
        } catch (e: Exception) {
            TokenValidationResult.Invalid("Token parsing failed: ${e.message}")
        }
    }
    
    suspend fun refreshAccessToken(refreshToken: String, userRepository: UserRepository): String? {
        val validation = validateToken(refreshToken)
        if (validation !is TokenValidationResult.Valid) return null
        
        val claims = validation.claims
        if (claims["type"] != "refresh") return null
        
        val userId = claims["sub"] as? String ?: return null
        val user = userRepository.findById(userId) ?: return null
        
        return generateAccessToken(user)
    }
    
    private fun createJWT(claims: Map<String, Any>): String {
        // Simplified JWT creation - in reality, use a proper JWT library
        val header = Base64.getUrlEncoder().withoutPadding().encodeToString(
            """{"alg":"HS256","typ":"JWT"}""".toByteArray()
        )
        
        val payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
            claims.toString().toByteArray()
        )
        
        val signature = Base64.getUrlEncoder().withoutPadding().encodeToString(
            createSignature("$header.$payload")
        )
        
        return "$header.$payload.$signature"
    }
    
    private fun parseJWT(token: String): Map<String, Any> {
        // Simplified JWT parsing - in reality, use a proper JWT library
        val parts = token.split(".")
        if (parts.size != 3) throw IllegalArgumentException("Invalid JWT format")
        
        val payload = String(Base64.getUrlDecoder().decode(parts[1]))
        // Parse JSON payload - this is a placeholder
        return mapOf("sub" to "user123", "exp" to (System.currentTimeMillis() / 1000 + 3600))
    }
    
    private fun createSignature(data: String): ByteArray {
        val mac = javax.crypto.Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
        mac.init(secretKeySpec)
        return mac.doFinal(data.toByteArray())
    }
}

/**
 * Role-Based Access Control (RBAC)
 */
class RBACManager {
    
    private val rolePermissions = mapOf(
        "admin" to setOf(
            "user:read", "user:write", "user:delete",
            "conversation:read", "conversation:write", "conversation:delete",
            "file:read", "file:write", "file:delete",
            "model:read", "model:write", "model:delete",
            "system:monitor", "system:configure"
        ),
        "user" to setOf(
            "conversation:read", "conversation:write",
            "file:read", "file:write",
            "model:read"
        ),
        "guest" to setOf(
            "conversation:read"
        )
    )
    
    fun hasPermission(user: User, permission: String): Boolean {
        return user.permissions.contains(permission) || 
               user.permissions.any { role -> 
                   rolePermissions[role]?.contains(permission) == true 
               }
    }
    
    fun hasAnyPermission(user: User, permissions: Set<String>): Boolean {
        return permissions.any { hasPermission(user, it) }
    }
    
    fun hasAllPermissions(user: User, permissions: Set<String>): Boolean {
        return permissions.all { hasPermission(user, it) }
    }
    
    fun getEffectivePermissions(user: User): Set<String> {
        val directPermissions = user.permissions.toSet()
        val rolePermissions = user.permissions.flatMap { role ->
            rolePermissions[role] ?: emptySet()
        }.toSet()
        
        return directPermissions + rolePermissions
    }
}

/**
 * Authentication Service
 */
class AuthenticationService(
    private val userRepository: UserRepository,
    private val tokenManager: JWTTokenManager,
    private val passwordHasher: PasswordHasher,
    private val rbacManager: RBACManager,
    private val auditLogger: AuditLogger
) {
    
    suspend fun authenticate(email: String, password: String): AuthenticationResult {
        val user = userRepository.findByEmail(email)
        if (user == null) {
            auditLogger.logFailedLogin(email, "User not found")
            return AuthenticationResult.Failed("Invalid credentials")
        }
        
        if (!passwordHasher.verify(password, user.passwordHash)) {
            auditLogger.logFailedLogin(email, "Invalid password")
            return AuthenticationResult.Failed("Invalid credentials")
        }
        
        val accessToken = tokenManager.generateAccessToken(user)
        val refreshToken = tokenManager.generateRefreshToken(user)
        
        auditLogger.logSuccessfulLogin(user.id, email)
        
        return AuthenticationResult.Success(
            user = user,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
    
    suspend fun register(email: String, password: String, name: String): RegistrationResult {
        // Validate input
        if (!isValidEmail(email)) {
            return RegistrationResult.Failed("Invalid email format")
        }
        
        if (!isValidPassword(password)) {
            return RegistrationResult.Failed("Password does not meet requirements")
        }
        
        // Check if user already exists
        if (userRepository.findByEmail(email) != null) {
            return RegistrationResult.Failed("User already exists")
        }
        
        // Create new user
        val userId = generateUserId()
        val passwordHash = passwordHasher.hash(password)
        
        val user = User(
            id = userId,
            email = email,
            name = name,
            passwordHash = passwordHash,
            permissions = listOf("user"),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val savedUser = userRepository.save(user)
        auditLogger.logUserRegistration(userId, email)
        
        return RegistrationResult.Success(savedUser)
    }
    
    suspend fun authorize(user: User, permission: String): Boolean {
        val hasPermission = rbacManager.hasPermission(user, permission)
        
        auditLogger.logAuthorization(
            userId = user.id,
            permission = permission,
            granted = hasPermission
        )
        
        return hasPermission
    }
    
    suspend fun refreshToken(refreshToken: String): TokenRefreshResult {
        val newAccessToken = tokenManager.refreshAccessToken(refreshToken, userRepository)
        
        return if (newAccessToken != null) {
            TokenRefreshResult.Success(newAccessToken)
        } else {
            TokenRefreshResult.Failed("Invalid refresh token")
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }
    
    private fun isValidPassword(password: String): Boolean {
        // Password requirements: 8+ chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$".toRegex()
        return passwordRegex.matches(password)
    }
    
    private fun generateUserId(): String = "user_${System.currentTimeMillis()}_${(1000..9999).random()}"
}

// ============================================================================
// INPUT VALIDATION & SANITIZATION
// ============================================================================

/**
 * Input Validator
 */
class InputValidator {
    
    fun validateAndSanitize(input: String, type: InputType): ValidationResult {
        return when (type) {
            InputType.TEXT -> validateText(input)
            InputType.EMAIL -> validateEmail(input)
            InputType.URL -> validateUrl(input)
            InputType.FILENAME -> validateFilename(input)
            InputType.MODEL_NAME -> validateModelName(input)
            InputType.JSON -> validateJson(input)
        }
    }
    
    private fun validateText(input: String): ValidationResult {
        if (input.isBlank()) {
            return ValidationResult.Invalid("Input cannot be empty")
        }
        
        if (input.length > 10000) {
            return ValidationResult.Invalid("Input too long")
        }
        
        // Check for XSS patterns
        val xssPatterns = listOf(
            "<script", "javascript:", "onload=", "onerror=", "onclick="
        )
        
        val lowerInput = input.lowercase()
        for (pattern in xssPatterns) {
            if (lowerInput.contains(pattern)) {
                return ValidationResult.Invalid("Potentially malicious input detected")
            }
        }
        
        val sanitized = sanitizeHtml(input)
        return ValidationResult.Valid(sanitized)
    }
    
    private fun validateEmail(input: String): ValidationResult {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        
        return if (emailRegex.matches(input)) {
            ValidationResult.Valid(input.lowercase().trim())
        } else {
            ValidationResult.Invalid("Invalid email format")
        }
    }
    
    private fun validateUrl(input: String): ValidationResult {
        val urlRegex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$".toRegex()
        
        return if (urlRegex.matches(input)) {
            ValidationResult.Valid(input)
        } else {
            ValidationResult.Invalid("Invalid URL format")
        }
    }
    
    private fun validateFilename(input: String): ValidationResult {
        if (input.isBlank()) {
            return ValidationResult.Invalid("Filename cannot be empty")
        }
        
        val invalidChars = listOf("/", "\\", ":", "*", "?", "\"", "<", ">", "|")
        for (char in invalidChars) {
            if (input.contains(char)) {
                return ValidationResult.Invalid("Filename contains invalid characters")
            }
        }
        
        if (input.length > 255) {
            return ValidationResult.Invalid("Filename too long")
        }
        
        return ValidationResult.Valid(input)
    }
    
    private fun validateModelName(input: String): ValidationResult {
        val modelNameRegex = "^[a-zA-Z0-9._-]+$".toRegex()
        
        return if (modelNameRegex.matches(input)) {
            ValidationResult.Valid(input)
        } else {
            ValidationResult.Invalid("Invalid model name format")
        }
    }
    
    private fun validateJson(input: String): ValidationResult {
        return try {
            // Simple JSON validation - in reality, use a proper JSON parser
            if (input.trim().startsWith("{") && input.trim().endsWith("}")) {
                ValidationResult.Valid(input)
            } else {
                ValidationResult.Invalid("Invalid JSON format")
            }
        } catch (e: Exception) {
            ValidationResult.Invalid("Invalid JSON format: ${e.message}")
        }
    }
    
    private fun sanitizeHtml(input: String): String {
        return input
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("&", "&amp;")
    }
}

// ============================================================================
// RATE LIMITING
// ============================================================================

/**
 * Rate Limiter
 */
class RateLimiter(
    private val maxRequests: Int,
    private val windowMs: Long
) {
    private val requests = ConcurrentHashMap<String, MutableList<Long>>()
    
    suspend fun isAllowed(identifier: String): RateLimitResult {
        val now = System.currentTimeMillis()
        val windowStart = now - windowMs
        
        // Clean up old requests
        val userRequests = requests.computeIfAbsent(identifier) { mutableListOf() }
        userRequests.removeAll { it < windowStart }
        
        // Check if limit exceeded
        if (userRequests.size >= maxRequests) {
            return RateLimitResult.Blocked(
                retryAfter = userRequests.minOrNull()?.let { it + windowMs - now } ?: windowMs
            )
        }
        
        // Add current request
        userRequests.add(now)
        
        return RateLimitResult.Allowed(
            remaining = maxRequests - userRequests.size,
            resetTime = now + windowMs
        )
    }
    
    suspend fun reset(identifier: String) {
        requests.remove(identifier)
    }
}

/**
 * Multi-tier Rate Limiter
 */
class MultiTierRateLimiter {
    private val globalLimiter = RateLimiter(1000, 60_000) // 1000 requests per minute
    private val userLimiters = ConcurrentHashMap<String, RateLimiter>()
    private val endpointLimiters = ConcurrentHashMap<String, RateLimiter>()
    
    suspend fun checkRateLimit(
        userId: String?,
        endpoint: String,
        userLimit: Int = 100,
        endpointLimit: Int = 200
    ): RateLimitResult {
        // Global rate limit
        val globalResult = globalLimiter.isAllowed("global")
        if (globalResult is RateLimitResult.Blocked) {
            return globalResult
        }
        
        // User-specific rate limit
        if (userId != null) {
            val userLimiter = userLimiters.computeIfAbsent(userId) { 
                RateLimiter(userLimit, 60_000) 
            }
            val userResult = userLimiter.isAllowed(userId)
            if (userResult is RateLimitResult.Blocked) {
                return userResult
            }
        }
        
        // Endpoint-specific rate limit
        val endpointLimiter = endpointLimiters.computeIfAbsent(endpoint) { 
            RateLimiter(endpointLimit, 60_000) 
        }
        return endpointLimiter.isAllowed(endpoint)
    }
}

// ============================================================================
// ENCRYPTION
// ============================================================================

/**
 * Encryption Service
 */
class EncryptionService(private val key: String) {
    
    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plaintext.toByteArray())
        
        val combined = iv + encrypted
        return Base64.getEncoder().encodeToString(combined)
    }
    
    fun decrypt(ciphertext: String): String {
        val combined = Base64.getDecoder().decode(ciphertext)
        val iv = combined.sliceArray(0..15)
        val encrypted = combined.sliceArray(16 until combined.size)
        
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        val decrypted = cipher.doFinal(encrypted)
        
        return String(decrypted)
    }
    
    fun hash(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray())
        return Base64.getEncoder().encodeToString(hash)
    }
}

/**
 * Password Hasher
 */
class PasswordHasher {
    private val saltLength = 32
    private val iterations = 100000
    
    fun hash(password: String): String {
        val salt = ByteArray(saltLength)
        SecureRandom().nextBytes(salt)
        
        val hash = pbkdf2(password, salt, iterations)
        val saltBase64 = Base64.getEncoder().encodeToString(salt)
        val hashBase64 = Base64.getEncoder().encodeToString(hash)
        
        return "$saltBase64:$hashBase64:$iterations"
    }
    
    fun verify(password: String, hashedPassword: String): Boolean {
        val parts = hashedPassword.split(":")
        if (parts.size != 3) return false
        
        val salt = Base64.getDecoder().decode(parts[0])
        val hash = Base64.getDecoder().decode(parts[1])
        val iterations = parts[2].toInt()
        
        val testHash = pbkdf2(password, salt, iterations)
        return hash.contentEquals(testHash)
    }
    
    private fun pbkdf2(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val keySpec = javax.crypto.spec.PBEKeySpec(
            password.toCharArray(),
            salt,
            iterations,
            256
        )
        val keyFactory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return keyFactory.generateSecret(keySpec).encoded
    }
}

// ============================================================================
// AUDIT LOGGING
// ============================================================================

/**
 * Audit Logger
 */
class AuditLogger(
    private val logRepository: AuditLogRepository
) {
    
    suspend fun logSuccessfulLogin(userId: String, email: String) {
        logEvent(AuditEvent(
            id = generateEventId(),
            userId = userId,
            action = "LOGIN_SUCCESS",
            resource = "authentication",
            details = mapOf("email" to email),
            timestamp = System.currentTimeMillis(),
            ipAddress = getCurrentIpAddress(),
            userAgent = getCurrentUserAgent()
        ))
    }
    
    suspend fun logFailedLogin(email: String, reason: String) {
        logEvent(AuditEvent(
            id = generateEventId(),
            userId = null,
            action = "LOGIN_FAILED",
            resource = "authentication",
            details = mapOf("email" to email, "reason" to reason),
            timestamp = System.currentTimeMillis(),
            ipAddress = getCurrentIpAddress(),
            userAgent = getCurrentUserAgent()
        ))
    }
    
    suspend fun logUserRegistration(userId: String, email: String) {
        logEvent(AuditEvent(
            id = generateEventId(),
            userId = userId,
            action = "USER_REGISTRATION",
            resource = "user",
            details = mapOf("email" to email),
            timestamp = System.currentTimeMillis(),
            ipAddress = getCurrentIpAddress(),
            userAgent = getCurrentUserAgent()
        ))
    }
    
    suspend fun logAuthorization(userId: String, permission: String, granted: Boolean) {
        logEvent(AuditEvent(
            id = generateEventId(),
            userId = userId,
            action = "AUTHORIZATION_CHECK",
            resource = "authorization",
            details = mapOf("permission" to permission, "granted" to granted),
            timestamp = System.currentTimeMillis(),
            ipAddress = getCurrentIpAddress(),
            userAgent = getCurrentUserAgent()
        ))
    }
    
    suspend fun logDataAccess(userId: String, resource: String, action: String, details: Map<String, Any>) {
        logEvent(AuditEvent(
            id = generateEventId(),
            userId = userId,
            action = action,
            resource = resource,
            details = details,
            timestamp = System.currentTimeMillis(),
            ipAddress = getCurrentIpAddress(),
            userAgent = getCurrentUserAgent()
        ))
    }
    
    private suspend fun logEvent(event: AuditEvent) {
        logRepository.save(event)
    }
    
    private fun generateEventId(): String = "audit_${System.currentTimeMillis()}_${(1000..9999).random()}"
    private fun getCurrentIpAddress(): String = "127.0.0.1" // Placeholder
    private fun getCurrentUserAgent(): String = "KoogAgent/1.0" // Placeholder
}

// ============================================================================
// DATA CLASSES AND INTERFACES
// ============================================================================

sealed class TokenValidationResult {
    data class Valid(val claims: Map<String, Any>) : TokenValidationResult()
    data class Invalid(val reason: String) : TokenValidationResult()
    object Expired : TokenValidationResult()
}

sealed class AuthenticationResult {
    data class Success(val user: User, val accessToken: String, val refreshToken: String) : AuthenticationResult()
    data class Failed(val reason: String) : AuthenticationResult()
}

sealed class RegistrationResult {
    data class Success(val user: User) : RegistrationResult()
    data class Failed(val reason: String) : RegistrationResult()
}

sealed class TokenRefreshResult {
    data class Success(val accessToken: String) : TokenRefreshResult()
    data class Failed(val reason: String) : TokenRefreshResult()
}

sealed class ValidationResult {
    data class Valid(val sanitizedInput: String) : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

enum class InputType {
    TEXT, EMAIL, URL, FILENAME, MODEL_NAME, JSON
}

sealed class RateLimitResult {
    data class Allowed(val remaining: Int, val resetTime: Long) : RateLimitResult()
    data class Blocked(val retryAfter: Long) : RateLimitResult()
}

data class AuditEvent(
    val id: String,
    val userId: String?,
    val action: String,
    val resource: String,
    val details: Map<String, Any>,
    val timestamp: Long,
    val ipAddress: String,
    val userAgent: String
)

interface AuditLogRepository {
    suspend fun save(event: AuditEvent)
    suspend fun findByUserId(userId: String, startTime: Long, endTime: Long): List<AuditEvent>
    suspend fun findByAction(action: String, startTime: Long, endTime: Long): List<AuditEvent>
}

// ============================================================================
// SECURITY MIDDLEWARE
// ============================================================================

/**
 * Security Middleware for API Gateway
 */
class SecurityMiddleware(
    private val authenticationService: AuthenticationService,
    private val inputValidator: InputValidator,
    private val rateLimiter: MultiTierRateLimiter,
    private val auditLogger: AuditLogger
) {
    
    suspend fun processRequest(
        request: SecurityRequest
    ): SecurityResponse {
        
        // Rate limiting
        val rateLimitResult = rateLimiter.checkRateLimit(
            userId = request.userId,
            endpoint = request.endpoint
        )
        
        if (rateLimitResult is RateLimitResult.Blocked) {
            return SecurityResponse.Blocked(
                reason = "Rate limit exceeded",
                retryAfter = rateLimitResult.retryAfter
            )
        }
        
        // Input validation
        val validationResult = inputValidator.validateAndSanitize(
            input = request.body,
            type = InputType.TEXT
        )
        
        if (validationResult is ValidationResult.Invalid) {
            return SecurityResponse.Blocked(
                reason = "Invalid input: ${validationResult.reason}",
                retryAfter = 0
            )
        }
        
        // Authentication (if required)
        if (request.requiresAuth) {
            val token = request.headers["Authorization"]?.removePrefix("Bearer ")
            if (token == null) {
                return SecurityResponse.Blocked(
                    reason = "Authentication required",
                    retryAfter = 0
                )
            }
            
            // Validate token and get user
            // This would be implemented with the JWT token manager
        }
        
        return SecurityResponse.Allowed(
            sanitizedBody = (validationResult as ValidationResult.Valid).sanitizedInput
        )
    }
}

data class SecurityRequest(
    val userId: String?,
    val endpoint: String,
    val body: String,
    val headers: Map<String, String>,
    val requiresAuth: Boolean = true
)

sealed class SecurityResponse {
    data class Allowed(val sanitizedBody: String) : SecurityResponse()
    data class Blocked(val reason: String, val retryAfter: Long) : SecurityResponse()
}
