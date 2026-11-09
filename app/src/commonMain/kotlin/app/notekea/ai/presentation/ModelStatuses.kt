package app.notekea.ai.presentation

import app.notekea.ai.domain.InstallProgress
import app.notekea.ai.domain.ModelSize

/** Where [size] stands: its installation first, then whether it is on the disk and in use. */
internal fun ModelManagerState.statusOf(size: ModelSize): ModelStatus {
    val session = session

    val row = models.firstOrNull {
        it.size == size
    }

    return when {
        session?.size == size -> when (val progress = session.progress) {
            is InstallProgress.Failed -> ModelStatus.Failed(error = progress.error)

            else -> ModelStatus.Installing(
                progress = progress,
                isPaused = session.isPaused
            )
        }

        row?.isInstalled == true && active == size -> ModelStatus.InUse

        row?.isInstalled == true -> ModelStatus.Installed

        session != null && session.progress !is InstallProgress.Failed -> ModelStatus.Waiting

        else -> ModelStatus.NotInstalled
    }
}

/** The disk space deleting [row] frees: its file, and the reranker's when no other chat model is installed. */
internal fun ModelManagerState.freedBy(row: ModelRow): Long {
    val isLast = models.none {
        it.isInstalled && it.size != row.size
    }

    return row.downloadBytes + if (isLast && isRerankerInstalled) rerankerBytes else 0L
}

/** [bytes] in gigabytes of 10⁹ with one decimal ("2.7 GB"), or megabytes under a tenth of a gigabyte. */
internal fun formatBytes(bytes: Long): String {
    if (bytes < BYTES_PER_TENTH_GB) return "${(bytes + BYTES_PER_MB / 2) / BYTES_PER_MB} MB"
    val tenths = (bytes + BYTES_PER_TENTH_GB / 2) / BYTES_PER_TENTH_GB

    return "${tenths / TENTHS}.${tenths % TENTHS} GB"
}

/** A download speed in the largest unit that keeps it above 1, with one decimal: "24.5 MB/s", "850 KB/s". */
internal fun formatSpeed(bytesPerSecond: Long): String = when {
    bytesPerSecond >= BYTES_PER_GB -> "${oneDecimal(
        value = bytesPerSecond,
        unit = BYTES_PER_GB
    )} GB/s"

    bytesPerSecond >= BYTES_PER_MB -> "${oneDecimal(
        value = bytesPerSecond,
        unit = BYTES_PER_MB
    )} MB/s"

    else -> "${(bytesPerSecond + BYTES_PER_KB / 2) / BYTES_PER_KB} KB/s"
}

private fun oneDecimal(
    value: Long,
    unit: Long
): String {
    val tenths = (value * TENTHS + unit / 2) / unit

    return "${tenths / TENTHS}.${tenths % TENTHS}"
}

private const val BYTES_PER_GB = 1_000_000_000L
private const val BYTES_PER_KB = 1_000L
private const val BYTES_PER_MB = 1_000_000L
private const val BYTES_PER_TENTH_GB = 100_000_000L
private const val TENTHS = 10
