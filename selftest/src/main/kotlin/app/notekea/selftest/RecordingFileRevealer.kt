package app.notekea.selftest

import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.FileRevealer
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath

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
