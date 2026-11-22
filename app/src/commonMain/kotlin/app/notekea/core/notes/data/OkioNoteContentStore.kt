package app.notekea.core.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteFileAttributes
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/**
 * [NoteContentStore] on Okio. A write goes to a hidden temporary file next to the note, reaches
 * the disk itself, takes the note's [attributes] and replaces the note with an atomic move; on
 * failure the note is left as it was ([writeWhole]).
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
        val file = fileOf(
            vaultPath = vaultPath,
            path = path
        )

        try {
            val bytes = fileSystem.read(file = file) { readByteArray() }
            Outcome.Success(value = bytes.decodeToString(throwOnInvalidSequence = true))
        } catch (_: IOException) {
            Outcome.Failure(
                error = fileSystem.readFailureOf(
                    file = file,
                    path = path
                )
            )
        } catch (_: CharacterCodingException) {
            Outcome.Failure(error = NotesError.Unreadable(path = path.value))
        }
    }

    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): String? = (
        readText(
            vaultPath = vaultPath,
            path = path
        ) as? Outcome.Success
        )?.value

    override suspend fun write(
        vaultPath: String,
        path: RelativePath,
        content: String
    ): Outcome<Unit, NotesError> = withContext(context = dispatchers.io) {
        val file = fileOf(
            vaultPath = vaultPath,
            path = path
        )

        try {
            fileSystem.writeWhole(
                file = file,
                content = content,
                beforeMove = { temporary ->
                    attributes.copy(
                        sourcePath = file.toString(),
                        targetPath = temporary.toString()
                    )
                }
            )

            Outcome.Success(value = Unit)
        } catch (_: IOException) {
            Outcome.Failure(error = NotesError.OperationFailed(path = path.value))
        }
    }

    private fun fileOf(
        vaultPath: String,
        path: RelativePath
    ): Path = vaultPath.toPath() / path.value
}
