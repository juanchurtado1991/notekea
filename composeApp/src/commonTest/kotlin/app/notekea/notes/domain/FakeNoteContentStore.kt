package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.yield

/**
 * Note texts in memory; writes to [refused] paths fail as a locked file would. With [suspendsLikeDisk], every read and
 * write suspends as the real store does when it switches to the IO dispatcher, so a cancelled caller is stopped there.
 * [reads] lists every path read, in order, and [onRead] runs after each read.
 */
class FakeNoteContentStore : NoteContentStore {
    val texts = mutableMapOf<RelativePath, String>()
    val refused = mutableSetOf<RelativePath>()
    var suspendsLikeDisk = false
    val reads = mutableListOf<RelativePath>()
    var onRead: () -> Unit = {}

    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): String? {
        if (suspendsLikeDisk) yield()
        reads += path
        onRead()
        return texts[path]
    }

    override suspend fun write(
        vaultPath: String,
        path: RelativePath,
        content: String
    ): Outcome<Unit, NotesError> {
        if (suspendsLikeDisk) yield()
        if (path in refused) return Outcome.Failure(error = NotesError.OperationFailed(path = path.value))
        texts[path] = content
        return Outcome.Success(value = Unit)
    }
}
