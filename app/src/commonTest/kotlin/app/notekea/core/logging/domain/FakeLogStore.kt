package app.notekea.core.logging.domain

/** [LogStore] holding [lines] in memory, as the log files would, oldest first. */
class FakeLogStore(
    private val lines: List<String> = emptyList()
) : LogStore {
    override suspend fun recentLines(
        count: Int
    ): List<String> = lines.takeLast(n = count)
}
