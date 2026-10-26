package app.notekea.notes.presentation.switcher

import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.RelativePath

/** What the user did in the quick switcher, plus the vault's notes as they change. */
sealed interface QuickSwitcherIntent {
    data object OnClose : QuickSwitcherIntent

    /** `Enter`: opens the selected result. */
    data object OnConfirm : QuickSwitcherIntent

    /** `Tab`: switches between searching names and content, keeping the text. */
    data object OnModeSwitch : QuickSwitcherIntent

    /** The vault's notes, sent when the palette is shown and again whenever the tree changes. */
    data class OnNotesChange(val notes: Collection<NoteEntry>) : QuickSwitcherIntent

    /** `Mod+P`, `Mod+Shift+F` or a clicked `#tag`, which opens content search with the tag already typed. */
    data class OnOpen(
        val mode: QuickSwitcherMode,
        val query: String = ""
    ) : QuickSwitcherIntent

    data class OnQueryChange(val query: String) : QuickSwitcherIntent

    data class OnRowClick(val path: RelativePath) : QuickSwitcherIntent

    data object OnSelectNext : QuickSwitcherIntent

    data object OnSelectPrevious : QuickSwitcherIntent
}
