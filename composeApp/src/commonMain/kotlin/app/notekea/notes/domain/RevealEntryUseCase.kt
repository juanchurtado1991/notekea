package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/** Shows a note or folder in the system file manager. */
class RevealEntryUseCase(private val fileRevealer: FileRevealer) {
    suspend operator fun invoke(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> = fileRevealer.reveal(vaultPath = vaultPath, path = path)
}
