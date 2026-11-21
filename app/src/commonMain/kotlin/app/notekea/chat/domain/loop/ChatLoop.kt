package app.notekea.chat.domain.loop

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.memory.domain.MemoryNote
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.preferences.domain.AppPreferencesStore
import kotlinx.coroutines.flow.first

/**
 * The one loop as agent mode reaches it while it is a preview (`docs/agent-loop.md`, step 5): [runTurn], used when
 * Settings → General turns it on ([isOn]); today's agent otherwise, and always on the small model.
 */
class ChatLoop(
    val runTurn: RunTurnUseCase,
    private val preferencesStore: AppPreferencesStore,
    private val llmEngine: LlmEngine
) {
    suspend fun isOn(): Boolean {
        val isChosen = preferencesStore
            .observe()
            .first()
            .chatLoop

        return isChosen && !isSmall()
    }

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

    // The 2B repeats a refused call until the turn ends (`docs/agent-loop.md`, item 7): it keeps today's path.
    private suspend fun isSmall(): Boolean = when (val ready = llmEngine.readiness()) {
        is Outcome.Success -> ready.value.isSmall
        is Outcome.Failure -> false
    }
}
