package app.notekea.notes.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.db.NotekeaDatabase
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class SqlDelightNoteMetadataCacheTest {
    private val testDispatcher = StandardTestDispatcher()
    private val driver = JdbcSqliteDriver(url = JdbcSqliteDriver.IN_MEMORY).also { NotekeaDatabase.Schema.create(it) }
    private val cache = SqlDelightNoteMetadataCache(
        database = NotekeaDatabase(driver = driver),
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    @Test
    fun `notes come back with their metadata in order, per vault`() = runTest(context = testDispatcher) {
        cache.update(vaultPath = NOTES, upserted = listOf(AREPAS, LISBON), removed = emptyList())
        cache.update(vaultPath = WORK, upserted = listOf(LISBON), removed = emptyList())

        assertEquals(
            expected = listOf(AREPAS, LISBON),
            actual = cache.read(vaultPath = NOTES).sortedBy { it.file.path.value }
        )
        assertEquals(
            expected = listOf(LISBON),
            actual = cache.read(vaultPath = WORK)
        )
    }

    @Test
    fun `a changed note replaces its tags and a removed one is forgotten`() = runTest(context = testDispatcher) {
        cache.update(vaultPath = NOTES, upserted = listOf(AREPAS, LISBON), removed = emptyList())
        val retagged = AREPAS.copy(metadata = AREPAS.metadata.copy(tags = listOf("dinner"), aliases = emptyList()))

        cache.update(vaultPath = NOTES, upserted = listOf(retagged), removed = listOf(LISBON.file.path))

        assertEquals(
            expected = listOf(retagged),
            actual = cache.read(vaultPath = NOTES)
        )
    }

    @Test
    fun `a broken database reads as empty and skips updates instead of failing`() = runTest(context = testDispatcher) {
        cache.update(vaultPath = NOTES, upserted = listOf(AREPAS), removed = emptyList())
        driver.execute(identifier = null, sql = "DROP TABLE note_cache", parameters = 0)

        cache.update(vaultPath = NOTES, upserted = listOf(LISBON), removed = emptyList())

        assertEquals(
            expected = emptyList(),
            actual = cache.read(vaultPath = NOTES)
        )
    }

    companion object {
        private const val NOTES = "/home/ana/Notes"
        private const val WORK = "/home/ana/Work"
        private val AREPAS = NoteEntry(
            file = NoteFile(
                path = RelativePath(value = "Recipes/Arepas.md"),
                size = 120L,
                modifiedAt = Instant.fromEpochMilliseconds(epochMilliseconds = 1_800_000_000_000L)
            ),
            metadata = NoteMetadata(
                tags = listOf("recipe", "venezuelan"),
                aliases = listOf("Arepa", "Corn bread"),
                title = "Arepas"
            )
        )
        private val LISBON = NoteEntry(
            file = NoteFile(path = RelativePath(value = "Travel/Lisbon trip.md"), size = null, modifiedAt = null),
            metadata = NoteMetadata.NONE
        )
    }
}
