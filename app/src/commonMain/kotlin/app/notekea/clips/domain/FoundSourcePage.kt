package app.notekea.clips.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * A page of a site the vault has web sources of, not clipped yet, that may answer a question: its [url], what it is
 * ([about], from the site's index or the words of its address) and the [section] of notes it would join.
 */
data class FoundSourcePage(
    val url: String,
    val about: String,
    val section: RelativePath
)
