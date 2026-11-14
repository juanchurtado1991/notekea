package app.notekea.chat.domain

import app.notekea.chat.domain.conversation.Conversation

/** The chat of each vault, kept in the app's data, never inside the vault. */
interface ChatHistoryStore {
    /** The conversation of the vault at [vaultPath]; empty when there is none or it cannot be read. */
    suspend fun load(vaultPath: String): Conversation

    suspend fun save(
        vaultPath: String,
        conversation: Conversation
    )

    suspend fun clear(vaultPath: String)
}
