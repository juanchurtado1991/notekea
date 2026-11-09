package app.notekea.core.notes.domain

/**
 * After `from` was renamed to [to], rewrites the links that pointed to it in every other note, so no link breaks;
 * `notesBefore` are the vault's notes before the rename, so old links resolve as they did. Returns null when no note
 * links to the entry, so callers report nothing.
 */
class UpdateLinksAfterRenameUseCase(
    private val planLinkUpdates: PlanLinkUpdatesUseCase,
    private val applyLinkUpdates: ApplyLinkUpdatesUseCase
) {
    suspend operator fun invoke(
        vaultPath: String,
        notesBefore: Set<RelativePath>,
        from: RelativePath,
        to: RelativePath
    ): LinkUpdateResult? {
        val rewrites = planLinkUpdates(
            vaultPath = vaultPath,
            notesBefore = notesBefore,
            from = from,
            to = to
        )

        return if (rewrites.isEmpty()) {
            null
        } else {
            applyLinkUpdates(
                vaultPath = vaultPath,
                rewrites = rewrites
            )
        }
    }
}
