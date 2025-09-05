/*
 * Event-Driven Architecture for Koog Agent Deep Research
 * Implementing event-driven patterns for loose coupling
 */
package dev.craftmind.agent.architecture

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Event-Driven Architecture Implementation
 * 
 * Components:
 * - Event Bus: Kafka/RabbitMQ for inter-service communication
 * - Event Sourcing: Store conversation state as events
 * - CQRS: Separate read/write models for performance
 * - Saga Pattern: Handle distributed transactions
 */

// ============================================================================
// EVENT BUS
// ============================================================================

/**
 * Event Bus - Central hub for event communication
 */
interface EventBus {
    suspend fun publish(event: DomainEvent)
    suspend fun subscribe(eventType: Class<out DomainEvent>, handler: EventHandler)
    suspend fun unsubscribe(eventType: Class<out DomainEvent>, handler: EventHandler)
}

/**
 * In-Memory Event Bus Implementation
 */
class InMemoryEventBus : EventBus {
    private val handlers = ConcurrentHashMap<Class<out DomainEvent>, MutableList<EventHandler>>()
    
    override suspend fun publish(event: DomainEvent) {
        val eventHandlers = handlers[event::class.java] ?: return
        
        // Publish to all handlers asynchronously
        eventHandlers.forEach { handler ->
            GlobalScope.launch {
                try {
                    handler.handle(event)
                } catch (e: Exception) {
                    // Log error but don't fail other handlers
                    println("Error handling event ${event::class.simpleName}: ${e.message}")
                }
            }
        }
    }
    
    override suspend fun subscribe(eventType: Class<out DomainEvent>, handler: EventHandler) {
        handlers.computeIfAbsent(eventType) { CopyOnWriteArrayList() }.add(handler)
    }
    
    override suspend fun unsubscribe(eventType: Class<out DomainEvent>, handler: EventHandler) {
        handlers[eventType]?.remove(handler)
    }
}

/**
 * Kafka Event Bus Implementation
 */
class KafkaEventBus(
    private val kafkaProducer: KafkaProducer,
    private val kafkaConsumer: KafkaConsumer
) : EventBus {
    
    override suspend fun publish(event: DomainEvent) {
        kafkaProducer.send(event)
    }
    
    override suspend fun subscribe(eventType: Class<out DomainEvent>, handler: EventHandler) {
        kafkaConsumer.subscribe(eventType, handler)
    }
    
    override suspend fun unsubscribe(eventType: Class<out DomainEvent>, handler: EventHandler) {
        kafkaConsumer.unsubscribe(eventType, handler)
    }
}

// ============================================================================
// DOMAIN EVENTS
// ============================================================================

/**
 * Base Domain Event
 */
abstract class DomainEvent(
    val eventId: String = generateEventId(),
    val timestamp: Long = System.currentTimeMillis(),
    val version: Int = 1
) {
    abstract val eventType: String
    abstract val aggregateId: String
}

/**
 * Conversation Events
 */
data class ConversationCreatedEvent(
    override val aggregateId: String,
    val userId: String,
    val title: String
) : DomainEvent() {
    override val eventType = "conversation.created"
}

data class MessageAddedEvent(
    override val aggregateId: String,
    val messageId: String,
    val content: String,
    val role: String,
    val userId: String
) : DomainEvent() {
    override val eventType = "message.added"
}

data class ConversationDeletedEvent(
    override val aggregateId: String,
    val userId: String
) : DomainEvent() {
    override val eventType = "conversation.deleted"
}

/**
 * AI Events
 */
data class AIResponseGeneratedEvent(
    override val aggregateId: String,
    val conversationId: String,
    val modelId: String,
    val response: String,
    val processingTime: Long,
    val tokensUsed: Int
) : DomainEvent() {
    override val eventType = "ai.response.generated"
}

data class ModelSwitchedEvent(
    override val aggregateId: String,
    val userId: String,
    val oldModelId: String,
    val newModelId: String
) : DomainEvent() {
    override val eventType = "model.switched"
}

/**
 * User Events
 */
data class UserRegisteredEvent(
    override val aggregateId: String,
    val email: String,
    val name: String
) : DomainEvent() {
    override val eventType = "user.registered"
}

data class UserLoggedInEvent(
    override val aggregateId: String,
    val sessionId: String,
    val ipAddress: String
) : DomainEvent() {
    override val eventType = "user.logged_in"
}

/**
 * File Events
 */
