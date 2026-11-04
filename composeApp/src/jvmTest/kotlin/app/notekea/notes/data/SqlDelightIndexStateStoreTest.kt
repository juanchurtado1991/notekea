package app.notekea.notes.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.db.NotekeaDatabase
import app.notekea.notes.domain.IndexedNote
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SkipReason
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

/** What the search index holds of each note, in the app's database (`ROADMAP.md` 2.3), per vault. */
class SqlDelightIndexStateStoreTest {
    private val testDispatcher = StandardTestDispatcher()
    private val driver = JdbcSqliteDriver(url = JdbcSqliteDriver.IN_MEMORY).also { NotekeaDatabase.Schema.create(it) }
    private val store = SqlDelightIndexStateStore(
        database = NotekeaDatabase(driver = driver),
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

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
        val store = SqlDelightIndexStateStore(
            database = NotekeaDatabase(driver = buggy),
            dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
        )

        assertFailsWith<IllegalStateException> { store.read(vaultPath = NOTES) }
    }

    @Test
    fun `a saved note comes back as saved, replaced by a later save, per vault`() = runTest(context = testDispatcher) {
        store.save(vaultPath = NOTES, note = AREPAS)
        store.save(vaultPath = NOTES, note = AREPAS.copy(contentHash = "new"))
        store.save(vaultPath = WORK, note = UNKNOWN)

        assertEquals(
            expected = listOf(listOf(AREPAS.copy(contentHash = "new")), listOf(UNKNOWN)),
            actual = listOf(store.read(vaultPath = NOTES), store.read(vaultPath = WORK))
        )
    }

    @Test
    fun `a note left out comes back with its reason`() = runTest(context = testDispatcher) {
        val leftOut = AREPAS.copy(skipReason = SkipReason.TOO_LONG)

        store.save(vaultPath = NOTES, note = leftOut)

        assertEquals(expected = listOf(leftOut), actual = store.read(vaultPath = NOTES))
    }

    @Test
    fun `a deleted note is forgotten, and clearing forgets the vault's notes only`() = runTest(
        context = testDispatcher
    ) {
        store.save(vaultPath = NOTES, note = AREPAS)
        store.save(vaultPath = NOTES, note = UNKNOWN)
        store.save(vaultPath = WORK, note = AREPAS)
        store.delete(vaultPath = NOTES, path = AREPAS.file.path)
        val afterDelete = store.read(vaultPath = NOTES)
        store.clear(vaultPath = NOTES)

        assertEquals(
            expected = listOf(listOf(UNKNOWN), emptyList(), listOf(AREPAS)),
            actual = listOf(afterDelete, store.read(vaultPath = NOTES), store.read(vaultPath = WORK))
        )
    }

    private companion object {
        const val NOTES = "/home/ana/Notes"
        const val WORK = "/home/ana/Work"
        val AREPAS = IndexedNote(
            file = NoteFile(
                path = RelativePath(value = "Recipes/Arepas.md"),
                size = 120,
                modifiedAt = Instant.fromEpochMilliseconds(epochMilliseconds = 1_762_164_000_000)
            ),
            contentHash = "abc",
            version = "v1"
        )
        val UNKNOWN = IndexedNote(
            file = NoteFile(path = RelativePath(value = "Unknown.md"), size = null, modifiedAt = null),
            contentHash = "def",
            version = "v1"
        )
    }
}
