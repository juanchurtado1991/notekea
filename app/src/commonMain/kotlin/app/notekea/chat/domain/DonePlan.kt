package app.notekea.chat.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * What a plan did, to show it: the notes it [created], [changed], [moved] (from, to) and [trashed] (which only the
 * system's trash gives back), and the note to open after it ([opened]). The runner keeps how to take it back by [id].
 */
data class DonePlan(
    val id: Long,
    val created: List<RelativePath> = emptyList(),
    val changed: List<RelativePath> = emptyList(),
    val moved: List<Pair<RelativePath, RelativePath>> = emptyList(),
    val trashed: List<RelativePath> = emptyList(),
    val opened: RelativePath? = null
)
