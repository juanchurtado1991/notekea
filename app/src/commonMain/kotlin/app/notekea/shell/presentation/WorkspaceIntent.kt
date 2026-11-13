package app.notekea.shell.presentation

import androidx.compose.ui.unit.IntSize
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.VaultTree
import app.notekea.core.notes.domain.sidepanel.ConnectionsTab
import app.notekea.core.notes.domain.sidepanel.SidePanelMode
import app.notekea.core.notes.domain.sidepanel.SidePanelSection
import app.notekea.core.presentation.FeedIntent

/** What the user did with the tabs and panes, or what the panes around them report. */
sealed interface WorkspaceIntent {
    /** The side panel: shown or not, its width, its mode, its sections and where the keyboard goes in it. */
    sealed interface SidePanel : WorkspaceIntent

    /** The vault's files changed; [isLoaded] once the first scan is done, when the last run's tabs can be restored. */
    data class OnTreeChange(
        val tree: VaultTree,
        val isLoaded: Boolean
    ) : WorkspaceIntent,
        FeedIntent

    /**
     * [path] was opened (a click, a link, the quick switcher…): its tab is shown, or added behind when [inBackground].
     */
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

    /** A related note's Open to the side: [path] opens in the other pane, created if needed. */
    data class OnOpenBeside(val path: RelativePath) : WorkspaceIntent

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

    /**
     * A folder grid's card for [path] (a note or a picture) is on screen and needs its picture, at most [size] pixels:
     * the largest a card is drawn.
     */
    data class OnCoverRequest(
        val path: RelativePath,
        val size: IntSize
    ) : WorkspaceIntent,
        FeedIntent

    /** `Mod+Shift+\`, the toolbar's Outline or the panel's hide button. */
    data object OnSidePanelToggle : SidePanel

    data class OnSidePanelResize(val widthDp: Int) : SidePanel

    data class OnSidePanelModeSelect(val mode: SidePanelMode) : SidePanel

    data class OnSidePanelSectionToggle(val section: SidePanelSection) : SidePanel

    /** Connections shows [tab]: Related, Linked or Unlinked. */
    data class OnConnectionsTabSelect(val tab: ConnectionsTab) : SidePanel

    /** `Mod+Shift+O`: the outline takes the keyboard, the panel shown on the inspector and the outline opened first. */
    data object OnOutlineFocusRequest : SidePanel

    /** `Mod+L`: the chat's question field takes the keyboard, the panel shown on the chat. */
    data object OnChatFocusRequest : SidePanel
}
