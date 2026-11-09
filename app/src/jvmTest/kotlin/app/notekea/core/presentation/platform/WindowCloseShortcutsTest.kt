package app.notekea.core.presentation.platform

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import kotlin.test.Test
import kotlin.test.assertEquals

/** `Ctrl+Q` on Linux and `Mod+Shift+W` close the window by the path that runs its close guard (audit 46). */
@OptIn(InternalComposeUiApi::class)
class WindowCloseShortcutsTest {
    @Test
    fun `the quit shortcut and Close window ask to close, and Close tab does not`() {
        val dispatcher = ShortcutDispatcher()
        var closes = 0

        UiThread.run {
            ImageComposeScene(
                width = SIZE,
                height = SIZE
            ) {
                CompositionLocalProvider(LocalShortcutDispatcher provides dispatcher) {
                    WindowCloseShortcuts(
                        platformConventions = PreviewPlatformConventions.linux,
                        onClose = {
                            closes++
                        }
                    )
                }
            }.use { scene ->
                scene.render()
                dispatcher.dispatch(event = key(key = Key.Q))

                dispatcher.dispatch(
                    event = key(
                        key = Key.W,
                        isShiftPressed = true
                    )
                )

                dispatcher.dispatch(event = key(key = Key.W))
            }
        }

        assertEquals(
            expected = 2,
            actual = closes
        )
    }

    private fun key(
        key: Key,
        isShiftPressed: Boolean = false
    ): KeyEvent = KeyEvent(
        key = key,
        type = KeyEventType.KeyDown,
        isCtrlPressed = true,
        isShiftPressed = isShiftPressed
    )

    private companion object {
        const val SIZE = 100
    }
}
