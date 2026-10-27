package app.notekea.notes.presentation.workspace

import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SidePanelTab
import app.notekea.notes.domain.VaultTree

/** What the user did with the tabs and panes, or what the panes around them report. */
sealed interface WorkspaceIntent {
    /** The vault's files changed; [isLoaded] once the first scan is done, when the last run's tabs can be restored. */
    data class OnTreeChange(
        val tree: VaultTree,
        val isLoaded: Boolean
    ) : WorkspaceIntent

    /** [path] was opened (a click, a link, the quick switcher…): its tab is shown, or added behind when [inBackground]. */
    data class OnOpen(
        val path: RelativePath,
        val inBackground: Boolean = false
    ) : WorkspaceIntent

    data class OnTabSelect(
        val pane: Int,
        val tab: Int
    ) : WorkspaceIntent

    data class OnTabClose(
        val pane: Int,
        val tab: Int
    ) : WorkspaceIntent

    data class OnCloseOthers(
        val pane: Int,
        val tab: Int
    ) : WorkspaceIntent

    /** `Mod+W`: the active tab closes. */
    data object OnCloseActive : WorkspaceIntent

    /** `Ctrl+Tab` ([step] 1) or `Ctrl+Shift+Tab` ([step] -1). */
    data class OnCycle(val step: Int) : WorkspaceIntent

    /** `Mod+1` to `Mod+9`. */
    data class OnJump(val number: Int) : WorkspaceIntent

    /** `Mod+Shift+D` or the tab menu's Split right: the active tab moves to a new pane on the right. */
    data object OnSplitRight : WorkspaceIntent

    /** A tab was dragged and dropped in [toPane] at [toIndex], or moved there from its menu. */
    data class OnTabMove(
        val path: RelativePath,
        val toPane: Int,
        val toIndex: Int
    ) : WorkspaceIntent

    /** A click in [pane]: the keyboard works there now. */
    data class OnPaneFocus(val pane: Int) : WorkspaceIntent

    data class OnRenamed(
        val from: RelativePath,
        val to: RelativePath
    ) : WorkspaceIntent

    data class OnDeleted(val path: RelativePath) : WorkspaceIntent

    /** A folder grid's card for [path] (a note or a picture) is on screen and needs its picture. */
    data class OnCoverRequest(val path: RelativePath) : WorkspaceIntent

    /** `Mod+Shift+\`, the toolbar's Outline or the panel's hide button. */
    data object OnSidePanelToggle : WorkspaceIntent

    data class OnSidePanelResize(val widthDp: Int) : WorkspaceIntent

    /** One of the side panel's tabs was chosen, or a property was added while they are only there: it is shown. */
    data class OnSidePanelTabSelect(val tab: SidePanelTab) : WorkspaceIntent

    /**
     * The properties move between the note and the side panel (`ROADMAP.md` 1.14): moved to the panel, it shows its
     * Properties tab.
     */
    data object OnPropertiesInNoteToggle : WorkspaceIntent

    /** `Mod+Shift+O`: the outline takes the keyboard, the panel shown first if it was hidden. */
    data object OnOutlineFocusRequest : WorkspaceIntent
}
