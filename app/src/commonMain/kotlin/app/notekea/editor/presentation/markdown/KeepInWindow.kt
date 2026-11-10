package app.notekea.editor.presentation.markdown

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.delete
import androidx.compose.ui.text.TextRange
import kotlin.math.max
import kotlin.math.min

/**
 * Keeps what the text field does inside the [window] of the note it shows ([MarkdownOutputTransformation]). The field
 * reads the hidden text as part of the window's two edges, so a click or an arrow there would select it and a word
 * or line deletion would delete it: a selection stops at the edges, and a deletion takes only its part of the window.
 */
internal class KeepInWindow(private val window: () -> TextRange) : InputTransformation {
    @OptIn(ExperimentalFoundationApi::class)
    override fun TextFieldBuffer.transformInput() {
        val shown = window()

        if (changes.changeCount == 0) {
            selection = TextRange(
                start = shown.clamp(offset = selection.start),
                end = shown.clamp(offset = selection.end)
            )

            return
        }

        var start = Int.MAX_VALUE
        var end = 0

        for (change in 0 until changes.changeCount) {
            val range = changes.getOriginalRange(changeIndex = change)

            start = min(
                a = start,
                b = range.min
            )

            end = max(
                a = end,
                b = range.max
            )
        }

        if (start >= shown.min && end <= shown.max) return
        // Only a deletion reaches the text outside: what is typed or pasted replaces the selection, which is inside.
        revertAllChanges()
        val from = shown.clamp(offset = start)

        delete(
            start = from,
            end = shown.clamp(offset = end)
        )

        selection = TextRange(index = from)
    }

    private fun TextRange.clamp(offset: Int): Int = offset.coerceIn(
        minimumValue = min,
        maximumValue = max
    )
}
