package app.notekea.core.presentation.platform

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.input.key.KeyEvent
import app.notekea.core.platform.domain.Shortcut
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Window-level keyboard shortcuts. The window forwards every key event here (`Window(onPreviewKeyEvent)`), so a
 * shortcut works whatever has focus, even right after content changes and focus is lost. Screens register their
 * shortcuts while they are shown; the most recently registered one is asked first. Used on the UI thread only.
 */
class ShortcutDispatcher(private val clock: TimeSource = TimeSource.Monotonic) {
    private val actions = mutableStateListOf<Action>()
    private var lastPressed: Shortcut? = null
    private var lastPressedAt: TimeMark? = null

    fun dispatch(event: KeyEvent): Boolean {
        val action = actions.lastOrNull { it.shortcut.matches(event = event) }

        if (action != null) {
            lastPressed = action.shortcut
            lastPressedAt = clock.markNow()
        }

        return run(action = action)
    }

    /**
     * Does what [shortcut] does now, as if it was pressed (a menu's item); false when nothing would. A menu whose key
     * the window has just handled does nothing, so one press never does it twice (macOS's menu bar sees it too).
     */
    fun trigger(shortcut: Shortcut): Boolean {
        val pressedAt = lastPressedAt

        val wasJustPressed =
            lastPressed == shortcut &&
                pressedAt != null &&
                pressedAt.elapsedNow() < SAME_PRESS

        if (wasJustPressed) return true

        return run(
            action = actions.lastOrNull {
                it.shortcut == shortcut
            }
        )
    }

    /** Adds [shortcut], which does [onTrigger], and returns the function that removes it. */
    fun register(
        shortcut: Shortcut,
        onTrigger: () -> Unit
    ): () -> Unit {
        val action = Action(
            shortcut = shortcut,
            onTrigger = onTrigger
        )

        actions += action

        return { actions -= action }
    }

    private fun run(action: Action?): Boolean {
        action?.onTrigger?.invoke()

        return action != null
    }

    private class Action(
        val shortcut: Shortcut,
        val onTrigger: () -> Unit
    )

    private companion object {
        val SAME_PRESS = 250.milliseconds
    }
}
