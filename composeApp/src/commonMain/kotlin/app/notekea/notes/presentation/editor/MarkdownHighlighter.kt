package app.notekea.notes.presentation.editor

import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR

/**
 * Finds what to highlight in a note's body, line by line, fast enough to run on every keystroke: fenced code blocks,
 * headings, quote and list markers, and inline code, `[[wiki links]]`, Markdown links, bold, italic and `#tags`. The
 * Markdown syntax itself (`#`, `**`, `[[`, a link's target…) is a [HighlightKind.SYNTAX] span, shown muted. Code and
 * link targets are never searched for emphasis or tags. It only reads the text; the Read mode renders it fully.
 */
class MarkdownHighlighter {
    fun highlight(
        text: CharSequence
    ): List<HighlightSpan> {
        val spans = mutableListOf<HighlightSpan>()
        var fence: String? = null
        var lineStart = 0
        while (lineStart <= text.length) {
            val newline = text.indexOf(char = NEW_LINE_CHAR, startIndex = lineStart)
            val lineEnd = if (newline < 0) text.length else newline
            val line = text.substring(startIndex = lineStart, endIndex = lineEnd)
            val opening = MarkdownSyntax.FENCE_LINE.find(input = line)?.groupValues?.get(index = 1)
            // Spaces ending a code line keep the text's font: with another font, the cursor does not move past them.
            val codeEnd = lineStart + line.trimEnd().length
            when {
                fence != null -> {
                    spans += HighlightSpan(kind = HighlightKind.CODE_BLOCK, start = lineStart, end = codeEnd)
                    if (closesFence(line = line, fence = fence)) fence = null
                }

                opening != null -> {
                    spans += HighlightSpan(kind = HighlightKind.CODE_BLOCK, start = lineStart, end = codeEnd)
                    fence = opening
                }

                else -> highlightLine(line = line, offset = lineStart, spans = spans)
            }
            if (newline < 0) break
            lineStart = newline + 1
        }
        return spans.filter { it.end > it.start }
    }

    /** The fenced code blocks of [text], each from its opening fence to its closing one (or the end of the text). */
    fun codeBlocks(
        text: CharSequence
    ): List<IntRange> {
        val blocks = mutableListOf<IntRange>()
        var fence: String? = null
        var blockStart = 0
        var lineStart = 0
        while (lineStart <= text.length) {
            val newline = text.indexOf(char = NEW_LINE_CHAR, startIndex = lineStart)
            val lineEnd = if (newline < 0) text.length else newline
            val line = text.substring(startIndex = lineStart, endIndex = lineEnd)
            val opening = MarkdownSyntax.FENCE_LINE.find(input = line)?.groupValues?.get(index = 1)
            val open = fence
            if (open != null && closesFence(line = line, fence = open)) {
                blocks += blockStart until lineEnd
                fence = null
            } else if (open == null && opening != null) {
                fence = opening
                blockStart = lineStart
            }
            if (newline < 0) break
            lineStart = newline + 1
        }
        if (fence != null) blocks += blockStart until text.length
        return blocks
    }

    private fun closesFence(
        line: String,
        fence: String
    ): Boolean {
        val trimmed = line.trim()
        return trimmed.length >= fence.length && trimmed.all { it == fence.first() }
    }

    private fun highlightLine(
        line: String,
        offset: Int,
        spans: MutableList<HighlightSpan>
    ) {
        val heading = HEADING.find(input = line)
        val contentStart = if (heading != null) {
            val level = heading.groups[1]!!.value.length
            val textStart = heading.range.last + 1
            // The whole line takes the heading's size, so the muted marker lines up with the title.
            spans += HighlightSpan(kind = HEADING_LEVELS[level - 1], start = offset, end = offset + line.length)
            spans += syntax(start = offset, end = offset + textStart)
            textStart
        } else {
            BLOCK_MARKER.find(input = line)?.let { marker ->
                spans += syntax(start = offset, end = offset + marker.range.last + 1)
                marker.range.last + 1
            } ?: 0
        }
        InlineHighlighter(line = line, offset = offset, spans = spans).highlight(from = contentStart)
    }

