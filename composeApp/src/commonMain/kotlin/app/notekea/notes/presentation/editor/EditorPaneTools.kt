package app.notekea.notes.presentation.editor

import androidx.compose.runtime.Stable

/**
 * What one pane keeps for its editor while its notes change (`ROADMAP.md` 1.11): the [tracker] the outline follows,
 * and where each note was scrolled to ([scrollMemory]).
 */
@Stable
class EditorPaneTools {
    val tracker = OutlineTracker()
    val scrollMemory = ScrollMemory()
}
