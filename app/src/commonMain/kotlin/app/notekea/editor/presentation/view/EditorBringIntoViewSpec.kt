package app.notekea.editor.presentation.view

import androidx.compose.foundation.gestures.BringIntoViewSpec

/**
 * How the editor scrolls to show what asks to be seen (the cursor while typing, a focused field): only as far as
 * needed, and below the toolbar stuck over the top [topInset] pixels. A request as tall as the editor is the text field
 * taking the keyboard on a click, whose line is already in view, so it scrolls nothing.
 */
class EditorBringIntoViewSpec : BringIntoViewSpec {
    /** The height of the toolbar, set when it is laid out; read only while scrolling. */
    var topInset: Float = 0f

    override fun calculateScrollDistance(
        offset: Float,
        size: Float,
        containerSize: Float
    ): Float {
        val visible = containerSize - topInset
        val end = offset + size

        return when {
            size >= visible -> 0f
            offset < topInset -> offset - topInset
            end > containerSize -> end - containerSize
            else -> 0f
        }
    }
}