    /** Inline spans of one line, in order of precedence: what code or a link target covers is not searched again. */
    private class InlineHighlighter(
        private val line: String,
        private val offset: Int,
        private val spans: MutableList<HighlightSpan>
    ) {
        private val protected = mutableListOf<IntRange>()

        fun highlight(
            from: Int
        ) {
            matches(regex = INLINE_CODE, from = from).forEach { match ->
                wrapped(match = match, kind = HighlightKind.CODE_INLINE)
                protected += match.range
            }
            matches(regex = WIKI_LINK, from = from).forEach { match ->
                wrapped(match = match, kind = HighlightKind.WIKI_LINK)
                protected += match.range
            }
            matches(regex = MARKDOWN_LINK, from = from).forEach { match -> markdownLink(match = match) }
            matches(regex = BOLD, from = from).forEach { match -> wrapped(match = match, kind = HighlightKind.BOLD) }
            matches(regex = ITALIC, from = from).forEach { match ->
                wrapped(match = match, kind = HighlightKind.ITALIC)
            }
            matches(regex = MarkdownSyntax.TAG, from = from).forEach { match ->
                spans += span(kind = HighlightKind.TAG, range = match.range)
            }
        }

        /** Matches from [from] that do not touch a protected range. */
        private fun matches(
            regex: Regex,
            from: Int
        ): List<MatchResult> = regex.findAll(input = line, startIndex = from).filter { match ->
            protected.none { it.first <= match.range.last && match.range.first <= it.last }
        }.toList()

        /** Group 1 is the opening syntax, group 2 the content and group 3 the closing syntax. */
        private fun wrapped(
            match: MatchResult,
            kind: HighlightKind
        ) {
            spans += span(kind = HighlightKind.SYNTAX, range = match.groups[1]!!.range)
            spans += span(kind = kind, range = match.groups[2]!!.range)
            spans += span(kind = HighlightKind.SYNTAX, range = match.groups[3]!!.range)
        }

        /** `[text](target)`: the text is the link, the brackets and the whole target are syntax. */
        private fun markdownLink(
            match: MatchResult
        ) {
            val text = match.groups[2]!!.range
            spans += span(kind = HighlightKind.SYNTAX, range = match.groups[1]!!.range)
            if (!text.isEmpty()) spans += span(kind = HighlightKind.LINK, range = text)
            val target = text.last + 1..match.range.last
            spans += span(kind = HighlightKind.SYNTAX, range = target)
            protected += target
        }

        private fun span(
            kind: HighlightKind,
            range: IntRange
        ): HighlightSpan = HighlightSpan(kind = kind, start = offset + range.first, end = offset + range.last + 1)
    }

    companion object {
        private val BLOCK_MARKER = Regex(pattern = "^(\\s{0,3}>\\s?|\\s*(?:[-*+]|\\d+[.)])\\s+(?:\\[[ xX]]\\s+)?)")
        private val BOLD = Regex(pattern = "(\\*\\*|__)(?=\\S)(.+?)(?<=\\S)(\\1)")
        private val HEADING = Regex(pattern = "^(#{1,6})(\\s+)")
        private val HEADING_LEVELS = listOf(
            HighlightKind.HEADING_1,
            HighlightKind.HEADING_2,
            HighlightKind.HEADING_3,
            HighlightKind.HEADING_4,
            HighlightKind.HEADING_5,
            HighlightKind.HEADING_6
        )
        private val INLINE_CODE = Regex(pattern = "(`+)(.+?)(\\1)")
        private val ITALIC =
            Regex(pattern = "((?<![*\\w])\\*(?![*\\s])|(?<![_\\w])_(?![_\\s]))(.+?)((?<=\\S)\\1(?![*\\w]))")
        private val MARKDOWN_LINK = Regex(pattern = "(!?\\[)([^\\]\\n]*)]\\([^)\\n]*\\)")
        private val WIKI_LINK = Regex(pattern = "(!?\\[\\[)([^\\]\\n]+)(]])")

        private fun syntax(
            start: Int,
            end: Int
        ): HighlightSpan = HighlightSpan(kind = HighlightKind.SYNTAX, start = start, end = end)
    }
}
