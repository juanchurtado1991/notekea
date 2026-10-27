package app.notekea.core.presentation.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Remembers whether the last press inside an element held `Mod` (Cmd on macOS, Ctrl elsewhere; either is taken, as
 * Ctrl+click on macOS is a right click that never reaches a link), so a click handler that only gets a link, such as
 * the Markdown renderer's, can still tell a `Mod+click` (`ROADMAP.md` 1.11: open in a new tab behind the current one).
 */
class PressedModifiers {
    var isModPressed: Boolean = false
        private set

    /** The modifier that reads every press inside the element before anything handles it; nothing is consumed. */
    fun tracking(): Modifier = Modifier.pointerInput(this) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                if (event.type == PointerEventType.Press) {
                    isModPressed = event.keyboardModifiers.isCtrlPressed || event.keyboardModifiers.isMetaPressed
                }
            }
        }
    }
}
