package app.notekea.graph.domain

import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.links.LinkResolver
import app.notekea.core.notes.domain.links.NoteRewrite
import app.notekea.core.notes.domain.links.isWikiLinkable
import app.notekea.core.notes.domain.links.markdownLink

/**
 * Links two notes when the person clicks Link: `[[target]]` at the end of [source]'s body, as short as stays clear
 * ([LinkResolver.wikiTarget]), or a Markdown link when it cannot; written only if the note did not change meanwhile.
 * Returns what was written, which [NoteRewrite.reversed] takes back, or null.
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
        val original = noteContentStore.read(
            vaultPath = vaultPath,
            path = source
        ) ?: return null

        val document = NoteDocument.fromText(text = original)
        val wikiTarget = LinkResolver(notePaths = notes).wikiTarget(path = target)

        // A name with `#`, `^`, `|` or brackets would end a wiki link early: such a note gets a Markdown link.
        val written = if (isWikiLinkable(target = wikiTarget)) {
            "[[$wikiTarget]]"
        } else {
            markdownLink(
                label = target.nameWithoutExtension,
                target = target,
                source = source
            )
        }

        val link = "$written\n"
        val kept = document.body.trimEnd()
        val body = if (kept.isEmpty()) link else "$kept\n\n$link"

        val rewrite = NoteRewrite(
            path = source,
            original = original,
            updated = document.copy(body = body).toText(),
            linkCount = 1
        )

        return rewrite.takeIf { applyLinkUpdates(vaultPath, listOf(it)).updated.isNotEmpty() }
    }
}
