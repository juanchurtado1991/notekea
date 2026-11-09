package app.notekea.editor.presentation.noteinfo

import app.notekea.editor.domain.GetNoteStatsUseCase
import app.notekea.editor.domain.NoteTimesReader

/**
 * What the editor needs for the side panel's Note info: what the note holds and when its file was created and
 * modified; given together so its constructor stays short.
 */
class NoteInfoTools(
    val getNoteStats: GetNoteStatsUseCase,
    val noteTimesReader: NoteTimesReader
)
