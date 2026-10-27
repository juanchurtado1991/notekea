package app.notekea.notes.presentation.editor

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester

/**
 * How the screen holds the side panel (`ROADMAP.md` 1.9), one for every pane, beyond what the user chose and the tabs
 * keep: the [outlineFocus] that `Mod+Shift+O` gives the keyboard to, and whether the window is too narrow for the panel
 * beside the notes ([isOverlay]), so it floats over them instead, open only while it is used ([isOverlayOpen]): a
 * heading chosen, Esc or the hide button close it.
 */
@Stable
class SidePanelHost(
    private val send: (EditorIntent) -> Unit
) {
    val outlineFocus = FocusRequester()
    var isOverlay by mutableStateOf(value = false)
    var isOverlayOpen by mutableStateOf(value = false)

    /**
     * Sends a side panel request ([isRequest]) through [send], except showing or hiding the panel while it floats,
     * which only opens or closes it here: the panel the user keeps beside the notes is the tabs'. A tab chosen opens the
     * floating panel too.
     */
    fun onIntent(
        intent: EditorIntent
    ) {
        when {
            intent == EditorIntent.OnSidePanelToggle && isOverlay -> isOverlayOpen = !isOverlayOpen

            intent is EditorIntent.OnSidePanelTabSelect && isOverlay -> {
                isOverlayOpen = true
                send(intent)
            }

            else -> send(intent)
        }
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

    companion object {
        /** Whether [intent] is about the side panel, which the tabs handle rather than the note's editor. */
        fun isRequest(
            intent: EditorIntent
        ): Boolean = intent == EditorIntent.OnSidePanelToggle || intent == EditorIntent.OnOutlineFocusRequest ||
            intent is EditorIntent.OnSidePanelResize || intent is EditorIntent.OnSidePanelTabSelect ||
            intent == EditorIntent.OnPropertiesPlacementToggle
    }
}
