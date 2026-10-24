package app.notekea.core.presentation.preview

import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.platform.domain.ShortcutKey
import app.notekea.core.platform.domain.ShortcutModifier

/** Linux conventions for previews and presentation tests, so every feature shows the same shortcuts and paths. */
object PreviewPlatformConventions {
    private const val APP_DATA = "/home/ana/.local/share/notekea"
    private const val HOME = "/home/ana"

    val linux = PlatformConventions(
        operatingSystem = OperatingSystem.LINUX,
        primaryModifier = ShortcutModifier.CONTROL,
        openFolderShortcut = Shortcut(key = ShortcutKey.O, modifiers = setOf(ShortcutModifier.CONTROL)),
        quitShortcut = Shortcut(key = ShortcutKey.Q, modifiers = setOf(ShortcutModifier.CONTROL)),
        newNoteShortcut = Shortcut(key = ShortcutKey.N, modifiers = setOf(ShortcutModifier.CONTROL)),
        renameShortcut = Shortcut(key = ShortcutKey.F2, modifiers = emptySet()),
        deleteShortcut = Shortcut(key = ShortcutKey.DELETE, modifiers = emptySet()),
        toggleSidebarShortcut = Shortcut(key = ShortcutKey.BACKSLASH, modifiers = setOf(ShortcutModifier.CONTROL)),
        dialogButtonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
        appDataDirectory = APP_DATA,
        homeDirectory = HOME
    )
}
