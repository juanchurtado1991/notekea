package app.notekea.editor.presentation.markdown

import androidx.compose.ui.text.TextRange

/**
 * What the text field shows of the note's [window], whose text is [source]: [text], without the marks [hidden] (in
 * the note's offsets, in order) and with a dot for each of [bullets], styled by [spans], in its own offsets.
 * [toShown] and [toNote] turn an offset of one text into the other's.
 */
class ShownText(
    val source: String,
    val window: TextRange,
    val hidden: List<IntRange>,
    val bullets: List<Int>,
    styled: List<HighlightSpan>
) {
    val text: String = StringBuilder(source).also { shown ->
        bullets.forEach { shown[it - window.min] = BULLET }

        hidden.asReversed().forEach {
            shown.deleteRange(
                startIndex = it.first - window.min,
                endIndex = it.last + 1 - window.min
            )
        }
    }.toString()

    val spans: List<HighlightSpan> = styled.mapNotNull { span ->
        val start = toShown(offset = window.min + span.start)
        val end = toShown(offset = window.min + span.end)

        if (end > start) {
            span.copy(
                start = start,
                end = end
            )
        } else {
            null
        }
    }

    /** Where the note's [offset] is in the shown text: a hidden mark's characters are all where the mark was. */
    fun toShown(offset: Int): Int {
        val inWindow = offset.coerceIn(
            minimumValue = window.min,
            maximumValue = window.max
        )

        var removed = 0

        for (range in hidden) {
            if (range.first >= inWindow) break
            removed += minOf(range.last + 1, inWindow) - range.first
        }

        return inWindow - window.min - removed
    }

    /** Where the shown text's [offset] is in the note: after the marks hidden there, so before a line's text. */
    fun toNote(offset: Int): Int {
        var inNote = window.min + offset.coerceIn(
            minimumValue = 0,
            maximumValue = text.length
        )

        for (range in hidden) {
            if (range.first > inNote) break
            inNote += range.last + 1 - range.first
        }

        return inNote
    }

    companion object {
        const val BULLET = '•'
    }
}
