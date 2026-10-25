package app.notekea.notes.domain

/** How a link update went: the notes rewritten, and those left alone (changed meanwhile, or locked). */
data class LinkUpdateResult(
    val updated: List<RelativePath>,
    val skipped: List<RelativePath>
) {
    val updatedCount: Int get() = updated.size
}
