package app.notekea.notes.presentation.editor

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester

/**
 * How the editor's screen holds the side panel (`ROADMAP.md` 1.9), beyond what the user chose and the ViewModel keeps:
 * the [tracker] it shares with the outline, the [outlineFocus] that `Mod+Shift+O` gives the keyboard to, and whether
 * the window is too narrow for the panel beside the note ([isOverlay]), so it floats over it instead, open only while
 * it is used ([isOverlayOpen]): a heading chosen, Esc or the hide button close it.
 */
@Stable
internal class SidePanelHost(
    private val send: (EditorIntent) -> Unit
) {
    val tracker = OutlineTracker()
    val outlineFocus = FocusRequester()
    var isOverlay by mutableStateOf(value = false)
    var isOverlayOpen by mutableStateOf(value = false)

    /**
     * Sends [intent] to the ViewModel through [send], except showing or hiding the panel while it floats, which only
     * opens or closes it here: the panel the user keeps beside the note is the ViewModel's.
     */
    fun onIntent(
        intent: EditorIntent
    ) {
        if (intent == EditorIntent.OnSidePanelToggle && isOverlay) isOverlayOpen = !isOverlayOpen else send(intent)
    }
}
