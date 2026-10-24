package app.notekea.core.presentation.platform

import androidx.compose.ui.input.key.KeyEvent
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.platform.domain.ShortcutKey
import app.notekea.core.platform.domain.ShortcutModifier
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Drives the dispatcher with desktop Compose key events, the type the window forwards from `onPreviewKeyEvent`.
 * Compose's `KeyEvent(key, type, …)` factory is the only way to build one outside a window, and it is marked
 * internal; the opt-in is limited to this test.
 */
@OptIn(InternalComposeUiApi::class)
class ShortcutDispatcherTest {
    private val openFolder = Shortcut(key = ShortcutKey.O, modifiers = setOf(ShortcutModifier.CONTROL))
    private val dispatcher = ShortcutDispatcher()

    @Test
    fun `a registered shortcut handles its key press and nothing else`() {
        var triggered = 0
        dispatcher.register { event -> openFolder.matches(event = event).also { if (it) triggered++ } }

        val handled = listOf(
            dispatcher.dispatch(event = keyEvent(type = KeyEventType.KeyDown, isCtrlPressed = true)),
            dispatcher.dispatch(event = keyEvent(type = KeyEventType.KeyUp, isCtrlPressed = true)),
            dispatcher.dispatch(event = keyEvent(type = KeyEventType.KeyDown, isCtrlPressed = false))
        )

        assertEquals(
            expected = listOf(true, false, false),
            actual = handled
        )
        assertEquals(
            expected = 1,
            actual = triggered
        )
    }

    @Test
    fun `the newest handler is asked first and unregistering removes it`() {
        val calls = mutableListOf<String>()
        dispatcher.register {
            calls += FIRST
            false
        }
        val unregister = dispatcher.register {
            calls += SECOND
            true
        }

        dispatcher.dispatch(event = keyEvent(type = KeyEventType.KeyDown, isCtrlPressed = false))
        unregister()
        val handledAfterRemoval = dispatcher.dispatch(
            event = keyEvent(type = KeyEventType.KeyDown, isCtrlPressed = false)
        )

        assertEquals(
            expected = listOf(SECOND, FIRST),
            actual = calls
        )
        assertEquals(
            expected = false,
            actual = handledAfterRemoval
        )
    }

    private fun keyEvent(
        type: KeyEventType,
        isCtrlPressed: Boolean
    ): KeyEvent = KeyEvent(
        key = Key.O,
        type = type,
        isCtrlPressed = isCtrlPressed
    )

    companion object {
        private const val FIRST = "first"
        private const val SECOND = "second"
    }
}
