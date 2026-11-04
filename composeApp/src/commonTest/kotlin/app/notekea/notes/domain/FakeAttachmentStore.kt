package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.CompletableDeferred

/**
 * A vault's files in memory, like the disk: a write or copy to a path in use fails with [NotesError.NameTaken], and
 * every file written is also seen by [fileOperations], as the real disk is. [outside] holds files on the computer
 * outside the vault, by absolute path, to copy from; a path under `VAULT` is inside it. While [listingFails], listing
 * the vault fails as a disk error would. A copy takes until [copied] completes, when a test sets it. Every call that
 * touches the disk goes through [gate].
 */
class FakeAttachmentStore(
    private val fileOperations: FakeNoteFileOperations = FakeNoteFileOperations()
) : AttachmentStore {
    val contents = mutableMapOf<RelativePath, ByteArray>()
    val outside = mutableMapOf<String, ByteArray>()
    val gate = Gate()
    var listingFails = false
    var copied: CompletableDeferred<Unit>? = null

    /** Every file read, in order, so a test can tell a picture decoded again from one kept. */
    val reads = mutableListOf<RelativePath>()

    override suspend fun files(
        vaultPath: String
    ): Outcome<List<RelativePath>, NotesError> {
        gate.pass()
        return if (listingFails) {
            Outcome.Failure(error = NotesError.OperationFailed(path = vaultPath))
        } else {
            Outcome.Success(value = contents.keys.sortedBy { it.value })
        }
    }

    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): Outcome<ByteArray, NotesError> {
        gate.pass()
        reads += path
        return contents[path]?.let { Outcome.Success(value = it) }
            ?: Outcome.Failure(error = NotesError.EntryMissing(path.value))
    }

    override suspend fun write(
        vaultPath: String,
        path: RelativePath,
        bytes: ByteArray
    ): Outcome<Unit, NotesError> {
        gate.pass()
        if (path in contents || path in fileOperations.existing) {
            return Outcome.Failure(error = NotesError.NameTaken(name = path.name))
        }
        contents[path] = bytes
        fileOperations.existing += path
        return Outcome.Success(value = Unit)
    }

    override suspend fun copy(
        vaultPath: String,
        absolutePath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> {
        copied?.await()
        val bytes = outside[absolutePath] ?: return Outcome.Failure(error = NotesError.EntryMissing(absolutePath))
        return write(vaultPath = vaultPath, path = path, bytes = bytes)
    }

    override fun pathInVault(
        vaultPath: String,
        absolutePath: String
    ): RelativePath? = absolutePath.takeIf { it.startsWith(prefix = "$vaultPath/") }
        ?.let { RelativePath(value = it.removePrefix(prefix = "$vaultPath/")) }

    override fun absolutePath(
        vaultPath: String,
        path: RelativePath
    ): String = "$vaultPath/${path.value}"
}
