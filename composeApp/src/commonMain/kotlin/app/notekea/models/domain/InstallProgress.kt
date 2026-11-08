package app.notekea.models.domain

/** Where the installation of a chat model and its reranker stands. */
sealed interface InstallProgress {
    /** [doneBytes] of [totalBytes] are on the disk, counting both files. */
    data class Downloading(
        val doneBytes: Long,
        val totalBytes: Long
    ) : InstallProgress

    /** A file is downloaded and its SHA-256 is being checked. */
    data object Verifying : InstallProgress

    data object Installed : InstallProgress

    data class Failed(
        val error: ModelError
    ) : InstallProgress
}
