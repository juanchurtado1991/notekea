package app.notekea.notes.domain

/** How a link update went: how many notes were rewritten and which were left alone (changed meanwhile, or locked). */
data class LinkUpdateResult(
    val updatedCount: Int,
    val skipped: List<RelativePath>
)
