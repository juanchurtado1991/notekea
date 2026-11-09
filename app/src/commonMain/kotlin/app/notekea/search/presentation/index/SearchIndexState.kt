package app.notekea.search.presentation.index

import app.notekea.core.domain.time.ElapsedTime
import app.notekea.core.notes.domain.IndexRunError
import app.notekea.search.domain.IndexProgress
import kotlin.time.Duration

/**
 * The vault's search index as the status bar and its details show it: the current run's [progress] while [isRunning]
 * and the [remaining] time it estimates; after a run, the [readyNotes] the index holds, when the last run that changed
 * something ended ([lastRun]) and how long it [took]; [isFirstRun] while the index starts empty.
 */
data class SearchIndexState(
    val isRunning: Boolean = false,
    val progress: IndexProgress? = null,
    val remaining: Duration? = null,
    val readyNotes: Int? = null,
    val lastRun: ElapsedTime? = null,
    val took: Duration? = null,
    val error: IndexRunError? = null,
    val isFirstRun: Boolean = false,
    val isNoticeDismissed: Boolean = false,
    val isDetailsOpen: Boolean = false
) {
    /** Whether the first run's notice shows: a first run, large enough to take a while, not dismissed. */
    val showsNotice: Boolean
        get() =
            isRunning &&
                isFirstRun &&
                !isNoticeDismissed &&
                (progress?.total ?: 0) >= NOTICE_MIN_NOTES

    /** How far the run is, from 0 to 1. */
    val fraction: Float
        get() = progress?.let {
            if (it.total == 0) 1f else it.done.toFloat() / it.total
        } ?: 0f

    companion object {
        /** A first run of fewer notes ends before a notice could be read. */
        const val NOTICE_MIN_NOTES = 100
    }
}
