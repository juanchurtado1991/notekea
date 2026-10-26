package app.notekea.core.presentation.components

import androidx.compose.ui.input.key.Key

/** What a key pressed in a [NotekeaPalette]'s field does; any other key types into the field. */
enum class PaletteKeyAction {
    MOVE_DOWN,
    MOVE_UP,
    CONFIRM,
    DISMISS,
    SWITCH;

    companion object {
        private val SUBMIT_KEYS = setOf(Key.Enter, Key.NumPadEnter)

        /** The action of [key]; Tab switches only when the palette [canSwitch], and Shift+Tab never does. */
        fun of(
            key: Key,
            isShiftPressed: Boolean,
            canSwitch: Boolean
        ): PaletteKeyAction? = when {
            key == Key.DirectionDown -> MOVE_DOWN
            key == Key.DirectionUp -> MOVE_UP
            key in SUBMIT_KEYS -> CONFIRM
            key == Key.Escape -> DISMISS
            key == Key.Tab && !isShiftPressed && canSwitch -> SWITCH
            else -> null
        }
    }
}
