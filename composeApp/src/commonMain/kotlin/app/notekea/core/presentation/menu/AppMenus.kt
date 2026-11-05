package app.notekea.core.presentation.menu

import app.notekea.core.platform.domain.graphShortcut
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.zoomInShortcut
import app.notekea.core.platform.domain.zoomOutShortcut
import app.notekea.core.platform.domain.zoomResetShortcut
import app.notekea.core.presentation.menu.AppMenuEntry.Command
import app.notekea.core.presentation.menu.AppMenuEntry.OpensDialog
import app.notekea.core.presentation.menu.AppMenuEntry.Separator
import app.notekea.core.presentation.platform.WindowDialog
import app.notekea.resources.Res
import app.notekea.resources.menu_about
import app.notekea.resources.menu_bookmark
import app.notekea.resources.menu_close_tab
import app.notekea.resources.menu_close_window
import app.notekea.resources.menu_edit
import app.notekea.resources.menu_exit
import app.notekea.resources.menu_file
import app.notekea.resources.menu_find
import app.notekea.resources.menu_graph
import app.notekea.resources.menu_help
import app.notekea.resources.menu_new_note
import app.notekea.resources.menu_next_tab
import app.notekea.resources.menu_open_folder
import app.notekea.resources.menu_open_note
import app.notekea.resources.menu_outline
import app.notekea.resources.menu_previous_tab
import app.notekea.resources.menu_read_mode
import app.notekea.resources.menu_save
import app.notekea.resources.menu_search_notes
import app.notekea.resources.menu_settings
import app.notekea.resources.menu_shortcuts
import app.notekea.resources.menu_side_panel
import app.notekea.resources.menu_sidebar
import app.notekea.resources.menu_split_right
import app.notekea.resources.menu_template
import app.notekea.resources.menu_today
import app.notekea.resources.menu_toggle_task
import app.notekea.resources.menu_view
import app.notekea.resources.menu_window
import app.notekea.resources.menu_zoom_in
import app.notekea.resources.menu_zoom_out
import app.notekea.resources.menu_zoom_reset

/**
 * The window's menu bar (`PLATFORM_GUIDELINES.md` §4), so every command can be found without knowing its shortcut:
 * each runs what its shortcut runs. macOS's own app menu holds About, Settings and Quit, so File and Help leave them
 * out there, and Help opens About for its list of shortcuts instead.
 */
object AppMenus {
    fun of(
        conventions: PlatformConventions
    ): List<AppMenu> = listOf(
        AppMenu(title = Res.string.menu_file, entries = file(conventions = conventions)),
        AppMenu(title = Res.string.menu_edit, entries = edit(conventions = conventions)),
        AppMenu(title = Res.string.menu_view, entries = view(conventions = conventions)),
        AppMenu(title = Res.string.menu_window, entries = window(conventions = conventions)),
        AppMenu(title = Res.string.menu_help, entries = help(conventions = conventions))
    )

    private fun hasAppMenu(
        conventions: PlatformConventions
    ): Boolean = conventions.operatingSystem == OperatingSystem.MACOS

    private fun file(
        conventions: PlatformConventions
    ): List<AppMenuEntry> = with(conventions) {
        val leaving = listOf(
            Separator,
            OpensDialog(label = Res.string.menu_settings, dialog = WindowDialog.SETTINGS),
            Separator,
            Command(label = Res.string.menu_exit, shortcut = quitShortcut)
        )
        listOf(
            Command(label = Res.string.menu_new_note, shortcut = newNoteShortcut),
            Command(label = Res.string.menu_today, shortcut = todayShortcut),
            Command(label = Res.string.menu_open_note, shortcut = quickSwitcherShortcut),
            Command(label = Res.string.menu_open_folder, shortcut = openFolderShortcut),
            Separator,
            Command(label = Res.string.menu_save, shortcut = saveShortcut),
            Command(label = Res.string.menu_close_tab, shortcut = closeTabShortcut),
            Command(label = Res.string.menu_close_window, shortcut = closeWindowShortcut)
        ) + if (hasAppMenu(conventions = conventions)) emptyList() else leaving
    }

    private fun edit(
        conventions: PlatformConventions
    ): List<AppMenuEntry> = with(conventions) {
        listOf(
            Command(label = Res.string.menu_find, shortcut = findShortcut),
            Command(label = Res.string.menu_search_notes, shortcut = searchNotesShortcut),
            Separator,
            Command(label = Res.string.menu_template, shortcut = templateShortcut),
            Command(label = Res.string.menu_toggle_task, shortcut = toggleTaskShortcut),
            Command(label = Res.string.menu_bookmark, shortcut = bookmarkShortcut)
        )
    }

    private fun view(
        conventions: PlatformConventions
    ): List<AppMenuEntry> = with(conventions) {
        listOf(
            Command(label = Res.string.menu_sidebar, shortcut = toggleSidebarShortcut),
            Command(label = Res.string.menu_side_panel, shortcut = toggleSidePanelShortcut),
            Command(label = Res.string.menu_outline, shortcut = outlineShortcut),
            Command(label = Res.string.menu_graph, shortcut = graphShortcut),
            Command(label = Res.string.menu_read_mode, shortcut = toggleReadModeShortcut),
            Separator,
            Command(label = Res.string.menu_zoom_in, shortcut = zoomInShortcut),
            Command(label = Res.string.menu_zoom_out, shortcut = zoomOutShortcut),
            Command(label = Res.string.menu_zoom_reset, shortcut = zoomResetShortcut)
        )
    }

    private fun window(
        conventions: PlatformConventions
    ): List<AppMenuEntry> = with(conventions) {
        listOf(
            Command(label = Res.string.menu_next_tab, shortcut = nextTabShortcut),
            Command(label = Res.string.menu_previous_tab, shortcut = previousTabShortcut),
            Command(label = Res.string.menu_split_right, shortcut = splitRightShortcut)
        )
    }

    private fun help(
        conventions: PlatformConventions
    ): List<AppMenuEntry> = listOf(
        OpensDialog(
            label = if (hasAppMenu(conventions = conventions)) Res.string.menu_shortcuts else Res.string.menu_about,
            dialog = WindowDialog.ABOUT
        )
    )
}
