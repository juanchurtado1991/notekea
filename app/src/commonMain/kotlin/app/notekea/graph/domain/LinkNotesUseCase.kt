package app.notekea.graph.domain

import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.LinkResolver
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.NoteRewrite
import app.notekea.core.notes.domain.RelativePath

/**
 * Links two notes from the graph (`ROADMAP.md` 2.7), only because the person clicked Link: `[[target]]` goes at the end
 * of [source]'s body, as short as stays unambiguous ([LinkResolver.wikiTarget]). The note is written only if it does
 * not change meanwhile. Returns what was written, which [NoteRewrite.reversed] takes back, or null if nothing was.
 */
class LinkNotesUseCase(
    private val noteContentStore: NoteContentStore,
    private val applyLinkUpdates: ApplyLinkUpdatesUseCase
) {
    suspend operator fun invoke(
        vaultPath: String,
        source: RelativePath,
        target: RelativePath,
        notes: Collection<RelativePath>
    ): NoteRewrite? {
        val original = noteContentStore.read(vaultPath = vaultPath, path = source) ?: return null
        val document = NoteDocument.fromText(text = original)
        val link = "[[${LinkResolver(notePaths = notes).wikiTarget(path = target)}]]\n"
        val written = document.body.trimEnd()
        val body = if (written.isEmpty()) link else "$written\n\n$link"
        val rewrite = NoteRewrite(
            path = source,
            original = original,
            updated = document.copy(body = body).toText(),
            linkCount = 1
        )
        return rewrite.takeIf { applyLinkUpdates(vaultPath, listOf(it)).updated.isNotEmpty() }
    }
}
