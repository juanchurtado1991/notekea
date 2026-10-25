package app.notekea.notes.domain

/** Where a renamed entry now lives, and how the links to it were updated (null when nothing linked to it). */
data class RenamedEntry(
    val path: RelativePath,
    val links: LinkUpdateResult?
)
