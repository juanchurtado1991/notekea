package app.notekea.notes.domain

/**
 * After [from] was renamed to [to], rewrites the links that pointed to it in every other note, so no link breaks
 * (`ROADMAP.md` 1.4). [notesBefore] are the vault's notes before the rename, so old links resolve as they did. Returns
 * null when no note links to the entry, so callers report nothing. Used by every place that renames: the tree and the
 * editor's title.
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
        val rewrites = planLinkUpdates(vaultPath = vaultPath, notesBefore = notesBefore, from = from, to = to)
        return if (rewrites.isEmpty()) null else applyLinkUpdates(vaultPath = vaultPath, rewrites = rewrites)
    }
}
