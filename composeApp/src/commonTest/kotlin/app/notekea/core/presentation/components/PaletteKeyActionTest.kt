package app.notekea.core.presentation.components

import androidx.compose.ui.input.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals

class PaletteKeyActionTest {
    @Test
    fun `arrows move, enter confirms, esc closes, and other keys type`() {
        assertEquals(
            expected = listOf(
                PaletteKeyAction.MOVE_DOWN,
                PaletteKeyAction.MOVE_UP,
                PaletteKeyAction.CONFIRM,
                PaletteKeyAction.CONFIRM,
                PaletteKeyAction.DISMISS,
                null
            ),
            actual = listOf(Key.DirectionDown, Key.DirectionUp, Key.Enter, Key.NumPadEnter, Key.Escape, Key.A).map {
                PaletteKeyAction.of(key = it, isShiftPressed = false, canSwitch = true)
            }
        )
    }

    @Test
    fun `tab switches only when the palette can switch, and never with shift`() {
        assertEquals(
            expected = listOf(PaletteKeyAction.SWITCH, null, null),
            actual = listOf(false to true, true to true, false to false).map { (isShiftPressed, canSwitch) ->
                PaletteKeyAction.of(key = Key.Tab, isShiftPressed = isShiftPressed, canSwitch = canSwitch)
            }
        )
    }
}
