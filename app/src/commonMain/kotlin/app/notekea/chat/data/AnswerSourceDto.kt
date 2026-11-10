package app.notekea.chat.data

import com.ghost.serialization.annotations.GhostSerialization

/** A cited passage as the chat history keeps it: its note, headings, place (first and last character) and text. */
@GhostSerialization
data class AnswerSourceDto(
    val number: Int,
    val note: String,
    val first: Int,
    val last: Int,
    val text: String,
    val headingPath: List<String> = emptyList()
)
