package app.notekea.editor.presentation.markdown

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.text.TextRange
import app.notekea.editor.presentation.find.FindState

/**
 * Shows only the [window] of the note in its text field (the blocks the cursor is in), with its Markdown highlighted
 * and [findState]'s matches on top: the field lays out that text alone, while the cursor, selection and undo stay the
 * whole note's. The last text's spans are reused when the field redraws without an edit.
 */
class MarkdownOutputTransformation(
    private val styles: MarkdownStyles,
    private val window: () -> TextRange,
    private val highlighter: MarkdownHighlighter = MarkdownHighlighter(),
    private val findState: () -> FindState? = { null }
) : OutputTransformation {
    private var lastText: String? = null
    private var lastSpans: List<HighlightSpan> = emptyList()

    override fun TextFieldBuffer.transformOutput() {
        val shown = window()
        val start = shown.min.coerceAtMost(maximumValue = length)

        val end = shown.max.coerceIn(
            minimumValue = start,
            maximumValue = length
        )

        val text = originalText.substring(
            startIndex = start,
            endIndex = end
        )

        replace(
            start = end,
            end = length,
            text = ""
        )

        replace(
            start = 0,
            end = start,
            text = ""
        )

        spansOf(text = text).forEach { span ->
            addStyle(
                spanStyle = styles.of(kind = span.kind),
                start = span.start,
                end = span.end
            )
        }

        val find = findState() ?: return

        find.matches.forEachIndexed { index, match ->
            if (match.first < start || match.last >= end) return@forEachIndexed
            val style = if (index == find.current) styles.currentFindMatch else styles.findMatch

            addStyle(
                spanStyle = style,
                start = match.first - start,
                end = match.last + 1 - start
            )
        }
    }

    private fun spansOf(text: String): List<HighlightSpan> {
        if (text != lastText) {
            lastSpans = highlighter.highlight(text = text)
            lastText = text
        }

        return lastSpans
    }
}
