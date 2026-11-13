package app.notekea.core.notes.domain.index

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.FenceTracker
import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * The blocks of a section a long section is split between: its paragraphs, lists and tables, each
 * ended by a blank line, and its fenced code blocks whole, blank lines and all. A block larger than [ChunkSize.MAX] is
 * split further into pieces of at most [ChunkSize.TARGET] ([pieces]).
 */
object ChunkBlocks {
    fun of(
        body: String,
        range: IntRange
    ): List<IntRange> = blocks(
        body = body,
        range = range
    ).flatMap {
        pieces(
            body = body,
            block = it
        )
    }

    /** The blank-line separated blocks of [range], a fenced code block never split by its blank lines. */
    private fun blocks(
        body: String,
        range: IntRange
    ): List<IntRange> {
        val blocks = mutableListOf<IntRange>()
        var blockStart = -1
        val fences = FenceTracker()

        lines(
            body = body,
            range = range
        ).forEach { line ->
            val text = body.substring(range = line)
            fences.read(text = text)

            if (text.isBlank() && !fences.isOpen) {
                if (blockStart >= 0) {
                    blocks += trimmed(
                        body = body,
                        range = blockStart until line.first
                    )
                }

                blockStart = -1
            } else if (blockStart < 0) {
                blockStart = line.first
            }
        }

        if (blockStart >= 0) {
            blocks += trimmed(
                body = body,
                range = blockStart..range.last
            )
        }

        return blocks.filterNot { it.isEmpty() }
    }

    /**
     * [block] as it is, or, when it is larger than [ChunkSize.MAX], its units packed into pieces of the target size.
     */
    private fun pieces(
        body: String,
        block: IntRange
    ): List<IntRange> {
        if (ChunkSize.of(body = body, range = block) <= ChunkSize.MAX) return listOf(block)
        val text = body.substring(range = block)

        val units = if (isLineByLine(text = text)) {
            lines(
                body = body,
                range = block
            )
        } else {
            sentences(
                body = body,
                range = block
            )
        }

        return ChunkPacker.pack(
            body = body,
            ranges = units.flatMap {
                hardSplit(
                    body = body,
                    range = it
                )
            }
        )
            .map {
                trimmed(
                    body = body,
                    range = it
                )
            }
    }

    /** Code, tables and lists are split between lines; prose between sentences. */
    private fun isLineByLine(text: String): Boolean = text.lines().let { lines ->
        lines.any { MarkdownSyntax.FENCE_LINE.containsMatchIn(input = it) } ||
            lines.all { LINE_ITEM.containsMatchIn(input = it) || it.isBlank() }
    }

    /** The lines of [range], each without its line break. */
    internal fun lines(
        body: String,
        range: IntRange
    ): List<IntRange> {
        val lines = mutableListOf<IntRange>()
        var start = range.first

        while (start <= range.last) {
            val newline = body.indexOf(
                char = StringPatterns.NEW_LINE_CHAR,
                startIndex = start
            )

            val end = if (newline < 0 || newline > range.last) range.last + 1 else newline
            lines += start until end
            start = end + 1
        }

        return lines
    }

    /** The sentences of [range]: each up to its `.`, `!` or `?` and the space after it. */
    private fun sentences(
        body: String,
        range: IntRange
    ): List<IntRange> {
        val ends = SENTENCE_END.findAll(input = body.substring(range = range)).map {
            range.first + it.range.last
        }

        val starts = listOf(range.first) + ends.map {
            it + 1
        }

        return starts
            .zip(other = ends.toList() + range.last) { start, end ->
                start..end
            }
            .filterNot { it.isEmpty() }
    }

    /** [range], or when even it is larger than [ChunkSize.MAX] (a sentence without an end), cut at spaces. */
    private fun hardSplit(
        body: String,
        range: IntRange
    ): List<IntRange> {
        if (ChunkSize.of(body = body, range = range) <= ChunkSize.MAX) return listOf(range)

        val cut = hardCut(
            body = body,
            range = range
        )

        return hardSplit(
            body = body,
            range = range.first..cut
        ) + hardSplit(
            body = body,
            range = cut + 1..range.last
        )
    }

    /**
     * Where to cut [range]: the last space before the target size, or before its middle when it is shorter than twice
     * that (many short words reach the maximum in few characters), and there when it has no space.
     */
    private fun hardCut(
        body: String,
        range: IntRange
    ): Int {
        val limit = minOf(range.first + HARD_SPLIT_CHARACTERS, (range.first + range.last) / 2)

        return body.lastIndexOf(
            char = ' ',
            startIndex = limit
        ).takeIf {
            it > range.first
        } ?: limit
    }

    /** [range] without the whitespace at its ends. */
    internal fun trimmed(
        body: String,
        range: IntRange
    ): IntRange {
        var start = range.first
        var end = range.last
        fun isBlank(index: Int): Boolean = index in start..end && body[index].isWhitespace()

        while (isBlank(index = start)) start++
        while (isBlank(index = end)) end--

        return start..end
    }

    // A list item or a table row: the lines a list or a table is split between.
    private val LINE_ITEM = Regex(pattern = "^\\s*([-*+]\\s|\\d+[.)]\\s|\\|)")
    private val SENTENCE_END = Regex(pattern = "[.!?](?=\\s)")

    // Characters of a forced cut, within the target size of a chunk at the estimate's two characters a token.
    private const val HARD_SPLIT_CHARACTERS = ChunkSize.TARGET * 2
}
