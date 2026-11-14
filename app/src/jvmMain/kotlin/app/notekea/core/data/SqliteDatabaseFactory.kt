package app.notekea.core.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.notekea.db.NotekeaDatabase
import co.touchlab.kermit.Logger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.sql.SQLException

/**
 * The app's SQLite database, `notekea.db` in the app data directory (`PLATFORM_GUIDELINES.md`), created or migrated on
 * open. It only holds what can be rebuilt from the vaults, so a file that cannot be opened is deleted and created
 * again; if even that fails (a file locked by another process), the app runs on an in-memory database.
 */
class SqliteDatabaseFactory(private val appDataDirectory: String) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    fun create(): NotekeaDatabase = NotekeaDatabase(driver = openDriver())

    private fun open(url: String): SqlDriver = JdbcSqliteDriver(
        url = url,
        schema = NotekeaDatabase.Schema
    )

    private fun openDriver(): SqlDriver {
        val file = Path.of(appDataDirectory, DATABASE_FILE_NAME)

        return try {
            Files.createDirectories(file.parent)
            openOrRecreate(file = file)
        } catch (exception: IOException) {
            logger.w(throwable = exception) { IN_MEMORY_MESSAGE }

            open(url = JdbcSqliteDriver.IN_MEMORY)
        } catch (exception: SQLException) {
            logger.w(throwable = exception) { IN_MEMORY_MESSAGE }

            open(url = JdbcSqliteDriver.IN_MEMORY)
        }
    }

    private fun openOrRecreate(file: Path): SqlDriver = try {
        open(url = URL_PREFIX + file)
    } catch (exception: SQLException) {
        // Only a file that is no database any more is made again: one another Notekea holds open, or a full
        // disk, would otherwise cost the cache every time (the code review of 2026-11-13).
        val isBroken = BROKEN.any {
            exception
                .message
                .orEmpty()
                .contains(
                    other = it,
                    ignoreCase = true
                )
        }

        if (!isBroken) throw exception
        logger.w(throwable = exception) { RECREATE_MESSAGE }
        Files.deleteIfExists(file)
        open(url = URL_PREFIX + file)
    }

    companion object {
        const val DATABASE_FILE_NAME = "notekea.db"
        private val BROKEN =
            listOf("SQLITE_CORRUPT", "SQLITE_NOTADB", "malformed", "not a database")
        private const val IN_MEMORY_MESSAGE = "Database unavailable; running without a cache"
        private const val LOG_TAG = "SqliteDatabaseFactory"
        private const val RECREATE_MESSAGE = "Database unreadable; creating it again"
        private const val URL_PREFIX = "jdbc:sqlite:"
    }
}
