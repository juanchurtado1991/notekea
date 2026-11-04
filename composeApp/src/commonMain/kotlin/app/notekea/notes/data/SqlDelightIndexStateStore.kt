package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.db.NotekeaDatabase
import app.notekea.notes.domain.IndexStateStore
import app.notekea.notes.domain.IndexedNote
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SkipReason
import co.touchlab.kermit.Logger
import kotlinx.coroutines.withContext
import kotlin.time.Instant

/**
 * [IndexStateStore] on SQLDelight, in the app's database. A database error only costs time: reading answers as empty
 * (the notes are indexed again) and a failed write is skipped, both logged without note content.
 */
class SqlDelightIndexStateStore(
    database: NotekeaDatabase,
    private val dispatchers: AppDispatchers
) : IndexStateStore {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val queries = database.noteIndexStateQueries

    override suspend fun read(
        vaultPath: String
    ): List<IndexedNote> = withContext(context = dispatchers.io) {
        guarded(fallback = emptyList()) {
            queries.selectStates(vault = vaultPath).executeAsList().map { row ->
                IndexedNote(
                    file = NoteFile(
                        path = RelativePath(value = row.path),
                        size = row.size,
                        modifiedAt = row.modified_at?.let { Instant.fromEpochMilliseconds(epochMilliseconds = it) }
                    ),
                    contentHash = row.content_hash,
                    version = row.version,
                    skipReason = SkipReason.entries.find { it.name == row.skip_reason }
                )
            }
        }
    }

    override suspend fun save(
        vaultPath: String,
        note: IndexedNote
    ) = withContext(context = dispatchers.io) {
        guarded<Unit>(fallback = Unit) {
            queries.upsertState(
                vault = vaultPath,
                path = note.file.path.value,
                content_hash = note.contentHash,
                size = note.file.size,
                modified_at = note.file.modifiedAt?.toEpochMilliseconds(),
                version = note.version,
                skip_reason = note.skipReason?.name
            )
        }
    }

    override suspend fun delete(
        vaultPath: String,
        path: RelativePath
    ) = withContext(context = dispatchers.io) {
        guarded<Unit>(fallback = Unit) { queries.deleteState(vault = vaultPath, path = path.value) }
    }

    override suspend fun clear(
        vaultPath: String
    ): Boolean = withContext(context = dispatchers.io) {
        guarded(fallback = false) {
            queries.deleteVault(vault = vaultPath)
            true
        }
    }

    private fun <T> guarded(
        fallback: T,
        block: () -> T
    ): T = try {
        block()
    } catch (exception: Exception) {
        if (!isDatabaseFailure(exception = exception)) throw exception
        logger.w(throwable = exception) { FAILED_MESSAGE }
        fallback
    }

    companion object {
        private const val LOG_TAG = "IndexStateStore"
        private const val FAILED_MESSAGE = "Index state unavailable; notes will be indexed again"
    }
}
