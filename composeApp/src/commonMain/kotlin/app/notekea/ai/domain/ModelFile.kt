package app.notekea.ai.domain

/**
 * One file to download: [url] points at an exact commit of its repository, so its bytes never change, and [sha256]
 * (lowercase hex) checks them once downloaded. [sizeBytes] is the whole file.
 */
data class ModelFile(
    val fileName: String,
    val url: String,
    val sizeBytes: Long,
    val sha256: String
)
