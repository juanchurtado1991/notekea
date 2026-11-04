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
 * never truncated, moves are atomic renames, and a path that leaves the vault changes nothing. On case-insensitive disks (macOS, Windows) the target of a
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

    override suspend fun deleteEmptyFile(
        vaultPath: String,
        path: RelativePath
    ) = withContext(context = dispatchers.io) {
        val file = resolve(vaultPath = vaultPath, path = path)
        try {
            if (fileSystem.metadataOrNull(path = file)?.size == 0L) fileSystem.delete(path = file)
        } catch (_: IOException) {
            // An empty file left behind holds nothing to lose.
        }
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
            !isInVault(vaultPath = vaultPath, file = target) -> Outcome.Failure(error = NotesError.InvalidName)

            !existsOnDisk(file = source) -> Outcome.Failure(error = NotesError.EntryMissing(path = from.value))

            existsOnDisk(file = target) && (!isCaseOnlyRename || isListed(file = target)) -> {
                Outcome.Failure(error = NotesError.NameTaken(name = to.name))
            }

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
            !isInVault(vaultPath = vaultPath, file = target) -> {
                Outcome.Failure(error = NotesError.InvalidName)
            }

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

    /** Whether [file], once its `..` are followed (Okio takes `\\` for a separator too), is inside the vault. */
    private fun isInVault(
        vaultPath: String,
        file: Path
    ): Boolean {
        val vault = vaultPath.toPath(normalize = true)
        val normalized = file.normalized()
        return normalized.root == vault.root && normalized.segments.take(n = vault.segments.size) == vault.segments
    }

    /**
     * Whether [file]'s folder lists it by that exact name. A disk that ignores letter case finds `Foo.md` when only
     * `foo.md` exists, the same file; on one that does not, a listed `Foo.md` is another note, never to be replaced.
     */
    private fun isListed(
        file: Path
    ): Boolean = file.parent?.let { fileSystem.listOrNull(dir = it) }.orEmpty().any { it.name == file.name }

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
