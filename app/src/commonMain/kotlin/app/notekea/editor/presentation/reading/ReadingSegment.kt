package app.notekea.editor.presentation.reading

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.FenceTracker
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.notes.domain.OutlineHeading
import app.notekea.editor.presentation.attachments.AttachmentView

/**
 * A piece of a note as Read mode shows it: Markdown for the renderer, a file embedded on a line of its own, which
 * Notekea draws itself, or a query block drawn as its table. Files embedded inside a paragraph stay in the Markdown, as
 * links.
 */
sealed interface ReadingSegment {
    data class Markdown(val text: String) : ReadingSegment

    data class Attachment(val view: AttachmentView) : ReadingSegment

    data class Query(
        val text: String,
        val source: String
    ) : ReadingSegment

    companion object {
        /**
         * [text] cut at the lines that hold only an embedded file of [attachments], around its query blocks, and
         * before every line of [headings]; blank pieces are left out. [text] is the note's from [offset] on (a block
         * of the editor), and the embedded files are placed in the whole note: the ones outside [text] are left out.
         */
        fun of(
            text: String,
            attachments: List<AttachmentView>,
            headings: List<OutlineHeading> = emptyList(),
            offset: Int = 0
        ): List<ReadingSegment> {
            val segments = mutableListOf<ReadingSegment>()
            var start = 0

            val addMarkdown = { end: Int ->
                text
                    .substring(
                        startIndex = start,
                        endIndex = end
                    )
                    .takeIf { it.isNotBlank() }
                    ?.let { segments += Markdown(text = it) }
            }

            cutsOf(
                text = text,
                attachments = attachments,
                headings = headings,
                offset = offset
            ).forEach { cut ->
                if (cut.lineStart < start) return@forEach
                addMarkdown(cut.lineStart)
                start = cut.lineStart

                val piece = cut.view?.let { Attachment(view = it) } ?: cut.query

                if (piece != null) {
                    segments += piece
                    start = minOf(cut.lineEnd + 1, text.length)
                }
            }

            addMarkdown(text.length)

            return segments
        }

        /** Where [text] is cut, in text order: the embedded files' own lines and the headings' lines. */
        private fun cutsOf(
            text: String,
            attachments: List<AttachmentView>,
            headings: List<OutlineHeading>,
            offset: Int
        ): List<Cut> {
            val files = attachments.filter {
                it.embed.isOwnLine &&
                    it.embed.range.first >= offset &&
                    it.embed.range.last < offset + text.length
            }.map { view ->
                val lineStart = text.lastIndexOf(
                    char = StringPatterns.NEW_LINE_CHAR,
                    startIndex = view.embed.range.first - offset - 1
                ) + 1

                val lineEnd = text.indexOf(
                    char = StringPatterns.NEW_LINE_CHAR,
                    startIndex = view.embed.range.last - offset
                )
                    .takeIf {
                        it >= 0
                    } ?: text.length

                Cut(
                    lineStart = lineStart,
                    lineEnd = lineEnd,
                    view = view
                )
            }

            val titles = headings
                .filter {
                    it.offset < text.length
                }
                .map { heading ->
                    Cut(
                        lineStart = heading.offset,
                        lineEnd = heading.offset
                    )
                }

            return (files + titles + queriesOf(text = text)).sortedBy { it.lineStart }
        }

        /** The ```` ```query ```` blocks of [text], whole: a block that is not closed is no query yet. */
        private fun queriesOf(text: String): List<Cut> {
            if (MarkdownSyntax.QUERY_FENCE !in text) return emptyList()
            val tracker = FenceTracker()
            val cuts = mutableListOf<Cut>()
            var opened: Int? = null
            var lineStart = 0

            while (lineStart < text.length) {
                val lineEnd = text.indexOf(
                    char = StringPatterns.NEW_LINE_CHAR,
                    startIndex = lineStart
                )
                    .takeIf {
                        it >= 0
                    } ?: text.length

                val wasOpen = tracker.isOpen

                tracker.read(
                    text = text,
                    start = lineStart,
                    end = lineEnd
                )

                val line = text.substring(
                    startIndex = lineStart,
                    endIndex = lineEnd
                ).trim()

                val isQueryOpening =
                    !wasOpen &&
                        tracker.isOpen &&
                        line == MarkdownSyntax.QUERY_FENCE

                if (isQueryOpening) {
                    opened = lineStart
                }

                if (wasOpen && !tracker.isOpen) {
                    opened?.let {
                        cuts += queryCut(
                            text = text,
                            start = it,
                            lastLine = lineStart,
                            end = lineEnd
                        )
                    }

                    opened = null
                }

                lineStart = lineEnd + 1
            }

            return cuts
        }

        private fun queryCut(
            text: String,
            start: Int,
            lastLine: Int,
            end: Int
        ): Cut {
            val firstLineEnd = text.indexOf(
                char = StringPatterns.NEW_LINE_CHAR,
                startIndex = start
            )

            val yaml = text.substring(
                startIndex = firstLineEnd + 1,
                endIndex = lastLine
            )

            val source = text.substring(
                startIndex = start,
                endIndex = end
            )

            return Cut(
                lineStart = start,
                lineEnd = end,
                query = Query(
                    text = yaml,
                    source = source
                )
            )
        }

        /** One place [of] cuts the text: an embedded file's line ([view]), a query block, or a heading's line. */
        private data class Cut(
            val lineStart: Int,
            val lineEnd: Int,
            val view: AttachmentView? = null,
            val query: Query? = null
        )
    }
}
