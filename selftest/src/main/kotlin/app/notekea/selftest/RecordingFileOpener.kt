package app.notekea.selftest

import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.FileOpener
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath

/** Opening a file with its app, in a case: it records the file instead of opening another app on the machine. */
class RecordingFileOpener : FileOpener {
    val opened = mutableListOf<RelativePath>()

    override suspend fun open(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> {
        opened += path
        return Outcome.Success(value = Unit)
    }
}
