package app.notekea.core.platform.domain

/**
 * Everything that differs between operating systems, resolved once at startup. Menus, shortcuts and dialogs read
 * these values instead of checking the operating system themselves (`PLATFORM_GUIDELINES.md`).
 */
data class PlatformConventions(
    val operatingSystem: OperatingSystem,
    val primaryModifier: ShortcutModifier,
    val openFolderShortcut: Shortcut,
    val quitShortcut: Shortcut,
    val newNoteShortcut: Shortcut,
    val renameShortcut: Shortcut,
    val deleteShortcut: Shortcut,
    val toggleSidebarShortcut: Shortcut,
    val saveShortcut: Shortcut,
    val findShortcut: Shortcut,
    val toggleReadModeShortcut: Shortcut,
    val quickSwitcherShortcut: Shortcut,
    val searchNotesShortcut: Shortcut,
    val toggleTaskShortcut: Shortcut,
    val boldShortcut: Shortcut,
    val italicShortcut: Shortcut,
    val noteLinkShortcut: Shortcut,
    val webLinkShortcut: Shortcut,
    val pasteShortcut: Shortcut,
    val dialogButtonOrder: DialogButtonOrder,
    val appDataDirectory: String,
    val homeDirectory: String
)
