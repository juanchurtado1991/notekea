package app.notekea.cards.domain

import app.notekea.core.notes.domain.NoteSection

/**
 * A note cut into the parts its cards are made from, none longer than the model is given: its sections in order, as
 * many in a part as fit, and a section too long for one cut at its paragraphs, then at [max]. A long note's cards
 * all came from its first part (the cards' set, 2026-11-26).
 */
object CardsParts {
    fun of(
        text: String,
        sections: List<NoteSection>,
        max: Int
    ): List<String> {
        if (text.length <= max) return listOf(text)

        val pieces = sections
            .map { text.substring(range = it.range).trim() }
            .filter { it.isNotEmpty() }
            .flatMap {
                cut(
                    text = it,
                    max = max
                )
            }

        return packed(
            pieces = pieces,
            max = max
        )
    }

    // [text] in pieces of [max] characters at most: at its paragraphs, and a paragraph too long at [max].
    private fun cut(
        text: String,
        max: Int
    ): List<String> {
        if (text.length <= max) return listOf(text)

        val paragraphs = text
            .split(PARAGRAPH)
            .flatMap { it.chunked(size = max) }

        return packed(
            pieces = paragraphs,
            max = max
        )
    }

    // Consecutive [pieces] joined while they fit in [max].
    private fun packed(
        pieces: List<String>,
        max: Int
    ): List<String> = pieces
        .fold(initial = emptyList()) { parts, piece ->
            val last = parts.lastOrNull()
            val isFitting = last != null && last.length + PARAGRAPH.length + piece.length <= max

            if (isFitting) parts.dropLast(n = 1) + (last + PARAGRAPH + piece) else parts + piece
        }

    private const val PARAGRAPH = "\n\n"
}
