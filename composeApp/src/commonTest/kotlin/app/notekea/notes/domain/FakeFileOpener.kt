package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/** Opens files by recording them; [failure] makes every opening fail with it, as a missing app would. */
class FakeFileOpener : FileOpener {
    val opened = mutableListOf<RelativePath>()
    var failure: NotesError? = null

    override suspend fun open(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> {
        failure?.let { return Outcome.Failure(error = it) }
        opened += path
        return Outcome.Success(value = Unit)
    }
}
