package app.notekea.core.presentation.components

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

/**
 * Whether a [NotekeaContextMenuArea] is open and where, held in composition like a drawer state. A right click opens
 * it at the pointer; the keyboard (Shift+F10, the Menu key) opens it at the top start of its area with [open].
 */
@Stable
class NotekeaContextMenuState {
    var position: Offset? by mutableStateOf(value = null)
        private set

    fun close() {
        position = null
    }

    fun open(
        at: Offset = Offset.Zero
    ) {
        position = at
    }
}
