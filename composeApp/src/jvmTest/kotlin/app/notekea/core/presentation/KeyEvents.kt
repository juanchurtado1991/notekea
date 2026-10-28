package app.notekea.core.presentation

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType

/** A key pressed without Ctrl, as the window sends it. */
@OptIn(InternalComposeUiApi::class)
fun keyDown(
    key: Key,
    isShiftPressed: Boolean = false
): KeyEvent = KeyEvent(key = key, type = KeyEventType.KeyDown, isShiftPressed = isShiftPressed)

/** A key pressed with Ctrl, and Shift when [isShiftPressed], as the window sends it. */
@OptIn(InternalComposeUiApi::class)
fun ctrlKeyDown(
    key: Key,
    isShiftPressed: Boolean = false
): KeyEvent = KeyEvent(
    key = key,
    type = KeyEventType.KeyDown,
    isCtrlPressed = true,
    isShiftPressed = isShiftPressed
)
