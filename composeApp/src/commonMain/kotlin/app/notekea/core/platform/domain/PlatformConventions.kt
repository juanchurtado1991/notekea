package app.notekea.core.platform.domain

/**
 * Everything that differs between operating systems, resolved once at startup. Menus, shortcuts and dialogs read
 * these values instead of checking the operating system themselves (`PLATFORM_GUIDELINES.md`).
 */
data class PlatformConventions(
    val operatingSystem: OperatingSystem,
    val primaryModifier: ShortcutModifier,
    val quitShortcut: Shortcut,
    val dialogButtonOrder: DialogButtonOrder,
    val appDataDirectory: String
)
