package app.notekea.core.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuBarScope
import androidx.compose.ui.window.MenuScope
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.platform.domain.ShortcutModifier
import app.notekea.core.platform.domain.graphShortcut
import app.notekea.core.platform.domain.zoomInShortcut
import app.notekea.core.platform.domain.zoomOutShortcut
import app.notekea.core.platform.domain.zoomResetShortcut
import app.notekea.resources.Res
import app.notekea.resources.about_shortcut_bookmark
import app.notekea.resources.about_shortcut_chat
import app.notekea.resources.about_shortcut_close_tab
import app.notekea.resources.about_shortcut_graph
import app.notekea.resources.about_shortcut_new_note
import app.notekea.resources.about_shortcut_next_tab
import app.notekea.resources.about_shortcut_open_note
import app.notekea.resources.about_shortcut_outline
import app.notekea.resources.about_shortcut_previous_tab
import app.notekea.resources.about_shortcut_search_notes
import app.notekea.resources.about_shortcut_side_panel
import app.notekea.resources.about_shortcut_sidebar
import app.notekea.resources.about_shortcut_split
import app.notekea.resources.about_shortcut_template
import app.notekea.resources.about_shortcut_today
import app.notekea.resources.about_shortcut_zoom_in
import app.notekea.resources.about_shortcut_zoom_out
import app.notekea.resources.about_shortcut_zoom_reset
import app.notekea.resources.about_shortcuts_title
import app.notekea.resources.menu_file
import app.notekea.resources.menu_go
import app.notekea.resources.menu_help
import app.notekea.resources.menu_note
import app.notekea.resources.menu_view
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * macOS's menu bar (redesign extras): File, Note, View, Go and Help, each item the app's own shortcut, shown beside it
 * and doing what pressing it does ([ShortcutDispatcher.trigger]); Help opens the keyboard shortcuts. The app menu's
 * About, Settings and Quit stay the system's ([SystemAppMenu]). Text editing (undo, copy, paste) is the editor's own.
 */
object MacMenuBar {
    @Composable
    fun FrameWindowScope.Menus(
        conventions: PlatformConventions,
        shortcuts: ShortcutDispatcher,
        dialogs: WindowDialogs
    ) {
        MenuBar {
            FileMenu(
                conventions = conventions,
                shortcuts = shortcuts
            )

            NoteMenu(
                conventions = conventions,
                shortcuts = shortcuts
            )

            ViewMenu(
                conventions = conventions,
                shortcuts = shortcuts
            )

            GoMenu(
                conventions = conventions,
                shortcuts = shortcuts
            )

            HelpMenu(dialogs = dialogs)
        }
    }

    @Composable
    private fun MenuBarScope.FileMenu(
        conventions: PlatformConventions,
        shortcuts: ShortcutDispatcher
    ) {
        Menu(text = stringResource(resource = Res.string.menu_file)) {
            item(
                label = Res.string.about_shortcut_new_note,
                shortcut = conventions.newNoteShortcut,
                shortcuts = shortcuts
            )

            item(
                label = Res.string.about_shortcut_today,
                shortcut = conventions.todayShortcut,
                shortcuts = shortcuts
            )

            item(
                label = Res.string.about_shortcut_open_note,
                shortcut = conventions.quickSwitcherShortcut,
                shortcuts = shortcuts
            )

            Separator()

            item(
                label = Res.string.about_shortcut_close_tab,
                shortcut = conventions.closeTabShortcut,
                shortcuts = shortcuts
            )
        }
    }

    @Composable
    private fun MenuBarScope.NoteMenu(
        conventions: PlatformConventions,
        shortcuts: ShortcutDispatcher
    ) {
        Menu(text = stringResource(resource = Res.string.menu_note)) {
            item(
                label = Res.string.about_shortcut_template,
                shortcut = conventions.templateShortcut,
                shortcuts = shortcuts
            )

            item(
                label = Res.string.about_shortcut_bookmark,
                shortcut = conventions.bookmarkShortcut,
                shortcuts = shortcuts
            )

            item(
                label = Res.string.about_shortcut_split,
                shortcut = conventions.splitRightShortcut,
                shortcuts = shortcuts
            )
        }
    }

    @Composable
    private fun MenuBarScope.ViewMenu(
        conventions: PlatformConventions,
        shortcuts: ShortcutDispatcher
    ) {
        Menu(text = stringResource(resource = Res.string.menu_view)) {
            item(
                label = Res.string.about_shortcut_sidebar,
                shortcut = conventions.toggleSidebarShortcut,
                shortcuts = shortcuts
            )

            item(
                label = Res.string.about_shortcut_side_panel,
                shortcut = conventions.toggleSidePanelShortcut,
                shortcuts = shortcuts
            )

            item(
                label = Res.string.about_shortcut_chat,
                shortcut = conventions.chatShortcut,
                shortcuts = shortcuts
            )

            item(
                label = Res.string.about_shortcut_graph,
                shortcut = conventions.graphShortcut,
                shortcuts = shortcuts
            )

            Separator()

            item(
                label = Res.string.about_shortcut_zoom_in,
                shortcut = conventions.zoomInShortcut,
                shortcuts = shortcuts
            )

            item(
                label = Res.string.about_shortcut_zoom_out,
                shortcut = conventions.zoomOutShortcut,
                shortcuts = shortcuts
            )

            item(
                label = Res.string.about_shortcut_zoom_reset,
                shortcut = conventions.zoomResetShortcut,
                shortcuts = shortcuts
            )
        }
    }

    @Composable
    private fun MenuBarScope.GoMenu(
        conventions: PlatformConventions,
        shortcuts: ShortcutDispatcher
    ) {
        Menu(text = stringResource(resource = Res.string.menu_go)) {
            item(
                label = Res.string.about_shortcut_search_notes,
                shortcut = conventions.searchNotesShortcut,
                shortcuts = shortcuts
            )

            item(
                label = Res.string.about_shortcut_outline,
                shortcut = conventions.outlineShortcut,
                shortcuts = shortcuts
            )

            Separator()

            item(
                label = Res.string.about_shortcut_next_tab,
                shortcut = conventions.nextTabShortcut,
                shortcuts = shortcuts
            )

            item(
                label = Res.string.about_shortcut_previous_tab,
                shortcut = conventions.previousTabShortcut,
                shortcuts = shortcuts
            )
        }
    }

    @Composable
    private fun MenuBarScope.HelpMenu(dialogs: WindowDialogs) {
        Menu(text = stringResource(resource = Res.string.menu_help)) {
            Item(
                text = stringResource(resource = Res.string.about_shortcuts_title),
                onClick = { dialogs.shown = WindowDialog.ABOUT }
            )
        }
    }

    @Composable
    private fun MenuScope.item(
        label: StringResource,
        shortcut: Shortcut,
        shortcuts: ShortcutDispatcher
    ) {
        Item(
            text = stringResource(resource = label),
            shortcut = keyShortcutOf(shortcut = shortcut),
            onClick = { shortcuts.trigger(shortcut = shortcut) }
        )
    }

    private fun keyShortcutOf(shortcut: Shortcut): KeyShortcut = with(shortcut.modifiers) {
        KeyShortcut(
            key = shortcut.key.composeKey(),
            ctrl = ShortcutModifier.CONTROL in this,
            meta = ShortcutModifier.COMMAND in this,
            alt = ShortcutModifier.ALT in this,
            shift = ShortcutModifier.SHIFT in this
        )
    }
}
