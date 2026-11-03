package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.NoteRewrite
import app.notekea.notes.domain.RelativePath

/**
 * A rename's link updates waiting for an answer (`ROADMAP.md` 2.5): [from] became [to], and [rewrites] are the notes
 * whose links still name [from], rewritten but not written.
 */
data class LinkUpdateOffer(
    val from: RelativePath,
    val to: RelativePath,
    val rewrites: List<NoteRewrite>
) {
    val linkCount: Int get() = rewrites.sumOf { it.linkCount }

    val noteCount: Int get() = rewrites.size
}
