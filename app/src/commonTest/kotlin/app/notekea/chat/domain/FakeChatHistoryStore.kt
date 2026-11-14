package app.notekea.chat.domain

import app.notekea.chat.domain.conversation.Conversation

/** The chats in memory, by vault: their [turns], and each whole conversation as it was last [kept]. */
class FakeChatHistoryStore : ChatHistoryStore {
    val turns = mutableMapOf<String, List<ChatTurn>>()
    val kept = mutableMapOf<String, Conversation>()

    override suspend fun load(vaultPath: String): Conversation =
        kept[vaultPath] ?: Conversation(turns = turns[vaultPath].orEmpty())

    override suspend fun save(
        vaultPath: String,
        conversation: Conversation
    ) {
        turns[vaultPath] = conversation.turns
        kept[vaultPath] = conversation
    }

    override suspend fun clear(vaultPath: String) {
        turns.remove(key = vaultPath)
        kept.remove(key = vaultPath)
    }
}
