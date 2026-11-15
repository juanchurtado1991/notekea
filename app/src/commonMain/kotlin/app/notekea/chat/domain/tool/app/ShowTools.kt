package app.notekea.chat.domain.tool.app

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind

/** What the app shows at once, one tool a kind of step (`docs/agent-loop.md`, step 6): the *app* toolset's ends. */
object ShowTools {
    /** The tools, [setTheme] changing the theme and saying whether it knows the one named. */
    fun of(setTheme: suspend (String) -> Boolean): List<ShowTool> = listOf(
        ShowTool(
            name = "open_note",
            description = "opens a note: its name, and a heading in it to go to when the request names one.",
            kind = ActionKind.OPEN_NOTE,
            fields = listOf(ActionField.NOTE, ActionField.TEXT),
            optional = setOf(ActionField.TEXT)
        ),
        ShowTool(
            name = "open_today",
            description = "opens today's daily note.",
            kind = ActionKind.OPEN_TODAY
        ),
        ShowTool(
            name = "show_search",
            description = "opens the app's search on these words, for the person to look through.",
            kind = ActionKind.SEARCH,
            fields = listOf(ActionField.TEXT)
        ),
        ShowTool(
            name = "show_panel",
            description = "shows a panel: files, outline, related, backlinks, chat, graph, tasks, query or " +
                "bookmarks.",
            kind = ActionKind.SHOW_PANEL,
            fields = listOf(ActionField.PANEL)
        ),
        ShowTool(
            name = "split_pane",
            description = "splits the editor in two, side by side.",
            kind = ActionKind.SPLIT_PANE
        ),
        ShowTool(
            name = "close_other_tabs",
            description = "closes every tab but the one in front.",
            kind = ActionKind.CLOSE_OTHER_TABS
        ),
        ShowTool(
            name = "toggle_source",
            description = "shows the note as written in Markdown, or as it reads again.",
            kind = ActionKind.TOGGLE_SOURCE
        ),
        ShowTool(
            name = "set_theme",
            description = "changes the app's theme: its name (light, dark, or one the person installed).",
            kind = ActionKind.SET_THEME,
            fields = listOf(ActionField.THEME),
            setTheme = setTheme
        )
    )
}
