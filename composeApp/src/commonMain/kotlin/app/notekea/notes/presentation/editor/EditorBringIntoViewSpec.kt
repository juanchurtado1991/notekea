package app.notekea.notes.presentation.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec

/**
 * How the editor scrolls to show what asks to be seen (the cursor while typing, a focused field). It scrolls only as
 * far as needed, and keeps what it shows below the toolbar, which stays over the top [topInset] pixels of the editor
 * while it is stuck there. A request as tall as the editor is the text field taking the keyboard on a click: the
 * clicked line is already in view, so it scrolls nothing instead of aligning the field's top with the editor's.
 */
@OptIn(ExperimentalFoundationApi::class)
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
