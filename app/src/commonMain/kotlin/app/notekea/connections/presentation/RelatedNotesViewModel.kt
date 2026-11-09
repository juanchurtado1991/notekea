package app.notekea.connections.presentation

import app.notekea.core.domain.Outcome
import app.notekea.core.presentation.BaseViewModel
import app.notekea.core.notes.domain.IndexRunError
import app.notekea.core.notes.domain.FindRelatedNotesUseCase
import app.notekea.core.notes.domain.RelativePath
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * The Related section of the vault at [vaultPath] (`ROADMAP.md` 2.4): finds the open note's related notes
 * ([FindRelatedNotesUseCase]) a moment after another note opens, after the person stops typing for a while, and when
 * the index ends a run; a search under way is dropped for the newer one. Relations are only shown: nothing is ever
 * written to a note from here.
 */
class RelatedNotesViewModel(
    private val vaultPath: String,
    private val findRelatedNotes: FindRelatedNotesUseCase
) : BaseViewModel<RelatedNotesState, RelatedNotesIntent, Nothing>(initialState = RelatedNotesState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var text = ""
    private var notes: List<RelativePath> = emptyList()
    private var search: Job? = null

    override fun handleIntent(
        intent: RelatedNotesIntent
    ) {
        when (intent) {
            is RelatedNotesIntent.OnNoteChange -> onNoteChange(text = intent.text, note = intent.note)

            is RelatedNotesIntent.OnNotesChange -> notes = intent.notes

            is RelatedNotesIntent.OnIndexChange -> {
                reduce { it.copy(isIndexReady = intent.isReady) }
                if (intent.isReady) find(after = NOTE_DELAY)
            }

            RelatedNotesIntent.OnLinkedToggle -> reduce { it.copy(isLinkedOpen = !it.isLinkedOpen) }
        }
    }

    private fun onNoteChange(
        text: String,
        note: RelativePath?
    ) {
        val isOtherNote = note != state.value.note
        if (!isOtherNote && text == this.text) return
        this.text = text
        if (isOtherNote) {
            reduce { RelatedNotesState(note = note, isIndexReady = it.isIndexReady, isLoading = note != null) }
        }
        if (note == null) search?.cancel() else find(after = if (isOtherNote) NOTE_DELAY else TYPING_DELAY)
    }

    /** Drops the search under way and starts another [after] a moment, on the text as it is then. */
    private fun find(
        after: Duration
    ) {
        val note = state.value.note ?: return
        search?.cancel()
        search = launchGuarded(onError = ::logUnexpectedError) {
            delay(duration = after)
            val outcome = findRelatedNotes(vaultPath = vaultPath, note = note, text = text, notes = notes)
            reduce {
                when (outcome) {
                    is Outcome.Success -> it.copy(
                        related = outcome.value.related,
                        linked = outcome.value.linked,
                        isTooLong = outcome.value.isTooLong,
                        isLoading = false,
                        error = null
                    )

                    is Outcome.Failure -> it.copy(isLoading = false, error = outcome.error)
                }
            }
        }
    }

    private fun logUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
        reduce { it.copy(isLoading = false, error = IndexRunError.Index) }
    }

    private companion object {
        const val LOG_TAG = "RelatedNotesViewModel"
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error finding related notes"

        // Another note: soon, so the section follows the tab. Typing: once the person pauses, not at every key.
        val NOTE_DELAY = 200.milliseconds
        val TYPING_DELAY = 1_500.milliseconds
    }
}
