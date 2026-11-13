package app.notekea.core.presentation.designsystem.components.menus

/**
 * How a menu row's submenu is driven: whether it [isOpen] and, open, [hasKeyboard]; [onHover] runs as the pointer comes
 * to the row (or clicks it), [onEnter] when the keyboard goes in and [onLeave] when it comes back.
 */
internal class SubmenuControl(
    val isOpen: Boolean,
    val hasKeyboard: Boolean,
    val onHover: () -> Unit,
    val onEnter: () -> Unit,
    val onLeave: () -> Unit
) {
    companion object {
        /** A row of a submenu, which opens none of its own. */
        val NONE = SubmenuControl(
            isOpen = false,
            hasKeyboard = false,
            onHover = {},
            onEnter = {},
            onLeave = {}
        )
    }
}
