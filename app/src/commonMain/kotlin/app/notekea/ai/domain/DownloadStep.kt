package app.notekea.ai.domain

import app.notekea.core.domain.Outcome

/** One step of [ModelDownloader.download]. */
sealed interface DownloadStep {
    data class Received(val bytesOnDisk: Long) : DownloadStep

    data class Finished(val outcome: Outcome<Unit, ModelError>) : DownloadStep
}
