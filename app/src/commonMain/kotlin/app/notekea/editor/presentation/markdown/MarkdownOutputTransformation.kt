package app.notekea.editor.presentation.markdown

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import app.notekea.editor.presentation.find.FindState

/**
 * Shows the note's text field what [shown] says: only the window of the note (the blocks the cursor is in), without
 * the marks hidden away from the cursor, highlighted, with [findState]'s matches on top. The field lays out that text
 * alone, while the cursor, selection and undo stay the whole note's.
 */
class MarkdownOutputTransformation(
    private val styles: MarkdownStyles,
    private val shown: () -> ShownText,
    private val findState: () -> FindState? = { null }
) : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        val shown = shown()
        val window = shown.window

        // Between an edit and the next window the text may be shorter than the window: it is shown as it is.
        if (window.max > length) return

        replace(
            start = window.max,
            end = length,
            text = ""
        )

        // From the end, so the offsets of what is still to change stay the note's.
        var hidden = shown.hidden.lastIndex
        var bullet = shown.bullets.lastIndex

        while (hidden >= 0 || bullet >= 0) {
            val range = shown.hidden.getOrNull(index = hidden)
            val dot = shown.bullets.getOrNull(index = bullet)

            val isBulletNext = dot != null && (range == null || dot > range.first)

            if (dot != null && isBulletNext) {
                replace(
                    start = dot,
                    end = dot + 1,
                    text = ShownText.BULLET.toString()
                )

                bullet--
            } else if (range != null) {
                replace(
                    start = range.first,
                    end = range.last + 1,
                    text = ""
                )

                hidden--
            }
        }

        replace(
            start = 0,
            end = window.min,
            text = ""
        )

        shown.spans.forEach { span ->
            addStyle(
                spanStyle = styles.of(kind = span.kind),
                start = span.start,
                end = span.end
            )
        }

        val find = findState() ?: return

        find.matches.forEachIndexed { index, match ->
            val start = shown.toShown(offset = match.first)
            val end = shown.toShown(offset = match.last + 1)

            val isOutside = match.first < window.min || match.last >= window.max

            if (isOutside || end <= start) return@forEachIndexed

            val style = if (index == find.current) styles.currentFindMatch else styles.findMatch

            addStyle(
                spanStyle = style,
                start = start,
                end = end
            )
        }
    }
}
