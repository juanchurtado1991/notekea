package app.notekea.notes.presentation.switcher

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.presentation.BaseViewModel
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SearchNoteContentUseCase
import app.notekea.notes.domain.SearchNotesUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * Quick switcher palette (`ROADMAP.md` 1.6) for the vault at [vaultPath]: `Mod+P` finds notes by name and alias, at
 * once; `Mod+Shift+F` searches their content, waiting for a short pause in typing. Every new search cancels the one
 * before, so a slow search never shows results for text that was already changed. The notes come from the vault tree
 * ([QuickSwitcherIntent.OnNotesChange]), so the palette never scans the vault itself. Choosing a result closes the
 * palette and asks the workspace to open the note.
 */
class QuickSwitcherViewModel(
    private val vaultPath: String,
    private val searchNotes: SearchNotesUseCase,
    private val searchNoteContent: SearchNoteContentUseCase,
    private val dispatchers: AppDispatchers
) : BaseViewModel<QuickSwitcherState, QuickSwitcherIntent, QuickSwitcherEffect>(initialState = QuickSwitcherState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var notes: Collection<NoteEntry> = emptyList()
    private var searchJob: Job? = null

    override fun onIntent(
        intent: QuickSwitcherIntent
    ) {
        when (intent) {
            QuickSwitcherIntent.OnClose -> dismiss()
            QuickSwitcherIntent.OnConfirm -> confirm()
            QuickSwitcherIntent.OnModeSwitch -> switchMode()
            is QuickSwitcherIntent.OnNotesChange -> changeNotes(notes = intent.notes)
            is QuickSwitcherIntent.OnOpen -> openPalette(mode = intent.mode, query = intent.query)
            is QuickSwitcherIntent.OnQueryChange -> changeQuery(query = intent.query)
            is QuickSwitcherIntent.OnRowClick -> clickRow(path = intent.path)
            QuickSwitcherIntent.OnSelectNext -> reduce { reduceSelectionMove(state = it, step = 1) }
            QuickSwitcherIntent.OnSelectPrevious -> reduce { reduceSelectionMove(state = it, step = -1) }
        }
    }

    private fun openPalette(
        mode: QuickSwitcherMode,
        query: String
    ) {
        reduce { reduceOpened(state = it, mode = mode, query = query) }
        search(debounce = false)
    }

    private fun dismiss() {
        close()
        emit(effect = QuickSwitcherEffect.Dismissed)
    }

    private fun close() {
        searchJob?.cancel()
        reduce { QuickSwitcherState() }
    }

    private fun confirm() {
        state.value.rows.getOrNull(index = state.value.selectedIndex)?.let { open(row = it) }
    }

    private fun clickRow(
        path: RelativePath
    ) {
        state.value.rows.firstOrNull { it.path == path }?.let { open(row = it) }
    }

    private fun open(
        row: QuickSwitcherRow
    ) {
        close()
        emit(effect = QuickSwitcherEffect.OpenNote(path = row.path))
    }

    private fun changeQuery(
        query: String
    ) {
        if (query == state.value.query) return
        reduce { it.copy(query = query, selectedIndex = 0) }
        search(debounce = true)
    }

    private fun switchMode() {
        val next = if (state.value.mode ==
            QuickSwitcherMode.NOTES
        ) {
            QuickSwitcherMode.CONTENT
        } else {
            QuickSwitcherMode.NOTES
        }
        reduce { it.copy(mode = next, selectedIndex = 0) }
        search(debounce = false)
    }

    private fun changeNotes(
        notes: Collection<NoteEntry>
    ) {
        this.notes = notes
        if (state.value.isOpen) search(debounce = false)
    }

    /** Runs the search for the current text and mode, replacing any search still running. */
    private fun search(
        debounce: Boolean
    ) {
        searchJob?.cancel()
        val (mode, query) = state.value.let { it.mode to it.query }
        val notes = notes
        searchJob = launchGuarded(onError = ::showUnexpectedError) {
            val rows = when (mode) {
                QuickSwitcherMode.NOTES -> withContext(context = dispatchers.default) {
                    searchNotes(notes = notes, query = query).map(transform = ::noteRowOf)
                }

                QuickSwitcherMode.CONTENT -> {
                    reduce { it.copy(isSearching = query.isNotBlank()) }
                    if (debounce) delay(duration = CONTENT_DEBOUNCE)
                    searchNoteContent(
                        vaultPath = vaultPath,
                        notes = notes,
                        query = query
                    ).map(transform = ::contentRowOf)
                }
            }
            reduce { it.copy(rows = rows, selectedIndex = 0, isSearching = false) }
        }
    }

    private fun showUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
        reduce { it.copy(isSearching = false, hasUnexpectedError = true) }
    }

    companion object {
        /** Content search waits for this pause in typing, so it does not run for every keystroke. */
        private val CONTENT_DEBOUNCE = 150.milliseconds
        private const val LOG_TAG = "QuickSwitcherViewModel"
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the quick switcher"
    }
}
