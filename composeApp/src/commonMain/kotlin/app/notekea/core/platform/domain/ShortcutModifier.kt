package app.notekea.core.platform.domain

/** Keyboard modifiers, independent of any UI toolkit so shortcuts can be decided and tested in the domain. */
enum class ShortcutModifier {
    ALT,
    COMMAND,
    CONTROL,
    SHIFT
}
