package app.notekea.core.crash.domain

/** A crash report: its [text], shown or copied as it is, and the [path] of its file, null if it could not be kept. */
data class RecordedCrash(
    val text: String,
    val path: String?
)
