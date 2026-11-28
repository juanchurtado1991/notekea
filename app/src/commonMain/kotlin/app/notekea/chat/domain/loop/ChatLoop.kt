package app.notekea.chat.domain.loop

import app.notekea.core.memory.domain.MemoryNote
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.preferences.domain.AppPreferencesStore
import kotlinx.coroutines.flow.first

/**
 * The one loop the chat works on every request of the whole vault with ([runTurn], `docs/agent-loop.md`), and what
 * it remembers between conversations.
 */
class ChatLoop(
    val runTurn: RunTurnUseCase,
    private val preferencesStore: AppPreferencesStore
) {
    /** Whether the chat remembers between conversations: Settings → Chat memory (`docs/agent-memory.md`). */
    suspend fun isRemembering(): Boolean = preferencesStore
        .observe()
        .first()
        .chatMemory

    /** [notes] as a turn sees them: without the memory's note when the memory is turned off. */
    suspend fun notesFor(notes: Collection<NoteEntry>): Collection<NoteEntry> {
        if (isRemembering()) return notes

        return notes.filter {
            it.file.path != MemoryNote.PATH
        }
    }
}
