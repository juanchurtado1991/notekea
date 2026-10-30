package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * A vault's files in memory, like the disk: a write or copy to a path in use fails with [NotesError.NameTaken], and
 * every file written is also seen by [fileOperations], as the real disk is. [outside] holds files on the computer
 * outside the vault, by absolute path, to copy from; a path under `VAULT` is inside it.
 */
class FakeAttachmentStore(
    private val fileOperations: FakeNoteFileOperations = FakeNoteFileOperations()
) : AttachmentStore {
    val contents = mutableMapOf<RelativePath, ByteArray>()
    val outside = mutableMapOf<String, ByteArray>()

    /** Every file read, in order, so a test can tell a picture decoded again from one kept. */
    val reads = mutableListOf<RelativePath>()

    override suspend fun files(
        vaultPath: String
    ): Outcome<List<RelativePath>, NotesError> = Outcome.Success(value = contents.keys.sortedBy { it.value })

    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): Outcome<ByteArray, NotesError> {
        reads += path
        return contents[path]?.let { Outcome.Success(value = it) }
            ?: Outcome.Failure(error = NotesError.EntryMissing(path.value))
    }

    override suspend fun write(
        vaultPath: String,
        path: RelativePath,
        bytes: ByteArray
    ): Outcome<Unit, NotesError> {
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
