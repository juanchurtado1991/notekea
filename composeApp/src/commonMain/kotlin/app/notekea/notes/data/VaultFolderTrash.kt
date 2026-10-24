package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.Trash
import app.notekea.notes.domain.TrashLocation
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/**
 * [Trash] in the vault's own `.trash` folder, the same place Obsidian uses, so it works on every system and the vault
 * tree already hides it. Entries keep their name; when it is in use, a number is added (`Arepas 2.md`).
 */
class VaultFolderTrash(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers
) : Trash {
    override suspend fun moveToTrash(
        vaultPath: String,
        path: RelativePath
    ): Outcome<TrashLocation, NotesError> = withContext(context = dispatchers.io) {
        val source = vaultPath.toPath() / path.value
        if (fileSystem.metadataOrNull(path = source) == null) {
            return@withContext Outcome.Failure(error = NotesError.EntryMissing(path = path.value))
        }
        try {
            val trashFolder = vaultPath.toPath() / TRASH_FOLDER
            fileSystem.createDirectories(dir = trashFolder)
            fileSystem.atomicMove(source = source, target = freeTarget(trashFolder = trashFolder, path = path))
            Outcome.Success(value = TrashLocation.VAULT_TRASH_FOLDER)
        } catch (_: IOException) {
            Outcome.Failure(error = NotesError.OperationFailed(path = path.value))
        }
    }

    private fun freeTarget(
        trashFolder: Path,
        path: RelativePath
    ): Path {
        val extension = path.name.removePrefix(prefix = path.nameWithoutExtension)
        return generateSequence(seed = FIRST_SUFFIX) { it + 1 }
            .map { number ->
                val name = if (number == FIRST_SUFFIX) path.name else "${path.nameWithoutExtension} $number$extension"
                trashFolder / name
            }.first { fileSystem.metadataOrNull(path = it) == null }
    }

    companion object {
        const val TRASH_FOLDER = ".trash"
        private const val FIRST_SUFFIX = 1
    }
}
