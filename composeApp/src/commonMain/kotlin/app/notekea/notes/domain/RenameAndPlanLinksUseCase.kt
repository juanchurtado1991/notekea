package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * Renames an entry and plans the updates of the links to it, for callers that do not hold the vault's tree (the
 * editor's title); nothing is rewritten until the person agrees (`ROADMAP.md` 2.5). The vault is listed before the
 * rename, so old links resolve as they did; the tree renames with [RenameEntryUseCase] and [PlanLinkUpdatesUseCase]
 * directly because it already knows every note.
 */
class RenameAndPlanLinksUseCase(
    private val noteScanner: NoteScanner,
    private val renameEntry: RenameEntryUseCase,
    private val planLinkUpdates: PlanLinkUpdatesUseCase
) {
    suspend operator fun invoke(
        vaultPath: String,
        path: RelativePath,
        newName: String
    ): Outcome<RenamedEntry, NotesError> {
        val listing = when (val scanned = noteScanner.scan(vaultPath = vaultPath)) {
            is Outcome.Failure -> return scanned
            is Outcome.Success -> scanned.value
        }
        return when (val renamed = renameEntry(vaultPath = vaultPath, path = path, newName = newName)) {
            is Outcome.Failure -> renamed

            is Outcome.Success -> {
                val rewrites = if (renamed.value == path) {
                    emptyList()
                } else {
                    planLinkUpdates(
                        vaultPath = vaultPath,
                        notesBefore = listing.files.map { it.path }.toSet(),
                        from = path,
                        to = renamed.value
                    )
                }
                Outcome.Success(value = RenamedEntry(path = renamed.value, rewrites = rewrites))
            }
        }
    }
}
