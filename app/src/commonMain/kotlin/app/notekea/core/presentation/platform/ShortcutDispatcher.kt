package app.notekea.core.presentation.platform

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.input.key.KeyEvent
import app.notekea.core.platform.domain.Shortcut

/**
 * Window-level keyboard shortcuts. The window forwards every key event here (`Window(onPreviewKeyEvent)`), so a
 * shortcut works whatever has focus, including right after content changes and focus is lost; listening on a
 * screen's root depended on that root keeping focus, and shortcuts silently stopped working. Screens register their
 * shortcuts while they are shown; the most recently registered one is asked first. Used on the UI thread only.
 */
class ShortcutDispatcher {
    private val actions = mutableStateListOf<Action>()

    fun dispatch(
        event: KeyEvent
    ): Boolean = run(action = actions.lastOrNull { it.shortcut.matches(event = event) })

    /** Adds [shortcut], which does [onTrigger], and returns the function that removes it. */
    fun register(
        shortcut: Shortcut,
        onTrigger: () -> Unit
    ): () -> Unit {
        val action = Action(shortcut = shortcut, onTrigger = onTrigger)
        actions += action
        return { actions -= action }
    }

    private fun run(
        action: Action?
    ): Boolean {
        action?.onTrigger?.invoke()
        return action != null
    }

    private class Action(
        val shortcut: Shortcut,
        val onTrigger: () -> Unit
    )
}
