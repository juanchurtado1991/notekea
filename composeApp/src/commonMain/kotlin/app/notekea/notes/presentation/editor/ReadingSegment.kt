package app.notekea.notes.presentation.editor

import app.notekea.core.domain.StringPatterns
import app.notekea.notes.domain.OutlineHeading

/**
 * A piece of a note as Read mode shows it: Markdown for the renderer, or a file embedded on a line of its own, which
 * Notekea draws itself (`ROADMAP.md` 1.8). Files embedded inside a paragraph stay in the Markdown, as links. A piece
 * of Markdown that starts with a heading of the outline knows its index ([Markdown.heading]), so the outline can
 * scroll to it (`ROADMAP.md` 1.9).
 */
sealed interface ReadingSegment {
    data class Markdown(
        val text: String,
        val heading: Int? = null
    ) : ReadingSegment

    data class Attachment(val view: AttachmentView) : ReadingSegment

    companion object {
        /**
         * [text] cut at the lines that hold only an embedded file of [attachments], and before every line of
         * [headings]; blank pieces are left out.
         */
        fun of(
            text: String,
            attachments: List<AttachmentView>,
            headings: List<OutlineHeading> = emptyList()
        ): List<ReadingSegment> {
            val segments = mutableListOf<ReadingSegment>()
            var start = 0
            var heading: Int? = null
            val addMarkdown = { end: Int ->
                text.substring(startIndex = start, endIndex = end).takeIf { it.isNotBlank() }?.let {
                    segments += Markdown(text = it, heading = heading)
                }
                heading = null
            }
            cutsOf(text = text, attachments = attachments, headings = headings).forEach { cut ->
                if (cut.lineStart < start) return@forEach
                addMarkdown(cut.lineStart)
                start = cut.lineStart
                when (val view = cut.view) {
                    null -> heading = cut.heading

                    else -> {
                        segments += Attachment(view = view)
                        start = minOf(cut.lineEnd + 1, text.length)
                    }
                }
            }
            addMarkdown(text.length)
            return segments
        }

        /** Where [text] is cut, in text order: the embedded files' own lines and the headings' lines. */
        private fun cutsOf(
            text: String,
            attachments: List<AttachmentView>,
            headings: List<OutlineHeading>
        ): List<Cut> {
            val files = attachments.filter { it.embed.isOwnLine && it.embed.range.last < text.length }.map { view ->
                val lineStart =
                    text.lastIndexOf(char = StringPatterns.NEW_LINE_CHAR, startIndex = view.embed.range.first - 1) + 1
                val lineEnd = text.indexOf(char = StringPatterns.NEW_LINE_CHAR, startIndex = view.embed.range.last)
                    .takeIf { it >= 0 } ?: text.length
                Cut(lineStart = lineStart, lineEnd = lineEnd, view = view)
            }
            val titles = headings.withIndex().filter { it.value.offset < text.length }.map { (index, heading) ->
                Cut(lineStart = heading.offset, lineEnd = heading.offset, heading = index)
            }
            return (files + titles).sortedBy { it.lineStart }
        }

        /** One place [of] cuts the text: an embedded file's line ([view]) or a heading's ([heading]). */
        private data class Cut(
            val lineStart: Int,
            val lineEnd: Int,
            val view: AttachmentView? = null,
            val heading: Int? = null
        )
    }
}
