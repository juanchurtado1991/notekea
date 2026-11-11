package app.notekea.core.presentation

private const val BYTES_PER_MB = 1_000_000L
private const val BYTES_PER_TENTH_GB = 100_000_000L
private const val TENTHS = 10

/** [bytes] in gigabytes of 10⁹ with one decimal ("2.7 GB"), or megabytes under a tenth of a gigabyte. */
fun formatBytes(bytes: Long): String {
    if (bytes < BYTES_PER_TENTH_GB) return "${(bytes + BYTES_PER_MB / 2) / BYTES_PER_MB} MB"
    val tenths = (bytes + BYTES_PER_TENTH_GB / 2) / BYTES_PER_TENTH_GB

    return "${tenths / TENTHS}.${tenths % TENTHS} GB"
}