data class FileUploadedEvent(
    override val aggregateId: String,
    val fileId: String,
    val userId: String,
    val fileName: String,
    val fileSize: Long
) : DomainEvent() {
    override val eventType = "file.uploaded"
}

data class FileDeletedEvent(
    override val aggregateId: String,
    val fileId: String,
    val userId: String
) : DomainEvent() {
    override val eventType = "file.deleted"
}

/**
 * System Events
 */
data class SystemHealthChangedEvent(
    override val aggregateId: String,
    val status: String,
    val metrics: Map<String, Any>
) : DomainEvent() {
    override val eventType = "system.health.changed"
}

data class AlertTriggeredEvent(
    override val aggregateId: String,
    val alertType: String,
    val severity: String,
    val message: String
) : DomainEvent() {
    override val eventType = "alert.triggered"
}

// ============================================================================
// EVENT HANDLERS
// ============================================================================

/**
 * Event Handler Interface
 */
interface EventHandler {
    suspend fun handle(event: DomainEvent)
}

/**
 * Conversation Event Handlers
 */
class ConversationEventHandler(
    private val conversationProjection: ConversationProjection,
    private val searchIndex: SearchIndex
) : EventHandler {
    
    override suspend fun handle(event: DomainEvent) {
        when (event) {
            is ConversationCreatedEvent -> handleConversationCreated(event)
            is MessageAddedEvent -> handleMessageAdded(event)
            is ConversationDeletedEvent -> handleConversationDeleted(event)
        }
    }
    
    private suspend fun handleConversationCreated(event: ConversationCreatedEvent) {
        conversationProjection.createProjection(event.aggregateId, event.userId, event.title)
        searchIndex.indexConversation(event.aggregateId, event.title)
    }
    
    private suspend fun handleMessageAdded(event: MessageAddedEvent) {
        conversationProjection.addMessage(
            event.aggregateId,
            event.messageId,
            event.content,
            event.role
        )
        searchIndex.indexMessage(event.aggregateId, event.messageId, event.content)
    }
    
    private suspend fun handleConversationDeleted(event: ConversationDeletedEvent) {
        conversationProjection.deleteProjection(event.aggregateId)
        searchIndex.removeConversation(event.aggregateId)
    }
}

/**
 * AI Event Handlers
 */
class AIEventHandler(
    private val metricsCollector: MetricsCollector,
    private val notificationService: NotificationService
) : EventHandler {
    
    override suspend fun handle(event: DomainEvent) {
        when (event) {
            is AIResponseGeneratedEvent -> handleResponseGenerated(event)
            is ModelSwitchedEvent -> handleModelSwitched(event)
        }
    }
    
    private suspend fun handleResponseGenerated(event: AIResponseGeneratedEvent) {
        // Collect metrics
        metricsCollector.recordResponseTime(event.processingTime)
        metricsCollector.recordTokensUsed(event.tokensUsed)
        
        // Send notification if needed
        if (event.processingTime > 10000) { // 10 seconds
            notificationService.sendRealTimeUpdate(
                event.aggregateId,
                RealTimeUpdate("slow_response", "Response took longer than expected")
            )
        }
    }
    
    private suspend fun handleModelSwitched(event: ModelSwitchedEvent) {
        // Log model switch
        metricsCollector.recordModelSwitch(event.oldModelId, event.newModelId)
        
        // Notify user
        notificationService.sendRealTimeUpdate(
            event.aggregateId,
            RealTimeUpdate("model_switched", "Model switched to ${event.newModelId}")
        )
    }
}

// ============================================================================
// EVENT SOURCING
// ============================================================================

/**
 * Event Store - Stores all domain events
 */
interface EventStore {
    suspend fun saveEvents(aggregateId: String, events: List<DomainEvent>, expectedVersion: Int)
    suspend fun getEvents(aggregateId: String): List<DomainEvent>
    suspend fun getEvents(aggregateId: String, fromVersion: Int): List<DomainEvent>
}

/**
 * In-Memory Event Store
 */
class InMemoryEventStore : EventStore {
    private val events = ConcurrentHashMap<String, MutableList<DomainEvent>>()
    private val versions = ConcurrentHashMap<String, Int>()
    
