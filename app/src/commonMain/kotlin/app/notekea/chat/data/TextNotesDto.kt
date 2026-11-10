package app.notekea.chat.data

import com.ghost.serialization.annotations.GhostSerialization

/** Notes made from a pasted text, as the model writes them (`TextNotesPrompt.SCHEMA`). */
@GhostSerialization
data class TextNotesDto(val notes: List<TextNoteDto> = emptyList())
