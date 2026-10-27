package app.notekea.core.presentation.about

import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.resources.Res
import app.notekea.resources.about_group_notes
import app.notekea.resources.about_group_workspace
import app.notekea.resources.about_group_writing
import app.notekea.resources.about_shortcut_bold
import app.notekea.resources.about_shortcut_bookmark
import app.notekea.resources.about_shortcut_close_tab
import app.notekea.resources.about_shortcut_delete
import app.notekea.resources.about_shortcut_find
import app.notekea.resources.about_shortcut_italic
import app.notekea.resources.about_shortcut_new_note
import app.notekea.resources.about_shortcut_next_day
import app.notekea.resources.about_shortcut_next_tab
import app.notekea.resources.about_shortcut_note_link
import app.notekea.resources.about_shortcut_open_note
import app.notekea.resources.about_shortcut_outline
import app.notekea.resources.about_shortcut_previous_day
import app.notekea.resources.about_shortcut_previous_tab
import app.notekea.resources.about_shortcut_read_mode
import app.notekea.resources.about_shortcut_rename
import app.notekea.resources.about_shortcut_save
import app.notekea.resources.about_shortcut_search_notes
import app.notekea.resources.about_shortcut_side_panel
import app.notekea.resources.about_shortcut_sidebar
import app.notekea.resources.about_shortcut_split
import app.notekea.resources.about_shortcut_task
import app.notekea.resources.about_shortcut_template
import app.notekea.resources.about_shortcut_today
import app.notekea.resources.about_shortcut_web_link

/** About's shortcuts in three groups, each ordered from the most used: notes, writing, and the workspace. */
object AboutShortcuts {
    fun groups(
        conventions: PlatformConventions
    ): List<AboutShortcutGroup> = with(conventions) {
        listOf(
            AboutShortcutGroup(
                title = Res.string.about_group_notes,
                shortcuts = listOf(
                    AboutShortcut(label = Res.string.about_shortcut_open_note, shortcut = quickSwitcherShortcut),
                    AboutShortcut(label = Res.string.about_shortcut_new_note, shortcut = newNoteShortcut),
                    AboutShortcut(label = Res.string.about_shortcut_search_notes, shortcut = searchNotesShortcut),
                    AboutShortcut(label = Res.string.about_shortcut_today, shortcut = todayShortcut),
                    AboutShortcut(label = Res.string.about_shortcut_previous_day, shortcut = previousDayShortcut),
                    AboutShortcut(label = Res.string.about_shortcut_next_day, shortcut = nextDayShortcut),
                    AboutShortcut(label = Res.string.about_shortcut_template, shortcut = templateShortcut),
                    AboutShortcut(label = Res.string.about_shortcut_bookmark, shortcut = bookmarkShortcut),
                    AboutShortcut(label = Res.string.about_shortcut_rename, shortcut = renameShortcut),
                    AboutShortcut(label = Res.string.about_shortcut_delete, shortcut = deleteShortcut)
                )
            ),
            writing(conventions = conventions),
            workspace(conventions = conventions)
        )
    }

    private fun writing(
        conventions: PlatformConventions
    ) = with(conventions) {
        AboutShortcutGroup(
            title = Res.string.about_group_writing,
            shortcuts = listOf(
                AboutShortcut(label = Res.string.about_shortcut_read_mode, shortcut = toggleReadModeShortcut),
                AboutShortcut(label = Res.string.about_shortcut_find, shortcut = findShortcut),
                AboutShortcut(label = Res.string.about_shortcut_bold, shortcut = boldShortcut),
                AboutShortcut(label = Res.string.about_shortcut_italic, shortcut = italicShortcut),
                AboutShortcut(label = Res.string.about_shortcut_note_link, shortcut = noteLinkShortcut),
                AboutShortcut(label = Res.string.about_shortcut_web_link, shortcut = webLinkShortcut),
                AboutShortcut(label = Res.string.about_shortcut_task, shortcut = toggleTaskShortcut),
                AboutShortcut(label = Res.string.about_shortcut_save, shortcut = saveShortcut)
            )
        )
    }

    private fun workspace(
        conventions: PlatformConventions
    ) = with(conventions) {
        AboutShortcutGroup(
            title = Res.string.about_group_workspace,
            shortcuts = listOf(
                AboutShortcut(label = Res.string.about_shortcut_sidebar, shortcut = toggleSidebarShortcut),
                AboutShortcut(label = Res.string.about_shortcut_side_panel, shortcut = toggleSidePanelShortcut),
                AboutShortcut(label = Res.string.about_shortcut_outline, shortcut = outlineShortcut),
                AboutShortcut(label = Res.string.about_shortcut_close_tab, shortcut = closeTabShortcut),
                AboutShortcut(label = Res.string.about_shortcut_next_tab, shortcut = nextTabShortcut),
                AboutShortcut(label = Res.string.about_shortcut_previous_tab, shortcut = previousTabShortcut),
                AboutShortcut(label = Res.string.about_shortcut_split, shortcut = splitRightShortcut)
            )
        )
    }
}
