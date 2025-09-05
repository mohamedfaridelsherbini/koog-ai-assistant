/*
 * Perfect Architecture Design for Koog Agent Deep Research
 * Layered Architecture with Clear Separation of Concerns
 */
package dev.craftmind.agent.architecture

/**
 * Architecture Layers for Koog Agent Deep Research
 * 
 * ┌─────────────────────────────────────────┐
 * │           Presentation Layer            │  ← Web UI, API Gateway, WebSocket
 * ├─────────────────────────────────────────┤
 * │            Application Layer            │  ← Use Cases, Services, DTOs
 * ├─────────────────────────────────────────┤
 * │              Domain Layer               │  ← Business Logic, Entities, Value Objects
 * ├─────────────────────────────────────────┤
 * │           Infrastructure Layer          │  ← Data Access, External Services, Repositories
 * └─────────────────────────────────────────┘
 */

// ============================================================================
// PRESENTATION LAYER
// ============================================================================

/**
 * Presentation Layer - Handles all external communication
 * - REST API endpoints
 * - WebSocket connections
 * - Web UI controllers
 * - Authentication/Authorization
 */
interface PresentationLayer {
    fun handleHttpRequest(request: HttpRequest): HttpResponse
    fun handleWebSocketConnection(connection: WebSocketConnection)
    fun serveStaticContent(path: String): StaticContent
}

/**
 * API Gateway - Central entry point for all requests
 */
class ApiGateway(
    private val authService: AuthService,
    private val rateLimiter: RateLimiter,
    private val requestRouter: RequestRouter
) : PresentationLayer {
    
    override fun handleHttpRequest(request: HttpRequest): HttpResponse {
        return try {
            // Authentication
            val user = authService.authenticate(request)
            
            // Rate limiting
            if (!rateLimiter.isAllowed(user.id, request.endpoint)) {
                return HttpResponse(429, "Rate limit exceeded")
            }
            
            // Route to appropriate service
            requestRouter.route(request, user)
        } catch (e: Exception) {
            HttpResponse(500, "Internal server error")
        }
    }
    
    override fun handleWebSocketConnection(connection: WebSocketConnection) {
        // WebSocket handling logic
    }
    
    override fun serveStaticContent(path: String): StaticContent {
        // Static content serving
        return StaticContent("", "text/html")
    }
}

// ============================================================================
// APPLICATION LAYER
// ============================================================================

/**
 * Application Layer - Contains use cases and application services
 * - Use case implementations
 * - Service orchestration
 * - DTOs and mappers
 * - Transaction management
 */
interface ApplicationLayer {
    fun executeUseCase(useCase: UseCase): UseCaseResult
    fun handleEvent(event: DomainEvent)
    fun validateRequest(request: Request): ValidationResult
}

/**
 * Use Case Base Class
 */
abstract class UseCase<in T, out R> {
    abstract fun execute(input: T): R
    abstract fun validate(input: T): ValidationResult
}

/**
 * Chat Use Case
 */
class ChatUseCase(
    private val conversationService: ConversationService,
    private val aiService: AIService,
    private val eventBus: EventBus
) : UseCase<ChatRequest, ChatResponse>() {
    
    override fun execute(input: ChatRequest): ChatResponse {
        // Validate input
        val validation = validate(input)
        if (!validation.isValid) {
            throw ValidationException(validation.message)
        }
        
        // Get conversation context
        val context = conversationService.getContext(input.conversationId)
        
        // Generate AI response
        val response = aiService.generateResponse(input.message, context)
        
        // Save conversation
        conversationService.saveMessage(input.conversationId, input.message, response)
        
        // Publish event
        eventBus.publish(ChatMessageProcessedEvent(input.conversationId, response))
        
        return ChatResponse(response, input.conversationId)
    }
    
    override fun validate(input: ChatRequest): ValidationResult {
        return ValidationUtils.isValidInputText(input.message)
    }
}

// ============================================================================
// DOMAIN LAYER
// ============================================================================

/**
 * Domain Layer - Contains business logic and domain entities
 * - Domain entities
 * - Value objects
 * - Domain services
 * - Business rules
 */
interface DomainLayer {
    fun processBusinessLogic(entity: DomainEntity): BusinessResult
    fun validateBusinessRules(rules: List<BusinessRule>): ValidationResult
}

/**
 * Conversation Entity
 */
data class Conversation(
    val id: ConversationId,
    val userId: UserId,
    val messages: List<Message>,
    val context: ConversationContext,
    val createdAt: Timestamp,
    val updatedAt: Timestamp
) : DomainEntity {
    
    fun addMessage(message: Message): Conversation {
        return copy(
            messages = messages + message,
            updatedAt = Timestamp.now()
        )
    }
    
    fun getContextWindow(maxTokens: Int): List<Message> {
        // Intelligent context truncation logic
        return messages.takeLast(maxTokens)
    }
}

/**
 * Message Value Object
 */
data class Message(
    val id: MessageId,
    val content: String,
    val role: MessageRole,
    val timestamp: Timestamp,
    val metadata: MessageMetadata
) : ValueObject

/**
 * AI Model Entity
 */
