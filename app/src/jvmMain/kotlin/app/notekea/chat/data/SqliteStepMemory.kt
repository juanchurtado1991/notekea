package app.notekea.chat.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.notekea.chat.domain.context.StepChunk
import app.notekea.chat.domain.context.StepMemory
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The turn's steps kept in SQLite (`docs/agent-step-memory.md`): a full-text table in memory, never in a file, as it
 * only holds what a turn found while it runs; ranked by BM25 against what the task needs.
 */
class SqliteStepMemory : StepMemory {
    private val lock = Mutex()

    private val driver: SqlDriver by lazy {
        JdbcSqliteDriver(url = JdbcSqliteDriver.IN_MEMORY).also {
            it.execute(
                identifier = null,
                sql = CREATE,
                parameters = 0
            )
        }
    }

    override suspend fun keep(
        space: String,
        entry: Int,
        chunks: List<String>
    ) = lock.withLock {
        chunks.forEachIndexed { index, text ->
            driver
                .execute(
                    identifier = null,
                    sql = INSERT,
                    parameters = 4
                ) {
                    bindString(0, space)
                    bindLong(1, entry.toLong())
                    bindLong(2, index.toLong())
                    bindString(3, text)
                }
        }
    }

    override suspend fun ranked(
        space: String,
        need: String,
        entries: Set<Int>,
        most: Int
    ): List<StepChunk> = lock.withLock {
        val match = matchOf(need = need) ?: return@withLock emptyList()

        val found = driver
            .executeQuery(
                identifier = null,
                sql = RANKED,
                mapper = { cursor ->
                    val rows = mutableListOf<StepChunk>()

                    while (cursor.next().value) {
                        rows += StepChunk(
                            entry = cursor.getLong(0)?.toInt() ?: 0,
                            chunk = cursor.getLong(1)?.toInt() ?: 0
                        )
                    }

                    QueryResult.Value(value = rows)
                },
                parameters = 2
            ) {
                bindString(0, match)
                bindString(1, space)
            }
            .value

        found
            .filter {
                it.entry in entries
            }
            .take(n = most)
    }

    override suspend fun forget(space: String) = lock.withLock {
        driver
            .execute(
                identifier = null,
                sql = DELETE,
                parameters = 1
            ) { bindString(0, space) }

        Unit
    }

    // The need's words, any of them, a long one by its start: "memorizar" also matches "memorizing" and "memorize".
    private fun matchOf(need: String): String? = WORD
        .findAll(input = need.lowercase())
        .map { it.value }
        .filter {
            it.length >= SHORTEST_WORD && it !in COMMON
        }
        .distinct()
        .joinToString(separator = " OR ") { word ->
            if (word.length > LONG_WORD) "\"${word.dropLast(n = 2)}\"*" else "\"$word\""
        }
        .ifEmpty { null }

    private companion object {
        const val SHORTEST_WORD = 3
        const val LONG_WORD = 6

        val WORD = Regex(pattern = "[\\p{L}\\p{N}]+")

        // Words that say what to do with what is found, not what it is about.
        val COMMON = setOf(
            "the", "and", "for", "with", "what", "how", "make", "write", "note", "notes", "find", "look", "about",
            "into", "from", "that", "this", "these", "them", "add", "my", "una", "las", "los", "del", "con", "que",
            "para", "como", "cómo", "hazme", "haz", "nota", "notas", "busca", "sobre", "mis", "tus"
        )

        const val CREATE =
            "CREATE VIRTUAL TABLE step_chunk USING fts5(space UNINDEXED, entry UNINDEXED, chunk UNINDEXED, text, " +
                "tokenize = 'unicode61 remove_diacritics 2')"

        const val INSERT = "INSERT INTO step_chunk (space, entry, chunk, text) VALUES (?, ?, ?, ?)"

        const val RANKED =
            "SELECT entry, chunk FROM step_chunk WHERE step_chunk MATCH ? AND space = ? ORDER BY bm25(step_chunk)"

        const val DELETE = "DELETE FROM step_chunk WHERE space = ?"
    }
}
