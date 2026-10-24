package app.notekea.core.presentation.platform

import androidx.compose.ui.input.key.KeyEvent

/**
 * Window-level keyboard shortcuts. The window forwards every key event here (`Window(onPreviewKeyEvent)`), so a
 * shortcut works whatever has focus, including right after content changes and focus is lost; listening on a
 * screen's root depended on that root keeping focus, and shortcuts silently stopped working. Screens register a
 * handler while they are shown; the most recently registered handler is asked first. Used on the UI thread only.
 */
class ShortcutDispatcher {
    private val handlers = mutableListOf<(KeyEvent) -> Boolean>()

    fun dispatch(
        event: KeyEvent
    ): Boolean = handlers.asReversed().any { it(event) }

    /** Adds [handler] and returns the function that removes it. */
    fun register(
        handler: (KeyEvent) -> Boolean
    ): () -> Unit {
        handlers += handler
        return { handlers -= handler }
    }
}
