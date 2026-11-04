package app.notekea.notes.domain

import app.notekea.core.notes.domain.LinkUpdateResult
import app.notekea.core.notes.domain.RelativePath

/** Where a renamed entry now lives, and how the links to it were updated (null when nothing linked to it). */
data class RenamedEntry(
    val path: RelativePath,
    val links: LinkUpdateResult?
)
