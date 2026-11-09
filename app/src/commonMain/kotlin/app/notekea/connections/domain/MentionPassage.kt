package app.notekea.connections.domain

import app.notekea.core.notes.domain.MarkdownLineStripper

/**
 * The passage a mention shows: the line of [body] that holds [range], as a reader sees it
 * ([MarkdownLineStripper]), shortened to about [MAX_LENGTH] characters around the mention, with an ellipsis where it
 * was cut.
 */
internal object MentionPassage {
    private const val ELLIPSIS = "…"
    private const val LEAD = 48
    private const val MAX_LENGTH = 180
    private val stripper = MarkdownLineStripper()

    fun of(
        body: String,
        range: IntRange
    ): Pair<String, IntRange> {
        val lineStart = body.lastIndexOf(
            char = '\n',
            startIndex = range.first - 1
        ) + 1

        val lineEnd = body.indexOf(
            char = '\n',
            startIndex = range.last
        ).takeIf {
            it >= 0
        } ?: body.length

        val plain = stripper.strip(
            line = body.substring(
                startIndex = lineStart,
                endIndex = lineEnd
            ),
            ranges = listOf(range.first - lineStart..range.last - lineStart)
        )

        val highlight = plain.ranges.firstOrNull() ?: IntRange.EMPTY

        return shortened(
            text = plain.text,
            highlight = highlight
        )
    }

    /** [text] cut to [MAX_LENGTH] around [highlight], at spaces, with the highlight moved along. */
    private fun shortened(
        text: String,
        highlight: IntRange
    ): Pair<String, IntRange> {
        if (text.length <= MAX_LENGTH) {
            return text.trim() to highlight.shifted(by = -(text.length - text.trimStart().length))
        }

        val wanted = (highlight.first - LEAD).coerceAtLeast(minimumValue = 0)

        val start = if (wanted == 0) {
            0
        } else {
            text.indexOf(
                char = ' ',
                startIndex = wanted
            ).let {
                if (it in wanted..highlight.first) {
                    it + 1
                } else {
                    wanted
                }
            }
        }

        val end = (start + MAX_LENGTH).coerceAtMost(maximumValue = text.length).let { limit ->
            if (limit == text.length) {
                limit
            } else {
                text.lastIndexOf(
                    char = ' ',
                    startIndex = limit
                ).takeIf {
                    it > highlight.last
                }
                    ?: limit
            }
        }

        val prefix = if (start > 0) ELLIPSIS else ""
        val suffix = if (end < text.length) ELLIPSIS else ""

        return prefix + text.substring(
            startIndex = start,
            endIndex = end
        ).trim() + suffix to
            highlight.shifted(by = prefix.length - start)
    }

    private fun IntRange.shifted(by: Int): IntRange = if (isEmpty()) this else first + by..last + by
}
