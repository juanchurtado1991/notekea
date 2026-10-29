package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NoteContentStore
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/**
 * [NoteContentStore] on Okio. A write goes to a hidden temporary file next to the note (`.Arepas.md.notekea-tmp`),
 * which the tree and the watcher never show, and then replaces the note with an atomic move; on failure the temporary
 * file is removed and the note is left as it was. Windows refuses the move while another reader has the note open for
 * a moment (Notekea itself checking it for outside changes, an antivirus, a sync app), so the move is tried again a few
 * times, [MOVE_PAUSE_MILLIS] apart, before the save fails (1.5-03 failed on CI with "Note save failed").
 */
class OkioNoteContentStore(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers
) : NoteContentStore {
    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): String? = withContext(context = dispatchers.io) {
        try {
            fileSystem.read(file = fileOf(vaultPath = vaultPath, path = path)) { readUtf8() }
        } catch (_: IOException) {
            null
        }
    }

    override suspend fun write(
        vaultPath: String,
        path: RelativePath,
        content: String
    ): Outcome<Unit, NotesError> = withContext(context = dispatchers.io) {
        val file = fileOf(vaultPath = vaultPath, path = path)
        val temporary = file.parent?.div(child = HIDDEN_PREFIX + path.name + TEMPORARY_SUFFIX)
            ?: return@withContext Outcome.Failure(
                error = NotesError.OperationFailed(path = path.value)
            )
        try {
            fileSystem.write(file = temporary) { writeUtf8(string = content) }
            moveRetrying(source = temporary, target = file)
            Outcome.Success(value = Unit)
        } catch (_: IOException) {
            fileSystem.delete(path = temporary, mustExist = false)
            Outcome.Failure(error = NotesError.OperationFailed(path = path.value))
        }
    }

    private suspend fun moveRetrying(
        source: Path,
        target: Path
    ) {
        repeat(times = MOVE_ATTEMPTS - 1) {
            try {
                fileSystem.atomicMove(source = source, target = target)
                return
            } catch (_: IOException) {
                delay(timeMillis = MOVE_PAUSE_MILLIS)
            }
        }
        fileSystem.atomicMove(source = source, target = target)
    }

    private fun fileOf(
        vaultPath: String,
        path: RelativePath
    ): Path = vaultPath.toPath() / path.value

    companion object {
        private const val HIDDEN_PREFIX = "."
        private const val TEMPORARY_SUFFIX = ".notekea-tmp"
        private const val MOVE_ATTEMPTS = 5
        private const val MOVE_PAUSE_MILLIS = 50L
    }
}
