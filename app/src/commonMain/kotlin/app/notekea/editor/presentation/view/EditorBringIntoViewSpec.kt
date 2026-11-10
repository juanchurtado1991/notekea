package app.notekea.editor.presentation.view

import androidx.compose.foundation.gestures.BringIntoViewSpec

/**
 * How the editor scrolls to show what asks to be seen (the cursor while typing, a focused field): only as far as
 * needed, and the list already keeps it below its sticky toolbar. A request as tall as the editor is the text field
 * taking the keyboard on a click, whose line is already in view, so it scrolls nothing.
 */
object EditorBringIntoViewSpec : BringIntoViewSpec {
    override fun calculateScrollDistance(
        offset: Float,
        size: Float,
        containerSize: Float
    ): Float {
        val end = offset + size

        return when {
            size >= containerSize -> 0f
            offset < 0f -> offset
            end > containerSize -> end - containerSize
            else -> 0f
        }
    }
}