    override suspend fun saveEvents(aggregateId: String, events: List<DomainEvent>, expectedVersion: Int) {
        val currentVersion = versions[aggregateId] ?: 0
        if (currentVersion != expectedVersion) {
            throw ConcurrencyException("Expected version $expectedVersion, but was $currentVersion")
        }
        
        this.events.computeIfAbsent(aggregateId) { mutableListOf() }.addAll(events)
        versions[aggregateId] = currentVersion + events.size
    }
    
    override suspend fun getEvents(aggregateId: String): List<DomainEvent> {
        return events[aggregateId] ?: emptyList()
    }
    
    override suspend fun getEvents(aggregateId: String, fromVersion: Int): List<DomainEvent> {
        val allEvents = getEvents(aggregateId)
        return allEvents.drop(fromVersion)
    }
}

/**
 * Event Sourced Aggregate
 */
abstract class EventSourcedAggregate(
    val id: String,
    private var version: Int = 0
) {
    private val uncommittedEvents = mutableListOf<DomainEvent>()
    
    protected fun apply(event: DomainEvent) {
        when (event) {
            is ConversationCreatedEvent -> handle(event)
            is MessageAddedEvent -> handle(event)
            is AIResponseGeneratedEvent -> handle(event)
            // Add other event types
        }
        uncommittedEvents.add(event)
    }
    
    fun getUncommittedEvents(): List<DomainEvent> = uncommittedEvents.toList()
    
    fun markEventsAsCommitted() {
        uncommittedEvents.clear()
    }
    
    fun getVersion(): Int = version
    
    protected fun setVersion(version: Int) {
        this.version = version
    }
    
    // Abstract methods to be implemented by concrete aggregates
    protected abstract fun handle(event: DomainEvent)
}

// ============================================================================
// CQRS (Command Query Responsibility Segregation)
// ============================================================================

/**
 * Command Side - Handles write operations
 */
interface CommandHandler<in T : Command> {
    suspend fun handle(command: T): CommandResult
}

/**
 * Query Side - Handles read operations
 */
interface QueryHandler<in T : Query, out R> {
    suspend fun handle(query: T): R
}

/**
 * Command Bus
 */
class CommandBus(
    private val handlers: Map<Class<out Command>, CommandHandler<Command>>
) {
    suspend fun execute(command: Command): CommandResult {
        val handler = handlers[command::class.java]
            ?: throw NoHandlerFoundException("No handler for command ${command::class.simpleName}")
        
        return handler.handle(command)
    }
}

/**
 * Query Bus
 */
class QueryBus(
    private val handlers: Map<Class<out Query>, QueryHandler<Query, Any>>
) {
    suspend fun <T> execute(query: Query): T {
        val handler = handlers[query::class.java]
            ?: throw NoHandlerFoundException("No handler for query ${query::class.simpleName}")
        
        @Suppress("UNCHECKED_CAST")
        return handler.handle(query) as T
    }
}

// ============================================================================
// SAGA PATTERN
// ============================================================================

/**
 * Saga - Manages distributed transactions
 */
abstract class Saga(
    val sagaId: String,
    val status: SagaStatus = SagaStatus.STARTED
) {
    abstract suspend fun execute(): SagaResult
    abstract suspend fun compensate(): SagaResult
}

/**
 * Chat Processing Saga
 */
class ChatProcessingSaga(
    sagaId: String,
    private val conversationId: String,
    private val message: String,
    private val userId: String
) : Saga(sagaId) {
    
    private val steps = listOf(
        ValidateMessageStep(),
        GenerateAIResponseStep(),
        SaveMessageStep(),
        UpdateContextStep(),
        SendNotificationStep()
    )
    
    override suspend fun execute(): SagaResult {
        val executedSteps = mutableListOf<SagaStep>()
        
        try {
            for (step in steps) {
                val result = step.execute(this)
                if (!result.success) {
                    // Compensate executed steps
                    compensateSteps(executedSteps.reversed())
                    return SagaResult(false, result.error)
                }
                executedSteps.add(step)
            }
            
            return SagaResult(true, "Saga completed successfully")
        } catch (e: Exception) {
            compensateSteps(executedSteps.reversed())
            return SagaResult(false, e.message ?: "Unknown error")
        }
    }
    
    override suspend fun compensate(): SagaResult {
        // Implement compensation logic
        return SagaResult(true, "Compensation completed")
    }
    
    private suspend fun compensateSteps(steps: List<SagaStep>) {
        for (step in steps) {
            try {
                step.compensate(this)
            } catch (e: Exception) {
                // Log compensation error but continue
                println("Error compensating step ${step::class.simpleName}: ${e.message}")
            }
        }
    }
}

