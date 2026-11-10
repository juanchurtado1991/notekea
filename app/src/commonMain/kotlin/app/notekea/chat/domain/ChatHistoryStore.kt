package app.notekea.chat.domain

/** The chat history of each vault, kept in the app's data, never inside the vault. */
interface ChatHistoryStore {
    /** The turns of the vault at [vaultPath], oldest first; none when there are none or they cannot be read. */
    suspend fun load(vaultPath: String): List<ChatTurn>

    suspend fun save(
        vaultPath: String,
        turns: List<ChatTurn>
    )

    suspend fun clear(vaultPath: String)
}
