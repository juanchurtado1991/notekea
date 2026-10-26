package app.notekea.core.data

import java.nio.file.Files
import java.sql.DriverManager
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
    fun `a cache from before text tags is migrated and emptied, so every note is read again`() {
        Files.createDirectories(appData)
        DriverManager.getConnection(URL_PREFIX + appData.resolve(SqliteDatabaseFactory.DATABASE_FILE_NAME)).use {
            it.createStatement().use { statement ->
                VERSION_1_SCHEMA.forEach { sql -> statement.executeUpdate(sql) }
            }
        }

        val queries = SqliteDatabaseFactory(appDataDirectory = appData.toString()).create().noteCacheQueries
        queries.insertTextTag(vault = VAULT, path = NOTE, position = 0, tag = TITLE)

        assertEquals(
            expected = emptyList<String>() to listOf(TITLE),
            actual = queries.selectNotes(vault = VAULT).executeAsList().map { it.path } to
                queries.selectTextTags(vault = VAULT).executeAsList().map { it.tag }
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
        private const val URL_PREFIX = "jdbc:sqlite:"
        private val VERSION_1_SCHEMA = listOf(
            "CREATE TABLE note_cache (vault TEXT NOT NULL, path TEXT NOT NULL, size INTEGER, modified_at INTEGER, " +
                "title TEXT, PRIMARY KEY (vault, path))",
            "CREATE TABLE note_cache_tag (vault TEXT NOT NULL, path TEXT NOT NULL, position INTEGER NOT NULL, " +
                "tag TEXT NOT NULL, PRIMARY KEY (vault, path, position))",
            "CREATE INDEX note_cache_tag_by_tag ON note_cache_tag (vault, tag)",
            "CREATE TABLE note_cache_alias (vault TEXT NOT NULL, path TEXT NOT NULL, position INTEGER NOT NULL, " +
                "alias TEXT NOT NULL, PRIMARY KEY (vault, path, position))",
            "INSERT INTO note_cache (vault, path, title) VALUES ('/home/ana/Notes', 'Welcome.md', 'Old')",
            "PRAGMA user_version = 1"
        )
        private const val APP_DATA_FOLDER = "notekea"
        private const val GARBAGE = "this is not a SQLite database, it is a note someone saved here by mistake"
        private const val NOTE = "Welcome.md"
        private const val TEMP_PREFIX = "notekea-database"
        private const val TITLE = "Welcome"
        private const val VAULT = "/home/ana/Notes"
    }
}
