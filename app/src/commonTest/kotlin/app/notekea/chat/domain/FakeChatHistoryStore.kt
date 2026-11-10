package app.notekea.chat.domain

/** The chat histories in memory, by vault. */
class FakeChatHistoryStore : ChatHistoryStore {
    val turns = mutableMapOf<String, List<ChatTurn>>()

    override suspend fun load(vaultPath: String): List<ChatTurn> = turns[vaultPath].orEmpty()

    override suspend fun save(
        vaultPath: String,
        turns: List<ChatTurn>
    ) {
        this.turns[vaultPath] = turns
    }

    override suspend fun clear(vaultPath: String) {
        turns.remove(key = vaultPath)
    }
}
