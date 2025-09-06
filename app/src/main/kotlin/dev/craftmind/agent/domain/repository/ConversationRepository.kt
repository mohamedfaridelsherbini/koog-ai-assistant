package dev.craftmind.agent.domain.repository

import dev.craftmind.agent.domain.model.ConversationEntry

interface ConversationRepository {
    fun addEntry(entry: ConversationEntry)
    fun getRecentEntries(limit: Int): List<ConversationEntry>
    fun getAllEntries(): List<ConversationEntry>
    fun clear()
}