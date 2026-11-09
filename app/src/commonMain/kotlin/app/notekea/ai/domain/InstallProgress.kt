package app.notekea.ai.domain

/** Where the installation of a chat model and its reranker stands. */
sealed interface InstallProgress {
    /**
     * [doneBytes] of [totalBytes] are on the disk, counting both files, arriving at [bytesPerSecond] (null until a
     * second of the download has been measured).
     */
    data class Downloading(
        val doneBytes: Long,
        val totalBytes: Long,
        val bytesPerSecond: Long? = null
    ) : InstallProgress

    /** A file is downloaded and its SHA-256 is being checked. */
    data object Verifying : InstallProgress

    data object Installed : InstallProgress

    data class Failed(val error: ModelError) : InstallProgress
}
