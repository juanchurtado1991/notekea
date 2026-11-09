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
 * not change meanwhile. A note that already holds that very block is left as it is, so a second click adds nothing.
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
    ): QuerySave {
        val original = noteContentStore.read(vaultPath = vaultPath, path = path) ?: return QuerySave.Failed
        val document = NoteDocument.fromText(text = original)
        val block = blockOf(query = query)
        if (block.trimEnd() in document.body) return QuerySave.AlreadyThere
        val written = document.body.trimEnd()
        val gap = if (written.isEmpty()) StringPatterns.EMPTY else StringPatterns.BLANK_LINE
        val rewrite = NoteRewrite(
            path = path,
            original = original,
            updated = document.copy(body = written + gap + block).toText(),
            linkCount = 0
        )
        val isWritten = applyLinkUpdates(vaultPath, listOf(rewrite)).updated.isNotEmpty()
        return if (isWritten) QuerySave.Written(rewrite = rewrite) else QuerySave.Failed
    }

    private fun blockOf(
        query: VaultQuery
    ): String = listOf(MarkdownSyntax.QUERY_FENCE, queryYaml.write(query = query), MarkdownSyntax.FENCE)
        .filter { it.isNotEmpty() }
        .joinToString(separator = StringPatterns.NEW_LINE, postfix = StringPatterns.NEW_LINE)
}