data class AIModel(
    val id: ModelId,
    val name: String,
    val provider: ModelProvider,
    val capabilities: Set<ModelCapability>,
    val status: ModelStatus,
    val metadata: ModelMetadata
) : DomainEntity {
    
    fun canHandle(request: AIRequest): Boolean {
        return capabilities.containsAll(request.requiredCapabilities)
    }
    
    fun updateStatus(status: ModelStatus): AIModel {
        return copy(status = status)
    }
}

// ============================================================================
// INFRASTRUCTURE LAYER
// ============================================================================

/**
 * Infrastructure Layer - Handles external concerns
 * - Database access
 * - External API calls
 * - File system operations
 * - Caching
 */
interface InfrastructureLayer {
    fun save(entity: DomainEntity): Unit
    fun findById(id: EntityId): DomainEntity?
    fun findByCriteria(criteria: Criteria): List<DomainEntity>
    fun delete(id: EntityId): Unit
}

/**
 * Repository Pattern Implementation
 */
interface Repository<T : DomainEntity, ID : EntityId> {
    fun save(entity: T): T
    fun findById(id: ID): T?
    fun findAll(): List<T>
    fun delete(id: ID): Unit
    fun findByCriteria(criteria: Criteria): List<T>
}

/**
 * Conversation Repository
 */
class ConversationRepository(
    private val database: Database,
    private val cache: Cache
) : Repository<Conversation, ConversationId> {
    
    override fun save(entity: Conversation): Conversation {
        // Save to database
        val saved = database.save(entity)
        
        // Update cache
        cache.put(entity.id, saved)
        
        return saved
    }
    
    override fun findById(id: ConversationId): Conversation? {
        // Try cache first
        cache.get<Conversation>(id)?.let { return it }
        
        // Fallback to database
        val conversation = database.findById(id)
        conversation?.let { cache.put(id, it) }
        
        return conversation
    }
    
    override fun findAll(): List<Conversation> {
        return database.findAll()
    }
    
    override fun delete(id: ConversationId) {
        database.delete(id)
        cache.evict(id)
    }
    
    override fun findByCriteria(criteria: Criteria): List<Conversation> {
        return database.findByCriteria(criteria)
    }
}

// ============================================================================
// CROSS-CUTTING CONCERNS
// ============================================================================

/**
 * Cross-cutting concerns that span all layers
 */
interface CrossCuttingConcerns {
    fun log(level: LogLevel, message: String, context: Map<String, Any> = emptyMap())
    fun measure(operation: String, block: () -> Any): Any
    fun validateSecurity(request: Request): SecurityResult
    fun handleError(error: Throwable): ErrorResponse
}

/**
 * Aspect-Oriented Programming Implementation
 */
class AOPHandler : CrossCuttingConcerns {
    
    override fun log(level: LogLevel, message: String, context: Map<String, Any>) {
        Logger.log(level, message, context)
    }
    
    override fun measure(operation: String, block: () -> Any): Any {
        return PerformanceUtils.measurePerformance(operation) {
            block()
        }
    }
    
    override fun validateSecurity(request: Request): SecurityResult {
        return SecurityValidator.validate(request)
    }
    
    override fun handleError(error: Throwable): ErrorResponse {
        return ErrorHandler.handle(error)
    }
}

// ============================================================================
// DATA TRANSFER OBJECTS
// ============================================================================

/**
 * DTOs for data transfer between layers
 */
data class ChatRequest(
    val conversationId: ConversationId,
    val message: String,
    val userId: UserId,
    val metadata: RequestMetadata
)

data class ChatResponse(
    val response: String,
    val conversationId: ConversationId,
    val timestamp: Timestamp = Timestamp.now()
)

data class HttpRequest(
    val method: HttpMethod,
    val endpoint: String,
    val headers: Map<String, String>,
    val body: String?,
    val queryParams: Map<String, String>
)

data class HttpResponse(
    val statusCode: Int,
    val body: String,
    val headers: Map<String, String> = emptyMap()
)

// ============================================================================
// VALUE OBJECTS AND ENTITIES
// ============================================================================

/**
 * Value Objects - Immutable objects that represent concepts
 */
abstract class ValueObject
abstract class EntityId
abstract class DomainEntity

typealias ConversationId = String
typealias UserId = String
typealias MessageId = String
typealias ModelId = String
typealias Timestamp = Long

enum class MessageRole { USER, ASSISTANT, SYSTEM }
enum class ModelProvider { OLLAMA, OPENAI, ANTHROPIC, GOOGLE }
enum class ModelStatus { ACTIVE, INACTIVE, LOADING, ERROR }
enum class HttpMethod { GET, POST, PUT, DELETE, PATCH }
enum class LogLevel { DEBUG, INFO, WARN, ERROR }

// ============================================================================
// UTILITY CLASSES
// ============================================================================

object Timestamp {
    fun now(): Long = System.currentTimeMillis()
}

data class StaticContent(val content: String, val mimeType: String)
data class ValidationResult(val isValid: Boolean, val message: String)
data class BusinessResult(val success: Boolean, val data: Any?)
data class SecurityResult(val isSecure: Boolean, val violations: List<String>)
data class ErrorResponse(val code: String, val message: String, val details: Map<String, Any>)

// ============================================================================
// EXCEPTIONS
// ============================================================================

class ValidationException(message: String) : Exception(message)
class BusinessRuleViolationException(message: String) : Exception(message)
class SecurityViolationException(message: String) : Exception(message)
