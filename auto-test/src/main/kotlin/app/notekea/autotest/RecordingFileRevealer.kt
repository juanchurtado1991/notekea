package app.notekea.autotest

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.tree.domain.FileRevealer

/** Show in Files, in a case: it records the file instead of opening the file manager. */
class RecordingFileRevealer : FileRevealer {
    val revealed = mutableListOf<RelativePath>()

    override suspend fun reveal(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> {
        revealed += path

        return Outcome.Success(value = Unit)
    }
}
