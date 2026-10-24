package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/** Deletes a note or folder recoverably, telling where it went (the system trash or the vault's `.trash` folder). */
class DeleteEntryUseCase(private val trash: Trash) {
    suspend operator fun invoke(
        vaultPath: String,
        path: RelativePath
    ): Outcome<TrashLocation, NotesError> = trash.moveToTrash(vaultPath = vaultPath, path = path)
}
