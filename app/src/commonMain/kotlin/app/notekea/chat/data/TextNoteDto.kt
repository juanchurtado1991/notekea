package app.notekea.chat.data

import com.ghost.serialization.annotations.GhostSerialization

/** One of them: its title and its Markdown text. */
@GhostSerialization
data class TextNoteDto(
    val title: String = "",
    val text: String = ""
)