// ============================================================================
// DATA CLASSES AND INTERFACES
// ============================================================================

data class RealTimeUpdate(
    val type: String,
    val message: String,
    val data: Map<String, Any> = emptyMap()
)

data class CommandResult(
    val success: Boolean,
    val error: String? = null,
    val data: Any? = null
)

data class SagaResult(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)

enum class SagaStatus {
    STARTED, RUNNING, COMPLETED, FAILED, COMPENSATING
}

// ============================================================================
// EXCEPTIONS
// ============================================================================

class ConcurrencyException(message: String) : Exception(message)
class NoHandlerFoundException(message: String) : Exception(message)

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

private fun generateEventId(): String = "evt_${System.currentTimeMillis()}_${(0..9999).random()}"

// ============================================================================
// INTERFACES FOR DEPENDENCIES
// ============================================================================

interface KafkaProducer {
    suspend fun send(event: DomainEvent)
}

interface KafkaConsumer {
    suspend fun subscribe(eventType: Class<out DomainEvent>, handler: EventHandler)
    suspend fun unsubscribe(eventType: Class<out DomainEvent>, handler: EventHandler)
}

interface ConversationProjection {
    suspend fun createProjection(conversationId: String, userId: String, title: String)
    suspend fun addMessage(conversationId: String, messageId: String, content: String, role: String)
    suspend fun deleteProjection(conversationId: String)
}

interface SearchIndex {
    suspend fun indexConversation(conversationId: String, title: String)
    suspend fun indexMessage(conversationId: String, messageId: String, content: String)
    suspend fun removeConversation(conversationId: String)
}

interface MetricsCollector {
    suspend fun recordResponseTime(time: Long)
    suspend fun recordTokensUsed(tokens: Int)
    suspend fun recordModelSwitch(oldModel: String, newModel: String)
}

interface NotificationService {
    suspend fun sendRealTimeUpdate(userId: String, update: RealTimeUpdate)
}

// ============================================================================
// SAGA STEPS
// ============================================================================

abstract class SagaStep {
    abstract suspend fun execute(saga: Saga): SagaResult
    abstract suspend fun compensate(saga: Saga): SagaResult
}

class ValidateMessageStep : SagaStep() {
    override suspend fun execute(saga: Saga): SagaResult {
        // Implement message validation
        return SagaResult(true, "Message validated")
    }
    
    override suspend fun compensate(saga: Saga): SagaResult {
        // Implement compensation
        return SagaResult(true, "Validation compensation completed")
    }
}

class GenerateAIResponseStep : SagaStep() {
    override suspend fun execute(saga: Saga): SagaResult {
        // Implement AI response generation
        return SagaResult(true, "AI response generated")
    }
    
    override suspend fun compensate(saga: Saga): SagaResult {
        // Implement compensation
        return SagaResult(true, "AI response compensation completed")
    }
}

class SaveMessageStep : SagaStep() {
    override suspend fun execute(saga: Saga): SagaResult {
        // Implement message saving
        return SagaResult(true, "Message saved")
    }
    
    override suspend fun compensate(saga: Saga): SagaResult {
        // Implement compensation
        return SagaResult(true, "Message save compensation completed")
    }
}

class UpdateContextStep : SagaStep() {
    override suspend fun execute(saga: Saga): SagaResult {
        // Implement context update
        return SagaResult(true, "Context updated")
    }
    
    override suspend fun compensate(saga: Saga): SagaResult {
        // Implement compensation
        return SagaResult(true, "Context update compensation completed")
    }
}

class SendNotificationStep : SagaStep() {
    override suspend fun execute(saga: Saga): SagaResult {
        // Implement notification sending
        return SagaResult(true, "Notification sent")
    }
    
    override suspend fun compensate(saga: Saga): SagaResult {
        // Implement compensation
        return SagaResult(true, "Notification compensation completed")
    }
}

// ============================================================================
// COMMAND AND QUERY INTERFACES
// ============================================================================

interface Command
interface Query

data class CreateConversationCommand(
    val userId: String,
    val title: String
) : Command

data class SendMessageCommand(
    val conversationId: String,
    val message: String,
    val userId: String
) : Command

data class GetConversationQuery(
    val conversationId: String,
    val userId: String
) : Query

data class SearchConversationsQuery(
    val userId: String,
    val query: String,
    val limit: Int = 10
) : Query
