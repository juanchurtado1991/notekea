package app.notekea.query.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.NoteRewrite
import app.notekea.core.notes.domain.RelativePath

/**
 * Saves a query at the end of a note as a ```` ```query ```` block (`ROADMAP.md` 2.10), written only if the note does
 * not change meanwhile. Returns what was written, or null if nothing was.
 */
class SaveQueryUseCase(
    private val noteContentStore: NoteContentStore,
    private val applyLinkUpdates: ApplyLinkUpdatesUseCase,
    private val queryYaml: QueryYaml
) {
    suspend operator fun invoke(
        vaultPath: String,
        path: RelativePath,
        query: VaultQuery
    ): NoteRewrite? {
        val original = noteContentStore.read(vaultPath = vaultPath, path = path) ?: return null
        val document = NoteDocument.fromText(text = original)
        val written = document.body.trimEnd()
        val gap = if (written.isEmpty()) StringPatterns.EMPTY else StringPatterns.BLANK_LINE
        val rewrite = NoteRewrite(
            path = path,
            original = original,
            updated = document.copy(body = written + gap + blockOf(query = query)).toText(),
            linkCount = 0
        )
        return rewrite.takeIf { applyLinkUpdates(vaultPath, listOf(it)).updated.isNotEmpty() }
    }

    private fun blockOf(
        query: VaultQuery
    ): String = listOf(MarkdownSyntax.QUERY_FENCE, queryYaml.write(query = query), MarkdownSyntax.FENCE)
        .filter { it.isNotEmpty() }
        .joinToString(separator = StringPatterns.NEW_LINE, postfix = StringPatterns.NEW_LINE)
}
