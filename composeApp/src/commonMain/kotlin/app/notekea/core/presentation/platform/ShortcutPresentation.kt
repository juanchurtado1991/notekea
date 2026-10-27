package app.notekea.core.presentation.platform

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.platform.domain.ShortcutKey
import app.notekea.core.platform.domain.ShortcutModifier

/*
 * Domain shortcuts in the UI: labels written the way each operating system writes them (⌘O on macOS, Ctrl+O
 * elsewhere, modifiers in the platform's canonical order, named keys as their symbols on macOS) and matching against
 * Compose key events.
 */

private const val ALT_NAME = "Alt"
private const val BACKSLASH_NAME = "\\"
private const val BACKSPACE_NAME = "Backspace"
private const val CONTROL_NAME = "Ctrl"
private const val DELETE_NAME = "Delete"
private const val ENTER_NAME = "Enter"
private const val KEY_SEPARATOR = "+"
private const val DIGIT_PREFIX = "DIGIT_"
private const val LEFT_ARROW = "←"
private const val MAC_TAB_SYMBOL = "⇥"
private const val RIGHT_ARROW = "→"
private const val MAC_ALT_SYMBOL = "⌥"
private const val MAC_BACKSPACE_SYMBOL = "⌫"
private const val MAC_COMMAND_SYMBOL = "⌘"
private const val MAC_CONTROL_SYMBOL = "⌃"
private const val MAC_DELETE_SYMBOL = "⌦"
private const val MAC_ENTER_SYMBOL = "↩"
private const val MAC_SHIFT_SYMBOL = "⇧"
private const val SHIFT_NAME = "Shift"
private const val TAB_NAME = "Tab"
private const val WINDOWS_KEY_NAME = "Win"
private val MODIFIER_ORDER = listOf(
    ShortcutModifier.CONTROL,
    ShortcutModifier.ALT,
    ShortcutModifier.SHIFT,
    ShortcutModifier.COMMAND
)

fun Shortcut.label(
    operatingSystem: OperatingSystem
): String {
    val ordered = MODIFIER_ORDER.filter { it in modifiers }
    return when (operatingSystem) {
        OperatingSystem.MACOS -> {
            ordered.joinToString(separator = "") { it.macSymbol() } + key.macLabel()
        }

        OperatingSystem.LINUX, OperatingSystem.WINDOWS -> {
            (ordered.map { it.displayName() } + key.displayName()).joinToString(separator = KEY_SEPARATOR)
        }
    }
}

fun Shortcut.matches(
    event: KeyEvent
): Boolean = event.type == KeyEventType.KeyDown && matches(key = event.key, pressedModifiers = event.pressedModifiers())

fun Shortcut.matches(
    key: Key,
    pressedModifiers: Set<ShortcutModifier>
): Boolean = key == this.key.composeKey() && pressedModifiers == modifiers

private fun KeyEvent.pressedModifiers(): Set<ShortcutModifier> = buildSet {
    if (isAltPressed) add(element = ShortcutModifier.ALT)
    if (isMetaPressed) add(element = ShortcutModifier.COMMAND)
    if (isCtrlPressed) add(element = ShortcutModifier.CONTROL)
    if (isShiftPressed) add(element = ShortcutModifier.SHIFT)
}

private fun ShortcutKey.composeKey(): Key = when (this) {
    ShortcutKey.DIGIT_1 -> Key.One
    ShortcutKey.DIGIT_2 -> Key.Two
    ShortcutKey.DIGIT_3 -> Key.Three
    ShortcutKey.DIGIT_4 -> Key.Four
    ShortcutKey.DIGIT_5 -> Key.Five
    ShortcutKey.DIGIT_6 -> Key.Six
    ShortcutKey.DIGIT_7 -> Key.Seven
    ShortcutKey.DIGIT_8 -> Key.Eight
    ShortcutKey.DIGIT_9 -> Key.Nine
    ShortcutKey.B -> Key.B
    ShortcutKey.BACKSLASH -> Key.Backslash
    ShortcutKey.BACKSPACE -> Key.Backspace
    ShortcutKey.D -> Key.D
    ShortcutKey.DELETE -> Key.Delete
    ShortcutKey.E -> Key.E
    ShortcutKey.ENTER -> Key.Enter
    ShortcutKey.F -> Key.F
    ShortcutKey.F2 -> Key.F2
    ShortcutKey.F4 -> Key.F4
    ShortcutKey.I -> Key.I
    ShortcutKey.K -> Key.K
    ShortcutKey.LEFT -> Key.DirectionLeft
    ShortcutKey.N -> Key.N
    ShortcutKey.O -> Key.O
    ShortcutKey.P -> Key.P
    ShortcutKey.Q -> Key.Q
    ShortcutKey.RIGHT -> Key.DirectionRight
    ShortcutKey.S -> Key.S
    ShortcutKey.TAB -> Key.Tab
    ShortcutKey.V -> Key.V
    ShortcutKey.W -> Key.W
}

