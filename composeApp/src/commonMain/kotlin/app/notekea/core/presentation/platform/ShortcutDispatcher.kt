package app.notekea.core.presentation.platform

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.input.key.KeyEvent
import app.notekea.core.platform.domain.Shortcut

/**
 * Window-level keyboard shortcuts. The window forwards every key event here (`Window(onPreviewKeyEvent)`), so a
 * shortcut works whatever has focus, including right after content changes and focus is lost; listening on a
 * screen's root depended on that root keeping focus, and shortcuts silently stopped working. Screens register their
 * shortcuts while they are shown; the most recently registered one is asked first. The menu bar runs the same actions
 * ([trigger]) and shows as disabled what no screen offers now ([isAvailable]). Used on the UI thread only.
 */
class ShortcutDispatcher {
    private val actions = mutableStateListOf<Action>()

    fun dispatch(
        event: KeyEvent
    ): Boolean = run(action = actions.lastOrNull { it.shortcut.matches(event = event) })

    /** Runs what [shortcut] does now, as if it had been pressed; false when nothing offers it. */
    fun trigger(
        shortcut: Shortcut
    ): Boolean = run(action = actions.lastOrNull { it.shortcut == shortcut })

    /** Whether a screen shown now offers [shortcut]; read in a composition, it follows the screens. */
    fun isAvailable(
        shortcut: Shortcut
    ): Boolean = actions.any { it.shortcut == shortcut }

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
