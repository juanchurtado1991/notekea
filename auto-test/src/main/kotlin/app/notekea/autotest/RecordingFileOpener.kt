package app.notekea.autotest

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.FileOpener
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath

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
