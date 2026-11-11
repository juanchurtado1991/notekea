package app.notekea.chat.presentation

import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.graphShortcut
import app.notekea.core.presentation.platform.label

/**
 * The shortcuts the guide names as `{name}` (the [PlatformConventions] property without "Shortcut"), written as this
 * system writes them: ⌘N on macOS, Ctrl+N elsewhere.
 */
internal object HelpShortcuts {
    fun of(conventions: PlatformConventions): Map<String, String> = with(conventions) {
        mapOf(
            "openFolder" to openFolderShortcut,
            "newNote" to newNoteShortcut,
            "rename" to renameShortcut,
            "delete" to deleteShortcut,
            "toggleSidebar" to toggleSidebarShortcut,
            "save" to saveShortcut,
            "find" to findShortcut,
            "toggleSource" to toggleSourceShortcut,
            "quickSwitcher" to quickSwitcherShortcut,
            "searchNotes" to searchNotesShortcut,
            "toggleTask" to toggleTaskShortcut,
            "bold" to boldShortcut,
            "italic" to italicShortcut,
            "noteLink" to noteLinkShortcut,
            "webLink" to webLinkShortcut,
            "paste" to pasteShortcut,
            "toggleSidePanel" to toggleSidePanelShortcut,
            "outline" to outlineShortcut,
            "chat" to chatShortcut,
            "today" to todayShortcut,
            "previousDay" to previousDayShortcut,
            "nextDay" to nextDayShortcut,
            "closeTab" to closeTabShortcut,
            "nextTab" to nextTabShortcut,
            "previousTab" to previousTabShortcut,
            "splitRight" to splitRightShortcut,
            "template" to templateShortcut,
            "bookmark" to bookmarkShortcut,
            "graph" to graphShortcut
        ).mapValues { (_, shortcut) -> shortcut.label(operatingSystem = operatingSystem) }
    }
}
