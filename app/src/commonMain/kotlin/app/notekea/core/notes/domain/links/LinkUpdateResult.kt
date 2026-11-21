package app.notekea.core.notes.domain.links

import app.notekea.core.notes.domain.RelativePath

/**
 * How a link update went: the notes rewritten, those left alone (changed meanwhile, or locked), and each rewritten
 * note's text before, so an editor holding unsaved edits on it can carry the rewrite onto them.
 */
data class LinkUpdateResult(
    val updated: List<RelativePath>,
    val skipped: List<RelativePath>,
    val originals: Map<RelativePath, String> = emptyMap()
) {
    val updatedCount: Int get() = updated.size
}
