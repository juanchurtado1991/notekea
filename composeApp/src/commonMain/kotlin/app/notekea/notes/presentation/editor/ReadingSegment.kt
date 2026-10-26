package app.notekea.notes.presentation.editor

import app.notekea.core.domain.StringPatterns

/**
 * A piece of a note as Read mode shows it: Markdown for the renderer, or a file embedded on a line of its own, which
 * Notekea draws itself (`ROADMAP.md` 1.8). Files embedded inside a paragraph stay in the Markdown, as links.
 */
sealed interface ReadingSegment {
    data class Markdown(val text: String) : ReadingSegment

    data class Attachment(val view: AttachmentView) : ReadingSegment

    companion object {
        /** [text] cut at the lines that hold only an embedded file of [attachments]; blank pieces are left out. */
        fun of(
            text: String,
            attachments: List<AttachmentView>
        ): List<ReadingSegment> {
            val segments = mutableListOf<ReadingSegment>()
            var start = 0
            attachments.filter { it.embed.isOwnLine && it.embed.range.last < text.length }
                .sortedBy { it.embed.range.first }
                .forEach { view ->
                    val lineStart =
                        text.lastIndexOf(char = StringPatterns.NEW_LINE_CHAR, startIndex = view.embed.range.first - 1) +
                            1
                    val lineEnd = text.indexOf(char = StringPatterns.NEW_LINE_CHAR, startIndex = view.embed.range.last)
                        .takeIf { it >= 0 } ?: text.length
                    if (lineStart < start) return@forEach
                    text.substring(startIndex = start, endIndex = lineStart).takeIf { it.isNotBlank() }?.let {
                        segments += Markdown(text = it)
                    }
                    segments += Attachment(view = view)
                    start = minOf(lineEnd + 1, text.length)
                }
            text.substring(startIndex = start).takeIf { it.isNotBlank() }?.let { segments += Markdown(text = it) }
            return segments
        }
    }
}
