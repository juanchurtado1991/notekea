package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer

/**
 * Highlights the Markdown of the text field it is given to: it only adds styles, never changes the text, so the
 * cursor, selection and undo are those of the plain text. The spans of the last text are reused when the field
 * redraws without an edit. While Find in note is open, [findState] adds its matches on top; it is read inside the
 * transformation, so the field restyles as soon as the matches or the current one change.
 */
class MarkdownOutputTransformation(
    private val styles: MarkdownStyles,
    private val highlighter: MarkdownHighlighter = MarkdownHighlighter(),
    private val findState: () -> FindState? = { null }
) : OutputTransformation {
    private var lastText: String? = null
    private var lastSpans: List<HighlightSpan> = emptyList()

    override fun TextFieldBuffer.transformOutput() {
        spansOf(text = originalText.toString()).forEach { span ->
            addStyle(spanStyle = styles.of(kind = span.kind), start = span.start, end = span.end)
        }
        val find = findState() ?: return
        find.matches.forEachIndexed { index, match ->
            if (match.last >= length) return@forEachIndexed
            val style = if (index == find.current) styles.currentFindMatch else styles.findMatch
            addStyle(spanStyle = style, start = match.first, end = match.last + 1)
        }
    }

    private fun spansOf(
        text: String
    ): List<HighlightSpan> {
        if (text != lastText) {
            lastSpans = highlighter.highlight(text = text)
            lastText = text
        }
        return lastSpans
    }
}
