package app.notekea.core.notes.domain

import app.notekea.core.domain.Outcome
import kotlinx.coroutines.CompletableDeferred

/**
 * A vault's files in memory, like the disk: a write or copy to a path in use fails with [NotesError.NameTaken], and
 * every file written is also seen by [fileOperations]. [outside] holds files outside the vault, by absolute path, to
 * copy from. [listingFails] makes listing fail; a copy takes until [copied] completes; every call goes through [gate].
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

    override suspend fun files(vaultPath: String): Outcome<List<RelativePath>, NotesError> {
        gate.pass()

        return if (listingFails) {
            Outcome.Failure(error = NotesError.OperationFailed(path = vaultPath))
        } else {
            Outcome.Success(value = contents.keys.sortedBy { it.value })
        }
    }

    /** Folders with no file in them, which [folders] lists beside those of the files. */
    val emptyFolders = mutableListOf<RelativePath>()

    override suspend fun folders(vaultPath: String): List<RelativePath> {
        val holding = contents.keys.flatMap { file ->
            generateSequence(seed = file.parent) { it.parent }
                .takeWhile {
                    !it.isRoot
                }
                .toList()
        }

        // As the disk does: the hidden ones (.obsidian, .trash) are no folders to offer.
        return (holding + emptyFolders).distinct().filterNot { folder ->
            folder
                .value
                .split('/')
                .any { it.startsWith(prefix = ".") }
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

        val isTaken = path in contents || path in fileOperations.existing

        if (isTaken) {
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

        val bytes = outside[absolutePath]
            ?: return Outcome.Failure(error = NotesError.EntryMissing(absolutePath))

        return write(
            vaultPath = vaultPath,
            path = path,
            bytes = bytes
        )
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
