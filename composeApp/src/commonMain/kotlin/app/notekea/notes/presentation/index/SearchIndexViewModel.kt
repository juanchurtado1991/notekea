package app.notekea.notes.presentation.index

import app.notekea.core.domain.time.DescribeElapsedTimeUseCase
import app.notekea.core.presentation.BaseViewModel
import app.notekea.notes.domain.IndexProgress
import app.notekea.notes.domain.IndexRunError
import app.notekea.notes.domain.IndexStateStore
import app.notekea.notes.domain.IndexVaultUseCase
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.VectorIndex
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

/**
 * Keeps the search index of the vault at [vaultPath] up to date while it is open (`ROADMAP.md` 2.3): every change of
 * its notes starts a run ([IndexVaultUseCase]) a moment later, the run before it stopped (it resumes where it was), so
 * an edited note is in the index within seconds. An index another Notekea holds is tried again later. Rebuild index
 * forgets it and reads every note again. Closing the vault stops the run with this ViewModel.
 */
class SearchIndexViewModel(
    private val vaultPath: String,
    private val indexVault: IndexVaultUseCase,
    private val vectorIndex: VectorIndex,
    private val indexStateStore: IndexStateStore,
    private val describeElapsedTime: DescribeElapsedTimeUseCase,
    private val clock: Clock
) : BaseViewModel<SearchIndexState, SearchIndexIntent, Nothing>(initialState = SearchIndexState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var notes: List<NoteFile>? = null
    private var run: Job? = null
    private var lastRunAt: Instant? = null

    override fun handleIntent(
        intent: SearchIndexIntent
    ) {
        when (intent) {
            is SearchIndexIntent.OnNotesChange -> if (intent.isLoaded) {
                notes = intent.notes
                start(after = CHANGE_DELAY)
            }

            SearchIndexIntent.OnDetailsToggle -> toggleDetails()

            SearchIndexIntent.OnDetailsDismiss -> reduce { it.copy(isDetailsOpen = false) }

            SearchIndexIntent.OnRebuildClick -> rebuild()

            SearchIndexIntent.OnNoticeDismiss -> reduce { it.copy(isNoticeDismissed = true) }
        }
    }

    /** Opens or closes the details, telling how long ago the last run was when they open. */
    private fun toggleDetails() {
        val lastRun = lastRunAt?.let { describeElapsedTime(since = it) }
        reduce { it.copy(isDetailsOpen = !it.isDetailsOpen, lastRun = lastRun) }
    }

    /** Stops the current run and starts another [after] a moment, so a burst of changes starts one run. */
    private fun start(
        after: Duration
    ) {
        val current = notes ?: return
        run?.cancel()
        run = launchGuarded(onError = ::logUnexpectedError) {
            delay(duration = after)
            runOnce(notes = current)
        }
    }

    private suspend fun runOnce(
        notes: List<NoteFile>
    ) {
        val isFirstRun = indexStateStore.read(vaultPath = vaultPath).isEmpty()
        val startedAt = clock.now()
        reduce { it.copy(isRunning = true, isFirstRun = isFirstRun, error = null) }
        var last: IndexProgress? = null
        var doneBefore: Int? = null
        indexVault(vaultPath = vaultPath, notes = notes).collect { progress ->
            last = progress
            val before = doneBefore ?: progress.done.also { doneBefore = it }
            val remaining = remaining(progress = progress, doneBefore = before, since = startedAt)
            reduce { it.copy(progress = progress, remaining = remaining) }
        }
        finish(startedAt = startedAt, notes = notes.size, progress = last)
    }

    private fun finish(
        startedAt: Instant,
        notes: Int,
        progress: IndexProgress?
    ) {
        val now = clock.now()
        val changedSomething = (progress?.total ?: 0) > 0
        if (changedSomething) lastRunAt = now
        reduce {
            it.copy(
                isRunning = false,
                remaining = null,
                error = progress?.error,
                readyNotes = notes - progress?.skipped.orEmpty().size,
                took = if (changedSomething) now - startedAt else it.took
            )
        }
        if (progress?.error == IndexRunError.INDEX) start(after = RETRY_DELAY)
    }

    /**
     * About how long the run has left, at the pace of the notes it has done so far (those [doneBefore] it were
     * indexed by an earlier run); null before its first one.
     */
    private fun remaining(
        progress: IndexProgress,
        doneBefore: Int,
        since: Instant
    ): Duration? {
        val doneNow = progress.done - doneBefore
        if (doneNow <= 0 || progress.done >= progress.total) return null
        return (clock.now() - since) / doneNow * (progress.total - progress.done)
    }

    private fun rebuild() {
        run?.cancel()
        run = launchGuarded(onError = ::logUnexpectedError) {
            vectorIndex.clear(vaultPath = vaultPath)
            indexStateStore.clear(vaultPath = vaultPath)
            notes?.let { runOnce(notes = it) }
        }
        reduce { it.copy(isDetailsOpen = false, isNoticeDismissed = false) }
    }

    private fun logUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
        reduce { it.copy(isRunning = false, error = IndexRunError.INDEX) }
    }

    private companion object {
        const val LOG_TAG = "SearchIndexViewModel"
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error indexing the vault"

        // A save writes the note, then the tree reports it: one run for the burst, well within 2 s.
        val CHANGE_DELAY = 300.milliseconds
        val RETRY_DELAY = 30_000.milliseconds
    }
}
