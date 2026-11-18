package app.notekea.chat.domain.loop

import app.notekea.core.memory.domain.MemoryLine
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/**
 * Where a turn's frame comes from (`docs/agent-frame.md`): [TurnFrames] in the app, which asks the model; a fixed
 * frame in a test, which needs no second reply from its fake model. Null is no frame: nothing is held back for one.
 */
fun interface FrameSource {
    suspend fun of(
        task: String,
        vaultPath: String,
        notes: Collection<NoteEntry>,
        memory: List<MemoryLine>,
        openNote: RelativePath?
    ): TaskFrame?
}
