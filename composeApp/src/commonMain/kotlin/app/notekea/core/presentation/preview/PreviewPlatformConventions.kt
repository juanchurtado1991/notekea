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
        saveShortcut = Shortcut(key = ShortcutKey.S, modifiers = setOf(ShortcutModifier.CONTROL)),
        findShortcut = Shortcut(key = ShortcutKey.F, modifiers = setOf(ShortcutModifier.CONTROL)),
        toggleReadModeShortcut = Shortcut(key = ShortcutKey.E, modifiers = setOf(ShortcutModifier.CONTROL)),
        quickSwitcherShortcut = Shortcut(key = ShortcutKey.P, modifiers = setOf(ShortcutModifier.CONTROL)),
        searchNotesShortcut = Shortcut(
            key = ShortcutKey.F,
            modifiers = setOf(ShortcutModifier.CONTROL, ShortcutModifier.SHIFT)
        ),
        toggleTaskShortcut = Shortcut(key = ShortcutKey.ENTER, modifiers = setOf(ShortcutModifier.CONTROL)),
        boldShortcut = Shortcut(key = ShortcutKey.B, modifiers = setOf(ShortcutModifier.CONTROL)),
        italicShortcut = Shortcut(key = ShortcutKey.I, modifiers = setOf(ShortcutModifier.CONTROL)),
        noteLinkShortcut = Shortcut(key = ShortcutKey.K, modifiers = setOf(ShortcutModifier.CONTROL)),
        webLinkShortcut = Shortcut(
            key = ShortcutKey.K,
            modifiers = setOf(ShortcutModifier.CONTROL, ShortcutModifier.SHIFT)
        ),
        pasteShortcut = Shortcut(key = ShortcutKey.V, modifiers = setOf(ShortcutModifier.CONTROL)),
        toggleSidePanelShortcut = Shortcut(
            key = ShortcutKey.BACKSLASH,
            modifiers = setOf(ShortcutModifier.CONTROL, ShortcutModifier.SHIFT)
        ),
        outlineShortcut = Shortcut(
            key = ShortcutKey.O,
            modifiers = setOf(ShortcutModifier.CONTROL, ShortcutModifier.SHIFT)
        ),
        todayShortcut = Shortcut(key = ShortcutKey.D, modifiers = setOf(ShortcutModifier.CONTROL)),
        previousDayShortcut = Shortcut(
            key = ShortcutKey.LEFT,
            modifiers = setOf(ShortcutModifier.CONTROL, ShortcutModifier.ALT)
        ),
        nextDayShortcut = Shortcut(
            key = ShortcutKey.RIGHT,
            modifiers = setOf(ShortcutModifier.CONTROL, ShortcutModifier.ALT)
        ),
        closeTabShortcut = Shortcut(key = ShortcutKey.W, modifiers = setOf(ShortcutModifier.CONTROL)),
        nextTabShortcut = Shortcut(key = ShortcutKey.TAB, modifiers = setOf(ShortcutModifier.CONTROL)),
        previousTabShortcut = Shortcut(
            key = ShortcutKey.TAB,
            modifiers = setOf(ShortcutModifier.CONTROL, ShortcutModifier.SHIFT)
        ),
        splitRightShortcut = Shortcut(
            key = ShortcutKey.D,
            modifiers = setOf(ShortcutModifier.CONTROL, ShortcutModifier.SHIFT)
        ),
        goToTabShortcuts = ShortcutKey.entries.filter { it.name.startsWith(prefix = "DIGIT_") }.map {
            Shortcut(key = it, modifiers = setOf(ShortcutModifier.CONTROL))
        },
        dialogButtonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
        appDataDirectory = APP_DATA,
        homeDirectory = HOME
    )
}
