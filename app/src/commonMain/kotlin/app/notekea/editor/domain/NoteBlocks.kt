package app.notekea.editor.domain

import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR

/**
 * A note's text cut into blocks at its blank lines and its headings, so the editor lays out only the blocks on screen.
 * A block runs to the start of the next one, so it holds the blank lines after it: every character of the note is in
 * one block.
 */
class NoteBlocks private constructor(
    private val starts: IntArray,
    private val textLength: Int
) {
    val size: Int get() = starts.size

    fun start(index: Int): Int = starts[index]

    /** Where the block ends, not included: the start of the next block, or the end of the text. */
    fun end(index: Int): Int = starts.getOrElse(index = index + 1) { textLength }

    /** The block holding the character at [offset]; the last one for the end of the text. */
    fun at(offset: Int): Int {
        var low = 0
        var high = starts.lastIndex

        while (low < high) {
            val middle = (low + high + 1) / 2

            if (starts[middle] <= offset) low = middle else high = middle - 1
        }

        return low
    }

    /** The blocks a selection from [selectionMin] to [selectionMax] touches: the ones the editor shows as text. */
    fun window(
        selectionMin: Int,
        selectionMax: Int
    ): IntRange = at(offset = selectionMin)..at(offset = selectionMax)

    /**
     * Where the text of the blocks of [window] ends, not included. The line break that ends the last block's blank
     * line stays out, or the text would show one empty line too many; the note's last block has none to leave out.
     */
    fun textEnd(window: IntRange): Int = end(index = window.last).let { end ->
        if (end < textLength) end - 1 else end
    }

    override fun equals(other: Any?): Boolean =
        other is NoteBlocks &&
            textLength == other.textLength &&
            starts.contentEquals(other.starts)

    override fun hashCode(): Int = starts.contentHashCode() * HASH_PRIME + textLength

    companion object {
        private const val HASH_PRIME = 31
        private const val HEADING_MARK = '#'
        private const val MAX_HEADING_LEVEL = 6
        private const val FENCE_LENGTH = 3
        private const val FENCE_MARKS = "`~"

        fun of(text: CharSequence): NoteBlocks {
            val starts = mutableListOf(0)
            var lineStart = 0
            var isInFence = false
            var isAfterBlank = false
            var hasContent = false

            while (lineStart < text.length) {
                val lineEnd = text
                    .indexOf(
                        char = NEW_LINE_CHAR,
                        startIndex = lineStart
                    ).takeIf {
                        it >= 0
                    } ?: text.length

                val textStart = (lineStart until lineEnd).firstOrNull {
                    !text[it].isWhitespace()
                }

                // An indented line after a blank one goes on with the list item or the code above it.
                val isHeading = isHeading(
                    text = text,
                    start = lineStart,
                    end = lineEnd
                )

                val opensBlock =
                    textStart == lineStart &&
                        (isAfterBlank || isHeading) &&
                        hasContent &&
                        !isInFence

                if (opensBlock) starts += lineStart

                if (textStart != null) {
                    hasContent = true

                    val isFence = isFence(
                        text = text,
                        start = textStart,
                        end = lineEnd
                    )

                    if (isFence) isInFence = !isInFence
                }

                isAfterBlank = textStart == null
                lineStart = lineEnd + 1
            }

            return NoteBlocks(
                starts = starts.toIntArray(),
                textLength = text.length
            )
        }

        /** A line of one to six `#` and a space, as the outline reads headings: it opens a block with no blank line. */
        private fun isHeading(
            text: CharSequence,
            start: Int,
            end: Int
        ): Boolean {
            val marks = (start until end).takeWhile {
                text[it] == HEADING_MARK
            }.size

            return marks in 1..MAX_HEADING_LEVEL &&
                start + marks < end &&
                text[start + marks].isWhitespace()
        }

        private fun isFence(
            text: CharSequence,
            start: Int,
            end: Int
        ): Boolean {
            val mark = text[start]
            val canBeFence = mark in FENCE_MARKS && end - start >= FENCE_LENGTH

            if (!canBeFence) return false

            return (start until start + FENCE_LENGTH).all {
                text[it] == mark
            }
        }
    }
}
