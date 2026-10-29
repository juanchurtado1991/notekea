package app.notekea.core.logging.domain

/** The app's log files, as Copy diagnostics reads them. */
interface LogStore {
    /** The last [count] lines the app logged, oldest first, across the rotated files; none when nothing was logged. */
    suspend fun recentLines(
        count: Int
    ): List<String>
}
