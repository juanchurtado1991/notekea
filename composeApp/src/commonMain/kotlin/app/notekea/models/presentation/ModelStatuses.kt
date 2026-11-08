package app.notekea.models.presentation

import app.notekea.models.domain.InstallProgress
import app.notekea.models.domain.ModelSize

/** Where [size] stands: its installation first, then whether it is on the disk and in use. */
internal fun ModelManagerState.statusOf(
    size: ModelSize
): ModelStatus {
    val session = session
    val row = models.firstOrNull { it.size == size }
    return when {
        session?.size == size -> when (val progress = session.progress) {
            is InstallProgress.Failed -> ModelStatus.Failed(error = progress.error)
            else -> ModelStatus.Installing(progress = progress, isPaused = session.isPaused)
        }

        row?.isInstalled == true && active == size -> ModelStatus.InUse

        row?.isInstalled == true -> ModelStatus.Installed

        session != null && session.progress !is InstallProgress.Failed -> ModelStatus.Waiting

        else -> ModelStatus.NotInstalled
    }
}

/** [bytes] in gigabytes of 10⁹ with one decimal ("2.7 GB"), or megabytes under a tenth of a gigabyte. */
internal fun formatBytes(
    bytes: Long
): String {
    if (bytes < BYTES_PER_TENTH_GB) return "${(bytes + BYTES_PER_MB / 2) / BYTES_PER_MB} MB"
    val tenths = (bytes + BYTES_PER_TENTH_GB / 2) / BYTES_PER_TENTH_GB
    return "${tenths / TENTHS}.${tenths % TENTHS} GB"
}

private const val BYTES_PER_MB = 1_000_000L
private const val BYTES_PER_TENTH_GB = 100_000_000L
private const val TENTHS = 10
