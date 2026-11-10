package app.notekea.editor.presentation.sidepanel

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester
import app.notekea.editor.presentation.model.EditorIntent

/**
 * How the screen holds the side panel: the [outlineFocus] and [chatFocus] that `Mod+Shift+O` and `Mod+L` give the
 * keyboard to, and whether a narrow window floats it over the notes ([isOverlay]), open only while used
 * ([isOverlayOpen]). [connections] draws the inspector's Connections and [chat] the chat.
 */
@Stable
class SidePanelHost(
    private val send: (EditorIntent) -> Unit,
    val connections: @Composable ColumnScope.(
        isOpen: Boolean,
        onToggle: () -> Unit
    ) -> Unit = { _, _ -> },
    val chat: @Composable (inputFocus: FocusRequester) -> Unit = {}
) {
    val outlineFocus = FocusRequester()
    val chatFocus = FocusRequester()
    var isOverlay by mutableStateOf(value = false)
    var isOverlayOpen by mutableStateOf(value = false)

    /**
     * Sends a side panel request ([isRequest]) through [send], except showing or hiding the panel while it floats,
     * which only opens or closes it here: the panel the user keeps beside the notes is the tabs'.
     */
    fun onIntent(intent: EditorIntent) {
        val isOverlayToggle = intent == EditorIntent.OnSidePanelToggle && isOverlay
        if (isOverlayToggle) isOverlayOpen = !isOverlayOpen else send(intent)
    }

    /** Opens the panel if it floats, then gives its outline the keyboard once it is laid out. */
    suspend fun focusOutline() {
        if (isOverlay) isOverlayOpen = true
        withFrameNanos { }

        try {
            outlineFocus.requestFocus()
        } catch (_: IllegalStateException) {
            // The note has no headings, so there is no list to focus.
        }
    }

    /**
     * Opens the panel if it floats, then gives the chat's question field the keyboard once it is laid out, which may
     * take a few frames when the panel was on the inspector.
     */
    suspend fun focusChat() {
        if (isOverlay) isOverlayOpen = true

        repeat(times = FOCUS_FRAMES) {
            withFrameNanos { }

            try {
                chatFocus.requestFocus()

                return
            } catch (_: IllegalStateException) {
                // Not laid out yet, or no model: the chat shows its notice, with no question field.
            }
        }
    }

    companion object {
        private const val FOCUS_FRAMES = 4

        /** Whether [intent] is about the side panel, which the tabs handle rather than the note's editor. */
        fun isRequest(intent: EditorIntent): Boolean =
            intent == EditorIntent.OnSidePanelToggle ||
                intent == EditorIntent.OnOutlineFocusRequest ||
                intent is EditorIntent.OnSidePanelResize ||
                intent is EditorIntent.OnSidePanelSectionToggle ||
                intent is EditorIntent.OnSidePanelModeSelect
    }
}
