package app.notekea.search.presentation.switcher

import app.notekea.core.presentation.FeedIntent
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/** What the user did in the quick switcher, plus the vault's notes as they change. */
sealed interface QuickSwitcherIntent {
    data object OnClose : QuickSwitcherIntent

    /** `Enter`: opens the selected result. */
    data object OnConfirm : QuickSwitcherIntent

    /** `Tab`: switches between searching names and content, keeping the text. */
    data object OnModeSwitch : QuickSwitcherIntent

    /** The vault's notes, sent when the palette is shown and again whenever the tree changes. */
    data class OnNotesChange(val notes: Collection<NoteEntry>) :
        QuickSwitcherIntent,
        FeedIntent

    /** Whether the search index holds the vault, sent as it changes: content search says when it does not. */
    data class OnIndexChange(val isReady: Boolean) :
        QuickSwitcherIntent,
        FeedIntent

    /** `Mod+P`, `Mod+Shift+F` or a clicked `#tag`, which opens content search with the tag already typed. */
    data class OnOpen(
        val mode: QuickSwitcherMode,
        val query: String = "",
        val purpose: QuickSwitcherPurpose = QuickSwitcherPurpose.OPEN
    ) : QuickSwitcherIntent

    data class OnQueryChange(val query: String) : QuickSwitcherIntent

    data class OnRowClick(val path: RelativePath) : QuickSwitcherIntent

    data object OnSelectNext : QuickSwitcherIntent

    data object OnSelectPrevious : QuickSwitcherIntent
}
