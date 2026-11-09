package app.notekea.ai.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * Installs one chat model at a time, with its reranker, for as long as the app runs: closing Settings does not stop
 * it. A model that finishes installing becomes the one in use ([ActiveModelStore]).
 */
interface ModelInstaller {
    /** The installation under way or paused, or the last one that failed; null when there is none. */
    val session: StateFlow<InstallSession?>

    /** Starts installing [model], or resumes it from the bytes already downloaded. */
    fun start(model: ChatModel)

    /** Stops the download and keeps its bytes for [start]. */
    fun pause()

    /** Stops the download and deletes what it downloaded: the chat model, and the reranker unless it finished. */
    fun cancel()
}
