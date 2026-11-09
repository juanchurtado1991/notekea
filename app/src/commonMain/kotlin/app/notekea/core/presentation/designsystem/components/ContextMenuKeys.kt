package app.notekea.core.presentation.designsystem.components

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/** Shift+F10 or the Menu key: the context menu of what has the keyboard. */
val KeyEvent.isContextMenuKey: Boolean
    get() = type == KeyEventType.KeyDown && (key == Key.Menu || (key == Key.F10 && isShiftPressed))
