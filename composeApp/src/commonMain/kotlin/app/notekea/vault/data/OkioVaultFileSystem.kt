package app.notekea.vault.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.vault.domain.FolderAccess
import app.notekea.vault.domain.VaultError
import app.notekea.vault.domain.VaultFileSystem
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/**
 * [VaultFileSystem] on Okio. A path that exists but is a file counts as a missing folder, and a folder that cannot be
 * listed counts as not readable. Note counting skips hidden folders (`.obsidian`, `.git`, `.trash`) and does not
 * follow symbolic links, so a link pointing back up the tree cannot loop forever.
 */
class OkioVaultFileSystem(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers,
    private val homeDirectory: String
) : VaultFileSystem {
    override suspend fun access(
        path: String
    ): FolderAccess = withContext(context = dispatchers.io) { accessOf(folder = path.toPath()) }

    override suspend fun countNotes(
        path: String
    ): Int = withContext(context = dispatchers.io) { countNotesIn(folder = path.toPath()) }

    override suspend fun createFolder(
        parentPath: String,
        name: String
    ): Outcome<String, VaultError> = withContext(context = dispatchers.io) {
        val folder = parentPath.toPath() / name
        when {
            accessOf(folder = parentPath.toPath()) != FolderAccess.AVAILABLE -> {
                Outcome.Failure(error = VaultError.FolderMissing(path = parentPath))
            }

            fileSystem.exists(path = folder) && !isEmptyFolder(folder = folder) -> {
                Outcome.Failure(error = VaultError.FolderAlreadyExists(path = folder.toString()))
            }

            else -> {
                createDirectories(folder = folder)
            }
        }
    }

    override suspend fun suggestedParentFolder(): String = withContext(context = dispatchers.io) {
        val documents = homeDirectory.toPath() / DOCUMENTS_FOLDER
        if (isDirectory(path = documents)) documents.toString() else homeDirectory
    }

    private fun accessOf(
        folder: Path
    ): FolderAccess = try {
        if (isDirectory(path = folder)) {
            fileSystem.list(dir = folder)
            FolderAccess.AVAILABLE
        } else {
            FolderAccess.MISSING
        }
    } catch (_: IOException) {
        FolderAccess.NOT_READABLE
    }

    private fun countNotesIn(
        folder: Path
    ): Int = listOrEmpty(folder = folder)
        .filterNot { it.name.startsWith(prefix = HIDDEN_PREFIX) }
        .sumOf { noteCountOf(entry = it) }

    private fun createDirectories(
        folder: Path
    ): Outcome<String, VaultError> = try {
        fileSystem.createDirectories(dir = folder)
        Outcome.Success(value = folder.toString())
    } catch (_: IOException) {
        Outcome.Failure(error = VaultError.CreationFailed(path = folder.toString()))
    }

    private fun isDirectory(
        path: Path
    ): Boolean = fileSystem.metadataOrNull(path = path)?.isDirectory == true

    private fun isEmptyFolder(
        folder: Path
    ): Boolean = isDirectory(path = folder) && listOrEmpty(folder = folder).isEmpty()

    private fun listOrEmpty(
        folder: Path
    ): List<Path> = fileSystem.listOrNull(dir = folder).orEmpty()

    private fun noteCountOf(
        entry: Path
    ): Int = try {
        when {
            isDirectory(path = entry) -> countNotesIn(folder = entry)
            entry.name.endsWith(suffix = MARKDOWN_EXTENSION, ignoreCase = true) -> 1
            else -> 0
        }
    } catch (_: IOException) {
        0
    }

    companion object {
        private const val DOCUMENTS_FOLDER = "Documents"
        private const val HIDDEN_PREFIX = "."
        private const val MARKDOWN_EXTENSION = ".md"
    }
}
