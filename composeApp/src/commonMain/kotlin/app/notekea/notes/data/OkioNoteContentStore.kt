package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NoteContentStore
import app.notekea.notes.domain.NoteFileAttributes
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.use
import kotlin.random.Random

/**
 * [NoteContentStore] on Okio. A write goes to a hidden temporary file next to the note (`.3fa9c1d2.notekea-tmp`), named
 * apart from the note so any name the file system takes can be saved, which the tree and the watcher never show, reaches the disk itself, so a power cut cannot leave a renamed empty file,
 * takes the note's [attributes], and then replaces the note with an atomic move; on failure the temporary file is removed and the note is left as it
 * was. Windows refuses the move while another reader has the note open for
 * a moment (Notekea itself checking it for outside changes, an antivirus, a sync app), so the move is tried again a few
 * times, [MOVE_PAUSE_MILLIS] apart, before the save fails (1.5-03 failed on CI with "Note save failed").
 */
class OkioNoteContentStore(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers,
    private val attributes: NoteFileAttributes
) : NoteContentStore {
    override suspend fun readText(
        vaultPath: String,
        path: RelativePath
    ): Outcome<String, NotesError> = withContext(context = dispatchers.io) {
        val file = fileOf(vaultPath = vaultPath, path = path)
        try {
            val bytes = fileSystem.read(file = file) { readByteArray() }
            Outcome.Success(value = bytes.decodeToString(throwOnInvalidSequence = true))
        } catch (_: IOException) {
            Outcome.Failure(error = fileSystem.readFailureOf(file = file, path = path))
        } catch (_: CharacterCodingException) {
            Outcome.Failure(error = NotesError.Unreadable(path = path.value))
        }
    }

    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): String? = (readText(vaultPath = vaultPath, path = path) as? Outcome.Success)?.value

    override suspend fun write(
        vaultPath: String,
        path: RelativePath,
        content: String
    ): Outcome<Unit, NotesError> = withContext(context = dispatchers.io) {
        val file = fileOf(vaultPath = vaultPath, path = path)
        val temporary = file.parent?.div(child = temporaryName())
            ?: return@withContext Outcome.Failure(
                error = NotesError.OperationFailed(path = path.value)
            )
        try {
            writeDurably(file = temporary, content = content)
            attributes.copy(sourcePath = file.toString(), targetPath = temporary.toString())
            moveRetrying(source = temporary, target = file)
            Outcome.Success(value = Unit)
        } catch (_: IOException) {
            Outcome.Failure(error = NotesError.OperationFailed(path = path.value))
        } finally {
            removeLeftover(file = temporary)
        }
    }

    /** Runs however a write ends, a cancelled one too: nothing else knows this random name. */
    private fun removeLeftover(
        file: Path
    ) {
        try {
            fileSystem.delete(path = file, mustExist = false)
        } catch (_: IOException) {
            // The note is as it should be; a locked temporary file stays hidden.
        }
    }

    /** The same length for every note; random, so two notes saved at once never share one. */
    private fun temporaryName(): String {
        val id = Random.nextInt().toUInt().toString(
            radix = HEX_RADIX
        ).padStart(length = ID_LENGTH, padChar = ID_PADDING)
        return HIDDEN_PREFIX + id + TEMPORARY_SUFFIX
    }

    private fun writeDurably(
        file: Path,
        content: String
    ) {
        val bytes = Buffer().writeUtf8(string = content)
        fileSystem.openReadWrite(file = file, mustCreate = true).use { handle ->
            handle.write(fileOffset = 0L, source = bytes, byteCount = bytes.size)
            handle.flush()
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
        private const val HEX_RADIX = 16
        private const val HIDDEN_PREFIX = "."
        private const val ID_LENGTH = 8
        private const val ID_PADDING = '0'
        private const val TEMPORARY_SUFFIX = ".notekea-tmp"
        private const val MOVE_ATTEMPTS = 5
        private const val MOVE_PAUSE_MILLIS = 50L
    }
}
