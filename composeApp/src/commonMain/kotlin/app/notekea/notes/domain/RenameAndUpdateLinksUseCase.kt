package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * Renames an entry and updates the links to it, for callers that do not hold the vault's tree (the editor's title).
 * The vault is listed before the rename, so old links resolve as they did; the tree renames with
 * [RenameEntryUseCase] and [UpdateLinksAfterRenameUseCase] directly because it already knows every note.
 */
class RenameAndUpdateLinksUseCase(
    private val noteScanner: NoteScanner,
    private val renameEntry: RenameEntryUseCase,
    private val updateLinksAfterRename: UpdateLinksAfterRenameUseCase
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
                val links = if (renamed.value == path) {
                    null
                } else {
                    updateLinksAfterRename(
                        vaultPath = vaultPath,
                        notesBefore = listing.files.map { it.path }.toSet(),
                        from = path,
                        to = renamed.value
                    )
                }
                Outcome.Success(value = RenamedEntry(path = renamed.value, links = links))
            }
        }
    }
}
