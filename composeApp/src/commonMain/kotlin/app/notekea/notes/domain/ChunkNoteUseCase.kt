package app.notekea.notes.domain

/**
 * Splits a note into the [NoteChunk]s the index embeds and the chat cites (`ROADMAP.md` 2.1): its body only (the
 * frontmatter is not text to search), one chunk per section ([NoteSections]), each range trimmed of the blank lines
 * around it.
 */
class ChunkNoteUseCase(
    private val noteSections: NoteSections
) {
    operator fun invoke(
        note: RelativePath,
        text: String
    ): List<NoteChunk> {
        val body = NoteDocument.fromText(text = text).body
        return noteSections.of(body = body).mapIndexed { index, section ->
            val range = trimmed(body = body, range = section.range)
            val chunkText = body.substring(range = range)
            NoteChunk(
                note = note,
                index = index,
                headingPath = section.headingPath,
                range = range,
                text = chunkText,
                tokens = TokenEstimate.of(text = chunkText)
            )
        }
    }

    /** [range] without the whitespace at its ends. */
    private fun trimmed(
        body: String,
        range: IntRange
    ): IntRange {
        var start = range.first
        var end = range.last
        while (start <= end && body[start].isWhitespace()) start++
        while (end >= start && body[end].isWhitespace()) end--
        return start..end
    }
}
