package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.NoteTimes
import app.notekea.notes.domain.NoteTimesReader
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath
import kotlin.time.Instant

/** [NoteTimesReader] on Okio's file metadata; a file system that keeps no creation time leaves it null. */
class OkioNoteTimesReader(
    private val fileSystem: FileSystem,
    private val dispatchers: AppDispatchers
) : NoteTimesReader {
    override suspend fun times(
        vaultPath: String,
        path: RelativePath
    ): NoteTimes = withContext(context = dispatchers.io) {
        val metadata = try {
            fileSystem.metadataOrNull(path = vaultPath.toPath() / path.value)
        } catch (_: IOException) {
            null
        }
        NoteTimes(
            createdAt = metadata?.createdAtMillis?.let { Instant.fromEpochMilliseconds(epochMilliseconds = it) },
            modifiedAt = metadata?.lastModifiedAtMillis?.let { Instant.fromEpochMilliseconds(epochMilliseconds = it) }
        )
    }
}
