package app.notekea.core.logging.data

/** Where the app's log is kept in the app data directory, and how many rotated files stay. */
object LogFiles {
    const val FOLDER = "logs"

    /** The current file and two older ones: a few days of use, never more than about [MAX_BYTES] × [KEPT]. */
    const val KEPT = 3

    /** A file is rotated once it reaches a megabyte. */
    const val MAX_BYTES = 1_000_000L

    /** `notekea.log` for [index] 0, the current file; `notekea.<index>.log` for the older ones. */
    fun name(
        index: Int
    ): String = if (index == 0) "notekea.log" else "notekea.$index.log"
}
