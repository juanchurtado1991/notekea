package app.notekea.editor.presentation.markdown

import androidx.compose.ui.text.TextRange
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR

/**
 * Builds the [ShownText] of the note's window: the Markdown marks are hidden, and a list's mark is a dot, on every
 * line but the revealed ones (the lines of the selection, while the text has the keyboard). The last one built is
 * kept: the cursor moving inside a line changes nothing, and the field does not lay its text out again.
 */
class ShownTextBuilder(private val highlighter: MarkdownHighlighter = MarkdownHighlighter()) {
    private var lastSpans: List<HighlightSpan> = emptyList()
    private var lastLines: IntRange? = null
    private var last: ShownText? = null

    /** [revealed] is the part of [note] whose lines show their marks, in the note's offsets; null hides them all. */
    fun of(
        note: CharSequence,
        window: TextRange,
        revealed: TextRange?
    ): ShownText {
        val source = note.substring(
            startIndex = window.min,
            endIndex = window.max
        )

        val lines = revealed?.let {
            linesOf(
                text = source,
                from = it.min - window.min,
                to = it.max - window.min
            )
        }

        val previous = last
        val isSameText = previous?.source == source

        if (previous != null &&
            isSameText &&
            previous.window == window &&
            lines == lastLines
        ) {
            return previous
        }

        if (!isSameText) lastSpans = highlighter.highlight(text = source)
        lastLines = lines

        return build(
            source = source,
            window = window,
            lines = lines
        ).also { last = it }
    }

    private fun build(
        source: String,
        window: TextRange,
        lines: IntRange?
    ): ShownText {
        val hidden = mutableListOf<IntRange>()
        val bullets = mutableListOf<Int>()
        val styled = mutableListOf<HighlightSpan>()

        // An embedded file's line stays as written: without its marks only its width or its label would show.
        val embeds = EMBED
            .findAll(input = source)
            .map { it.range }
            .toList()

        lastSpans.forEach { span ->
            val isRevealed = (lines != null && span.start in lines) ||
                embeds.any { span.start in it }

            when {
                isRevealed -> styled += span

                span.kind == HighlightKind.SYNTAX -> hidden += span.start until span.end

                span.kind == HighlightKind.MARKER -> styled += marker(
                    source = source,
                    span = span,
                    hidden = hidden,
                    bullets = bullets
                )

                else -> styled += span
            }
        }

        return ShownText(
            source = source,
            window = window,
            hidden = merged(ranges = hidden).map {
                it.first + window.min..it.last + window.min
            },
            bullets = bullets.map {
                it + window.min
            },
            styled = styled
        )
    }

    /**
     * A line's mark away from the cursor: a quote's stays, muted; a list's is a dot, or nothing before a task's box,
     * which the editor draws over the brackets; a number stays as written, in the text's color.
     */
    private fun marker(
        source: String,
        span: HighlightSpan,
        hidden: MutableList<IntRange>,
        bullets: MutableList<Int>
    ): List<HighlightSpan> {
        val mark = (span.start until span.end).firstOrNull {
            !source[it].isWhitespace()
        } ?: return emptyList()

        if (source[mark] == QUOTE_MARK) return listOf(span)
        if (source[mark] !in LIST_MARKS) return emptyList()

        val box = source
            .indexOf(
                char = TASK_BOX_START,
                startIndex = mark
            )
            .takeIf {
                it in mark until span.end
            }

        if (box == null) bullets += mark else hidden += mark until box

        return emptyList()
    }

    /** [ranges] in the text's order, with the ones that touch or overlap joined. */
    private fun merged(ranges: List<IntRange>): List<IntRange> {
        val joined = mutableListOf<IntRange>()

        ranges.sortedBy { it.first }.forEach { range ->
            val before = joined.lastOrNull()

            if (before != null && range.first <= before.last + 1) {
                joined[joined.lastIndex] = before.first..maxOf(before.last, range.last)
            } else {
                joined += range
            }
        }

        return joined
    }

    /** The whole lines of [text] that hold the characters [from] to [to]. */
    private fun linesOf(
        text: String,
        from: Int,
        to: Int
    ): IntRange {
        val first = from.coerceIn(
            minimumValue = 0,
            maximumValue = text.length
        )

        val last = to.coerceIn(
            minimumValue = first,
            maximumValue = text.length
        )

        val start = text.lastIndexOf(
            char = NEW_LINE_CHAR,
            startIndex = first - 1
        ) + 1

        val end = text
            .indexOf(
                char = NEW_LINE_CHAR,
                startIndex = last
            )
            .takeIf {
                it >= 0
            }
            ?: text.length

        return start..end
    }

    private companion object {
        val EMBED = Regex(pattern = """!\[\[[^\]\n]*]]|!\[[^\]\n]*]\([^)\n]*\)""")
        const val LIST_MARKS = "-*+"
        const val QUOTE_MARK = '>'
        const val TASK_BOX_START = '['
    }
}
