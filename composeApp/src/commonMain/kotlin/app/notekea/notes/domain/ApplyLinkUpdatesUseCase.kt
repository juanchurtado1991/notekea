package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * Writes planned link updates. A note is rewritten only if its text is still exactly what was planned from: a note
 * edited meanwhile (in Notekea, another app or a sync) is skipped and reported, never overwritten.
 */
class ApplyLinkUpdatesUseCase(private val noteContentStore: NoteContentStore) {
    suspend operator fun invoke(
        vaultPath: String,
        rewrites: List<NoteRewrite>
    ): LinkUpdateResult {
        val updated = mutableListOf<RelativePath>()
        val skipped = mutableListOf<RelativePath>()
        rewrites.forEach { rewrite ->
            val isUnchanged = noteContentStore.read(vaultPath = vaultPath, path = rewrite.path) == rewrite.original
            val written = isUnchanged &&
                noteContentStore.write(
                    vaultPath = vaultPath,
                    path = rewrite.path,
                    content = rewrite.updated
                ) is Outcome.Success
            if (written) updated += rewrite.path else skipped += rewrite.path
        }
        return LinkUpdateResult(updated = updated, skipped = skipped)
    }
}
