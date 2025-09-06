package dev.craftmind.agent.infrastructure.repository

import dev.craftmind.agent.domain.model.ConversationEntry
import dev.craftmind.agent.domain.repository.ConversationRepository
import java.util.concurrent.CopyOnWriteArrayList

class InMemoryConversationRepository : ConversationRepository {
    private val entries = CopyOnWriteArrayList<ConversationEntry>()
    
    override fun addEntry(entry: ConversationEntry) {
        entries.add(entry)
    }
    
    override fun getRecentEntries(limit: Int): List<ConversationEntry> {
        return entries.takeLast(limit)
    }
    
    override fun getAllEntries(): List<ConversationEntry> {
        return entries.toList()
    }
    
    override fun clear() {
        entries.clear()
    }
}
