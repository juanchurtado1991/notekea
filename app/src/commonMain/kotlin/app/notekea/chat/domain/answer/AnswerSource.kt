package app.notekea.chat.domain.answer

import app.notekea.core.notes.domain.RelativePath

/**
 * A passage an answer cites as `[number]`: the [note] it comes from, the [headingPath] above it, its [range] in the
 * note's body and its [text], exactly that part of the body, so the citation can be shown and checked.
 */
data class AnswerSource(
    val number: Int,
    val note: RelativePath,
    val headingPath: List<String>,
    val range: IntRange,
    val text: String
)
