package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NoteFileOperations
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.withContext
import okio.FileMetadata
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/**
 * [NoteFileOperations] on Okio. Creation uses `mustCreate`, so a file that appears between the check and the write is
 * never truncated, and moves are atomic renames. On case-insensitive disks (macOS, Windows) the target of a
 * case-only rename "exists" because it is the same file, so that case is not treated as a name in use.
 */
class OkioNoteFileOperations(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers
) : NoteFileOperations {
    override suspend fun createFile(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> = withContext(context = dispatchers.io) {
        createIfFree(vaultPath = vaultPath, path = path) { fileSystem.write(file = it, mustCreate = true) {} }
    }

    override suspend fun createFolder(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> = withContext(context = dispatchers.io) {
        createIfFree(vaultPath = vaultPath, path = path) { fileSystem.createDirectory(dir = it, mustCreate = true) }
    }

    override suspend fun exists(
        vaultPath: String,
        path: RelativePath
    ): Boolean = withContext(
        context = dispatchers.io
    ) { existsOnDisk(file = resolve(vaultPath = vaultPath, path = path)) }

    override suspend fun move(
        vaultPath: String,
        from: RelativePath,
        to: RelativePath
    ): Outcome<Unit, NotesError> = withContext(context = dispatchers.io) {
        val source = resolve(vaultPath = vaultPath, path = from)
        val target = resolve(vaultPath = vaultPath, path = to)
        val isCaseOnlyRename = from.value.equals(other = to.value, ignoreCase = true)
        when {
            !existsOnDisk(file = source) -> Outcome.Failure(error = NotesError.EntryMissing(path = from.value))

            existsOnDisk(
                file = target
            ) && !isCaseOnlyRename -> Outcome.Failure(error = NotesError.NameTaken(name = to.name))

            else -> attempt(path = from) { fileSystem.atomicMove(source = source, target = target) }
        }
    }

    private fun attempt(
        path: RelativePath,
        change: () -> Unit
    ): Outcome<Unit, NotesError> = try {
        change()
        Outcome.Success(value = Unit)
    } catch (_: IOException) {
        Outcome.Failure(error = NotesError.OperationFailed(path = path.value))
    }

    private fun createIfFree(
        vaultPath: String,
        path: RelativePath,
        create: (Path) -> Unit
    ): Outcome<Unit, NotesError> {
        val target = resolve(vaultPath = vaultPath, path = path)
        val parent = path.parent ?: RelativePath.ROOT
        return when {
            existsOnDisk(file = target) -> {
                Outcome.Failure(error = NotesError.NameTaken(name = path.name))
            }

            !isFolder(file = resolve(vaultPath = vaultPath, path = parent)) -> {
                Outcome.Failure(error = NotesError.EntryMissing(path = parent.value))
            }

            else -> {
                attempt(path = path) { create(target) }
            }
        }
    }

    private fun existsOnDisk(
        file: Path
    ): Boolean = metadataOrNull(file = file) != null

    private fun isFolder(
        file: Path
    ): Boolean = metadataOrNull(file = file)?.isDirectory == true

    private fun metadataOrNull(
        file: Path
    ): FileMetadata? = try {
        fileSystem.metadataOrNull(path = file)
    } catch (_: IOException) {
        null
    }

    private fun resolve(
        vaultPath: String,
        path: RelativePath
    ): Path = if (path.isRoot) vaultPath.toPath() else vaultPath.toPath() / path.value
}
