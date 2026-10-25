package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer

/**
 * Highlights the Markdown of the text field it is given to: it only adds styles, never changes the text, so the
 * cursor, selection and undo are those of the plain text. The spans of the last text are reused when the field
 * redraws without an edit.
 */
class MarkdownOutputTransformation(
    private val styles: MarkdownStyles,
    private val highlighter: MarkdownHighlighter = MarkdownHighlighter()
) : OutputTransformation {
    private var lastText: String? = null
    private var lastSpans: List<HighlightSpan> = emptyList()

    override fun TextFieldBuffer.transformOutput() {
        spansOf(text = originalText.toString()).forEach { span ->
            addStyle(spanStyle = styles.of(kind = span.kind), start = span.start, end = span.end)
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
