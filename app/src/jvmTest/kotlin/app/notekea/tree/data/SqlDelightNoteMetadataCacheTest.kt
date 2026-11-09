package app.notekea.tree.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.db.NotekeaDatabase
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteTask
import app.notekea.core.notes.domain.NoteProperty
import app.notekea.core.notes.domain.PropertyValue
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
    fun `a vault cached before tasks is read again from its notes`() = runTest(context = testDispatcher) {
        val old = JdbcSqliteDriver(url = JdbcSqliteDriver.IN_MEMORY)
        NotekeaDatabase.Schema.create(old)
        old.execute(null, "DROP TABLE note_cache_task", 0)
        old.execute(null, "INSERT INTO note_cache (vault, path) VALUES ('$NOTES', 'Old.md')", 0)
        NotekeaDatabase.Schema.migrate(old, oldVersion = 5, newVersion = NotekeaDatabase.Schema.version)

        val cached = SqlDelightNoteMetadataCache(
            database = NotekeaDatabase(driver = old),
            dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
        ).read(vaultPath = NOTES)

        assertEquals(expected = emptyList(), actual = cached)
    }

    @Test
    fun `a note's tasks come back as they were, and go with it`() = runTest(context = testDispatcher) {
        val task = NoteTask(
            stateIndex = 3,
            endIndex = 20,
            text = "Book the flights",
            isDone = false,
            heading = "Bookings",
            date = CalendarDate(year = 2026, month = 11, day = 20)
        )
        val planned = AREPAS.copy(metadata = AREPAS.metadata.copy(tasks = listOf(task, task.copy(stateIndex = 25))))

        cache.update(vaultPath = NOTES, upserted = listOf(planned), removed = emptyList())
        val kept = cache.read(vaultPath = NOTES).single().metadata.tasks
        cache.update(vaultPath = NOTES, upserted = emptyList(), removed = listOf(planned.file.path))
        cache.update(vaultPath = NOTES, upserted = listOf(AREPAS), removed = emptyList())

        assertEquals(expected = planned.metadata.tasks, actual = kept)
        assertEquals(expected = emptyList(), actual = cache.read(vaultPath = NOTES).single().metadata.tasks)
    }

    @Test
    fun `a changed note replaces its tags and a removed one is forgotten`() = runTest(context = testDispatcher) {
        cache.update(vaultPath = NOTES, upserted = listOf(AREPAS, LISBON), removed = emptyList())
        val retagged = AREPAS.copy(
            metadata = AREPAS.metadata.copy(tags = listOf("dinner"), aliases = emptyList(), textTags = listOf("lunch"))
        )

        cache.update(vaultPath = NOTES, upserted = listOf(retagged), removed = listOf(LISBON.file.path))

        assertEquals(
            expected = listOf(retagged),
            actual = cache.read(vaultPath = NOTES)
        )
    }

    @Test
    fun `a bug is not taken for a broken database`() = runTest(context = testDispatcher) {
        val buggy = object : SqlDriver by driver {
            override fun <R> executeQuery(
                identifier: Int?,
                sql: String,
                mapper: (SqlCursor) -> QueryResult<R>,
                parameters: Int,
                binders: (SqlPreparedStatement.() -> Unit)?
            ): QueryResult<R> = error("a bug")
        }
        val cache = SqlDelightNoteMetadataCache(
            database = NotekeaDatabase(driver = buggy),
            dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
        )

        assertFailsWith<IllegalStateException> { cache.read(vaultPath = NOTES) }
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
                title = "Arepas",
                textTags = listOf("weekend", "recipe/drinks"),
                properties = listOf(
                    NoteProperty(key = "author", value = PropertyValue.Text(text = "Ana")),
                    NoteProperty(key = "rating", value = PropertyValue.Number(text = "4.5")),
                    NoteProperty(key = "finished", value = PropertyValue.Checkbox(isChecked = true)),
                    NoteProperty(key = "read", value = PropertyValue.Date(date = CalendarDate(2026, 3, 2))),
                    NoteProperty(key = "tags", value = PropertyValue.Items(items = listOf("recipe", "venezuelan"))),
                    NoteProperty(key = "none", value = PropertyValue.Items(items = emptyList())),
                    NoteProperty(key = "nested", value = PropertyValue.Other(source = "nested:\n  a: 1"))
                )
            )
        )
        private val LISBON = NoteEntry(
            file = NoteFile(path = RelativePath(value = "Travel/Lisbon trip.md"), size = null, modifiedAt = null),
            metadata = NoteMetadata.NONE
        )
    }
}
