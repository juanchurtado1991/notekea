package app.notekea.core.platform.domain

/** A key combination; the presentation layer maps it to the UI toolkit's shortcut type. */
data class Shortcut(
    val key: ShortcutKey,
    val modifiers: Set<ShortcutModifier>
)
