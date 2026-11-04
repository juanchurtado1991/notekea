package app.notekea.connections.presentation

import app.notekea.connections.domain.NoteMentions
import app.notekea.core.notes.domain.RelativePath

/**
 * The Linked and Unlinked tabs of the inspector's Connections (`ROADMAP.md` 2.5): the open [note]'s [mentions], whether
 * they are being found ([isLoading]), whether the last search failed ([searchFailed], which offers to try again; what
 * it found before stays), and the note a Link click could not change ([linkFailed]), which it says so.
 */
data class NoteMentionsState(
    val note: RelativePath? = null,
    val mentions: NoteMentions = NoteMentions.NONE,
    val isLoading: Boolean = false,
    val searchFailed: Boolean = false,
    val linkFailed: RelativePath? = null
)
