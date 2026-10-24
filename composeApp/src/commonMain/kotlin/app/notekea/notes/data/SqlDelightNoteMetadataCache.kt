package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.db.NotekeaDatabase
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.NoteMetadataCache
import app.notekea.notes.domain.RelativePath
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlin.time.Instant

/**
 * [NoteMetadataCache] on SQLDelight, in the app's database (never inside the vault). Dates are stored as epoch
 * milliseconds. A database error only costs speed: reading answers as empty and a failed update is skipped, both
 * logged without note content, and the notes are read from disk again.
 */
class SqlDelightNoteMetadataCache(
    private val database: NotekeaDatabase,
    private val dispatchers: AppDispatchers
) : NoteMetadataCache {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val queries = database.noteCacheQueries

    override suspend fun read(
        vaultPath: String
    ): List<NoteEntry> = withContext(context = dispatchers.io) {
        guarded(fallback = emptyList(), failureMessage = READ_FAILED) {
            val tags = queries.selectTags(vault = vaultPath).executeAsList().groupBy({ it.path }, { it.tag })
            val aliases = queries.selectAliases(vault = vaultPath).executeAsList().groupBy({ it.path }, { it.alias })
            queries.selectNotes(vault = vaultPath).executeAsList().map { row ->
                NoteEntry(
                    file = NoteFile(
                        path = RelativePath(value = row.path),
                        size = row.size,
                        modifiedAt = row.modified_at?.let { Instant.fromEpochMilliseconds(epochMilliseconds = it) }
                    ),
                    metadata = NoteMetadata(
                        tags = tags[row.path].orEmpty(),
                        aliases = aliases[row.path].orEmpty(),
                        title = row.title
                    )
                )
            }
        }
    }

    override suspend fun update(
        vaultPath: String,
        upserted: List<NoteEntry>,
        removed: List<RelativePath>
    ) = withContext(context = dispatchers.io) {
        guarded(fallback = Unit, failureMessage = UPDATE_FAILED) {
            database.transaction {
                removed.forEach { delete(vaultPath = vaultPath, path = it.value) }
                upserted.forEach { upsert(vaultPath = vaultPath, entry = it) }
            }
        }
    }

    private fun delete(
        vaultPath: String,
        path: String
    ) {
        queries.deleteNote(vault = vaultPath, path = path)
        queries.deleteTags(vault = vaultPath, path = path)
        queries.deleteAliases(vault = vaultPath, path = path)
    }

    private fun <T> guarded(
        fallback: T,
        failureMessage: String,
        block: () -> T
    ): T = try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (exception: Exception) {
        logger.w(throwable = exception) { failureMessage }
        fallback
    }

    private fun upsert(
        vaultPath: String,
        entry: NoteEntry
    ) {
        val path = entry.file.path.value
        delete(vaultPath = vaultPath, path = path)
        queries.upsertNote(
            vault = vaultPath,
            path = path,
            size = entry.file.size,
            modified_at = entry.file.modifiedAt?.toEpochMilliseconds(),
            title = entry.metadata.title
        )
        entry.metadata.tags.forEachIndexed {
            index,
            tag
            ->
            queries.insertTag(vault = vaultPath, path = path, position = index.toLong(), tag = tag)
        }
        entry.metadata.aliases.forEachIndexed { index, alias ->
            queries.insertAlias(vault = vaultPath, path = path, position = index.toLong(), alias = alias)
        }
    }

    companion object {
        private const val LOG_TAG = "NoteMetadataCache"
        private const val READ_FAILED = "Note cache unreadable; reading the notes from disk"
        private const val UPDATE_FAILED = "Note cache update failed; it will be refreshed on the next scan"
    }
}
