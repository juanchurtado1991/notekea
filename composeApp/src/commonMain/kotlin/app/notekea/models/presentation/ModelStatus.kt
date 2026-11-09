package app.notekea.models.presentation

import app.notekea.models.domain.InstallProgress
import app.notekea.models.domain.ModelError

/** Where one size stands, as its card or row shows it. */
sealed interface ModelStatus {
    data object NotInstalled : ModelStatus

    /** Another size is installing, so this one cannot start. */
    data object Waiting : ModelStatus

    data class Installing(
        val progress: InstallProgress,
        val isPaused: Boolean
    ) : ModelStatus

    data class Failed(
        val error: ModelError
    ) : ModelStatus

    data object Installed : ModelStatus

    data object InUse : ModelStatus
}
