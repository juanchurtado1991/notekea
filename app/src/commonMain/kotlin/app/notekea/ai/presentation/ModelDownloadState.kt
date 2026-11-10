package app.notekea.ai.presentation

import app.notekea.ai.domain.InstallProgress
import app.notekea.ai.domain.InstallSession

/** The installation of a chat model as the status bar shows it: the [session] under way, paused or failed, if any. */
data class ModelDownloadState(val session: InstallSession? = null) {
    /** The part downloaded, from 0 to 1, while the files arrive. */
    val fraction: Float?
        get() {
            val downloading = session?.progress as? InstallProgress.Downloading ?: return null
            if (downloading.totalBytes <= 0) return 0f

            return (downloading.doneBytes.toFloat() / downloading.totalBytes).coerceIn(0f, 1f)
        }
}
