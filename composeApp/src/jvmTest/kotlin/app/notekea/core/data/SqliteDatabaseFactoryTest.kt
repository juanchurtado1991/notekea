package app.notekea.core.data

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlin.io.path.ExperimentalPathApi::class)
class SqliteDatabaseFactoryTest {
    private val appData: Path = Files.createTempDirectory(TEMP_PREFIX).resolve(APP_DATA_FOLDER)

    @AfterTest
    fun deleteAppData() {
        appData.parent.deleteRecursively()
    }

    @Test
    fun `the database is created in the app data folder and keeps its rows`() {
        SqliteDatabaseFactory(appDataDirectory = appData.toString()).create().noteCacheQueries.upsertNote(
            vault = VAULT,
            path = NOTE,
            size = null,
            modified_at = null,
            title = TITLE
        )

        val reopened = SqliteDatabaseFactory(appDataDirectory = appData.toString()).create()

        assertTrue(actual = appData.resolve(SqliteDatabaseFactory.DATABASE_FILE_NAME).exists())
        assertEquals(
            expected = listOf(TITLE),
            actual = reopened.noteCacheQueries
                .selectNotes(vault = VAULT)
                .executeAsList()
                .map { it.title }
        )
    }

    @Test
    fun `a file that is not a database is replaced by an empty one`() {
        Files.createDirectories(appData)
        appData.resolve(SqliteDatabaseFactory.DATABASE_FILE_NAME).writeText(text = GARBAGE)

        val database = SqliteDatabaseFactory(appDataDirectory = appData.toString()).create()

        assertEquals(
            expected = emptyList(),
            actual = database.noteCacheQueries.selectNotes(vault = VAULT).executeAsList()
        )
    }

    companion object {
        private const val APP_DATA_FOLDER = "notekea"
        private const val GARBAGE = "this is not a SQLite database, it is a note someone saved here by mistake"
        private const val NOTE = "Welcome.md"
        private const val TEMP_PREFIX = "notekea-database"
        private const val TITLE = "Welcome"
        private const val VAULT = "/home/ana/Notes"
    }
}