private fun ShortcutKey.displayName(): String = when (this) {
    ShortcutKey.BACKSLASH -> BACKSLASH_NAME

    ShortcutKey.BACKSPACE -> BACKSPACE_NAME

    ShortcutKey.DELETE -> DELETE_NAME

    ShortcutKey.ENTER -> ENTER_NAME

    ShortcutKey.LEFT -> LEFT_ARROW

    ShortcutKey.RIGHT -> RIGHT_ARROW

    ShortcutKey.TAB -> TAB_NAME

    ShortcutKey.B,
    ShortcutKey.D,
    ShortcutKey.E,
    ShortcutKey.F,
    ShortcutKey.F2,
    ShortcutKey.F4,
    ShortcutKey.I,
    ShortcutKey.K,
    ShortcutKey.N,
    ShortcutKey.O,
    ShortcutKey.P,
    ShortcutKey.Q,
    ShortcutKey.S,
    ShortcutKey.V,
    ShortcutKey.W,
    ShortcutKey.DIGIT_1,
    ShortcutKey.DIGIT_2,
    ShortcutKey.DIGIT_3,
    ShortcutKey.DIGIT_4,
    ShortcutKey.DIGIT_5,
    ShortcutKey.DIGIT_6,
    ShortcutKey.DIGIT_7,
    ShortcutKey.DIGIT_8,
    ShortcutKey.DIGIT_9 -> name.removePrefix(prefix = DIGIT_PREFIX)
}

private fun ShortcutKey.macLabel(): String = when (this) {
    ShortcutKey.BACKSPACE -> MAC_BACKSPACE_SYMBOL

    ShortcutKey.DELETE -> MAC_DELETE_SYMBOL

    ShortcutKey.ENTER -> MAC_ENTER_SYMBOL

    ShortcutKey.LEFT -> LEFT_ARROW

    ShortcutKey.RIGHT -> RIGHT_ARROW

    ShortcutKey.TAB -> MAC_TAB_SYMBOL

    ShortcutKey.DIGIT_1,
    ShortcutKey.DIGIT_2,
    ShortcutKey.DIGIT_3,
    ShortcutKey.DIGIT_4,
    ShortcutKey.DIGIT_5,
    ShortcutKey.DIGIT_6,
    ShortcutKey.DIGIT_7,
    ShortcutKey.DIGIT_8,
    ShortcutKey.DIGIT_9,
    ShortcutKey.BACKSLASH,
    ShortcutKey.D,
    ShortcutKey.B,
    ShortcutKey.E,
    ShortcutKey.F,
    ShortcutKey.F2,
    ShortcutKey.F4,
    ShortcutKey.I,
    ShortcutKey.K,
    ShortcutKey.N,
    ShortcutKey.O,
    ShortcutKey.P,
    ShortcutKey.Q,
    ShortcutKey.S,
    ShortcutKey.V,
    ShortcutKey.W -> displayName()
}

private fun ShortcutModifier.displayName(): String = when (this) {
    ShortcutModifier.ALT -> ALT_NAME
    ShortcutModifier.COMMAND -> WINDOWS_KEY_NAME
    ShortcutModifier.CONTROL -> CONTROL_NAME
    ShortcutModifier.SHIFT -> SHIFT_NAME
}

private fun ShortcutModifier.macSymbol(): String = when (this) {
    ShortcutModifier.ALT -> MAC_ALT_SYMBOL
    ShortcutModifier.COMMAND -> MAC_COMMAND_SYMBOL
    ShortcutModifier.CONTROL -> MAC_CONTROL_SYMBOL
    ShortcutModifier.SHIFT -> MAC_SHIFT_SYMBOL
}
