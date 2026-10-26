package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.AttachmentStore
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/**
 * [AttachmentStore] on Okio. New files are written with `mustCreate`, so a file that appears meanwhile is never
 * overwritten; their folders are created first. Listing skips hidden folders and files, as the vault tree does.
 */
class OkioAttachmentStore(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers
) : AttachmentStore {
    override suspend fun files(
        vaultPath: String
    ): Outcome<List<RelativePath>, NotesError> = withContext(context = dispatchers.io) {
        val root = vaultPath.toPath()
        try {
            val files = fileSystem.listRecursively(dir = root)
                .map {
                    RelativePath(
                        value = it.relativeTo(other = root).segments.joinToString(separator = RelativePath.SEPARATOR)
                    )
                }
                .filter { path ->
                    path.value.split(RelativePath.SEPARATOR).none { it.startsWith(prefix = HIDDEN_PREFIX) }
                }
                .filter { fileSystem.metadataOrNull(path = root / it.value)?.isRegularFile == true }
                .toList()
            Outcome.Success(value = files)
        } catch (_: IOException) {
            Outcome.Failure(error = NotesError.VaultMissing(path = vaultPath))
        }
    }

    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): Outcome<ByteArray, NotesError> = withContext(context = dispatchers.io) {
        try {
            Outcome.Success(
                value = fileSystem.read(file = resolve(vaultPath = vaultPath, path = path)) {
                    readByteArray()
                }
            )
        } catch (_: IOException) {
            Outcome.Failure(error = NotesError.EntryMissing(path = path.value))
        }
    }

    override suspend fun write(
        vaultPath: String,
        path: RelativePath,
        bytes: ByteArray
    ): Outcome<Unit, NotesError> = create(vaultPath = vaultPath, path = path) { target ->
        fileSystem.write(file = target, mustCreate = true) { write(source = bytes) }
    }

    override suspend fun copy(
        vaultPath: String,
        absolutePath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> = create(vaultPath = vaultPath, path = path) { target ->
        fileSystem.read(file = absolutePath.toPath()) {
            val source = this
            fileSystem.write(file = target, mustCreate = true) { writeAll(source = source) }
        }
    }

    override fun pathInVault(
        vaultPath: String,
        absolutePath: String
    ): RelativePath? {
        val root = vaultPath.toPath().normalized()
        val file = absolutePath.toPath().normalized()
        if (!file.toString().startsWith(prefix = root.toString()) || file == root) return null
        return RelativePath(
            value = file.relativeTo(other = root).segments.joinToString(separator = RelativePath.SEPARATOR)
        )
    }

    override fun absolutePath(
        vaultPath: String,
        path: RelativePath
    ): String = resolve(vaultPath = vaultPath, path = path).toString()

    /** Creates [path]'s folders, then the file with [writeFile]; a path in use is [NotesError.NameTaken]. */
    private suspend fun create(
        vaultPath: String,
        path: RelativePath,
        writeFile: (Path) -> Unit
    ): Outcome<Unit, NotesError> = withContext(context = dispatchers.io) {
        val target = resolve(vaultPath = vaultPath, path = path)
        try {
            if (fileSystem.exists(
                    path = target
                )
            ) {
                return@withContext Outcome.Failure(error = NotesError.NameTaken(name = path.name))
            }
            target.parent?.let { fileSystem.createDirectories(dir = it) }
            writeFile(target)
            Outcome.Success(value = Unit)
        } catch (_: IOException) {
            Outcome.Failure(error = NotesError.OperationFailed(path = path.value))
        }
    }

    private fun resolve(
        vaultPath: String,
        path: RelativePath
    ): Path = if (path.isRoot) vaultPath.toPath() else vaultPath.toPath() / path.value

    companion object {
        private const val HIDDEN_PREFIX = "."
    }
}
