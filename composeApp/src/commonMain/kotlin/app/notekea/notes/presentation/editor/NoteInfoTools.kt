package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.GetNoteStatsUseCase
import app.notekea.notes.domain.GetNoteTimesUseCase

/**
 * What the editor needs for the side panel's Note info: what the note holds and when its file was created and
 * modified; given together so its constructor stays short.
 */
class NoteInfoTools(
    val getNoteStats: GetNoteStatsUseCase,
    val getNoteTimes: GetNoteTimesUseCase
)
