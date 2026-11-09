package app.notekea.core.notes.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.yield

/**
 * Note texts in memory; writes to [refused] paths and reads of [unreadable] ones fail. With [suspendsLikeDisk], every
 * read and write suspends as the real store does. [reads] lists every path read and [onRead] runs after each.
 * [staleReads] are answered first, one per read, as a read that began before a later write; reads wait for [readGate].
 */
class FakeNoteContentStore : NoteContentStore {
    val texts = mutableMapOf<RelativePath, String>()
    val refused = mutableSetOf<RelativePath>()
    val unreadable = mutableSetOf<RelativePath>()
    var suspendsLikeDisk = false
    val reads = mutableListOf<RelativePath>()
    var onRead: () -> Unit = {}
    val staleReads = ArrayDeque<String>()
    var readGate: CompletableDeferred<Unit>? = null

    override suspend fun readText(
        vaultPath: String,
        path: RelativePath
    ): Outcome<String, NotesError> {
        if (suspendsLikeDisk) yield()
        readGate?.await()
        reads += path
        onRead()

        if (path in unreadable) {
            return Outcome.Failure(error = NotesError.Unreadable(path = path.value))
        }

        val text = staleReads.removeFirstOrNull() ?: texts[path]

        return text?.let { Outcome.Success(value = it) }
            ?: Outcome.Failure(error = NotesError.EntryMissing(path = path.value))
    }

    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): String? = (
        readText(
            vaultPath = vaultPath,
            path = path
        ) as? Outcome.Success
        )?.value

    override suspend fun write(
        vaultPath: String,
        path: RelativePath,
        content: String
    ): Outcome<Unit, NotesError> {
        if (suspendsLikeDisk) yield()

        if (path in refused) {
            return Outcome.Failure(error = NotesError.OperationFailed(path = path.value))
        }

        texts[path] = content

        return Outcome.Success(value = Unit)
    }
}
