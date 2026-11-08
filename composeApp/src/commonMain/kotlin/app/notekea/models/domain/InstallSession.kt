package app.notekea.models.domain

/** The model being installed, one at a time: its [size], its [progress], and whether the person paused it. */
data class InstallSession(
    val size: ModelSize,
    val progress: InstallProgress,
    val isPaused: Boolean = false
)
