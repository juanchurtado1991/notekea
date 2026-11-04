package app.notekea.notes.presentation.mentions

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.presentation.BaseViewModel
import app.notekea.notes.domain.FindMentionsUseCase
import app.notekea.notes.domain.LinkMentionUseCase
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.NoteMention
import app.notekea.notes.domain.NoteMentions
import app.notekea.notes.domain.NoteTextCache
import app.notekea.notes.domain.RelativePath
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * The notes that name the open one, in the vault at [vaultPath] (`ROADMAP.md` 2.5): found ([FindMentionsUseCase]) a
 * moment after another note opens and after the vault's notes change (the file watcher, an edit saved, a Link made),
 * from texts kept in [noteTextCache] so only changed notes are read again; a search under way is dropped for the newer
 * one, and a failed one says so and can be tried again. It needs nothing from the search index. Link turns an unlinked mention into a link ([LinkMentionUseCase]),
 * only on that click.
 */
class NoteMentionsViewModel(
    private val vaultPath: String,
    private val findMentions: FindMentionsUseCase,
    private val linkMention: LinkMentionUseCase,
    private val noteTextCache: NoteTextCache,
    private val dispatchers: AppDispatchers
) : BaseViewModel<NoteMentionsState, NoteMentionsIntent, Nothing>(initialState = NoteMentionsState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var notes: Map<RelativePath, NoteEntry> = emptyMap()
    private var search: Job? = null

    override fun handleIntent(
        intent: NoteMentionsIntent
    ) {
        when (intent) {
            is NoteMentionsIntent.OnNoteChange -> {
                if (intent.note == state.value.note) return
                reduce { NoteMentionsState(note = intent.note, isLoading = intent.note != null) }
                find(after = NOTE_DELAY)
            }

            is NoteMentionsIntent.OnNotesChange -> {
                notes = intent.notes
                find(after = VAULT_DELAY)
            }

            is NoteMentionsIntent.OnLink -> link(mention = intent.mention)

            NoteMentionsIntent.OnRetry -> {
                reduce { it.copy(isLoading = true, searchFailed = false) }
                find(after = Duration.ZERO)
            }
        }
    }

    /** Drops the search under way and starts another [after] a moment, on the vault as it is then. */
    private fun find(
        after: Duration
    ) {
        search?.cancel()
        val note = state.value.note ?: return
        search = launchGuarded(onError = ::onSearchFailed) {
            delay(duration = after)
            val entry = notes[note]
            val mentions = if (entry == null) {
                NoteMentions.NONE
            } else {
                val texts = noteTextCache.textsOf(vaultPath = vaultPath, notes = notes.values)
                withContext(context = dispatchers.default) {
                    findMentions(note = entry, notes = notes.keys, texts = texts)
                }
            }
            reduce { it.copy(mentions = mentions, isLoading = false, searchFailed = false) }
        }
    }

    private fun link(
        mention: NoteMention
    ) {
        val entry = state.value.note?.let { notes[it] } ?: return
        launchGuarded(onError = ::logUnexpectedError) {
            val isLinked = linkMention(vaultPath = vaultPath, mention = mention, note = entry, notes = notes.keys)
            reduce { it.copy(linkFailed = if (isLinked) null else mention.source) }
            find(after = Duration.ZERO)
        }
    }

    private fun onSearchFailed(
        throwable: Throwable
    ) {
        logUnexpectedError(throwable = throwable)
        reduce { it.copy(isLoading = false, searchFailed = true) }
    }

    private fun logUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
    }

    private companion object {
        const val LOG_TAG = "NoteMentionsViewModel"
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error finding the notes that name the open one"

        // Another note: soon, so the tabs follow it. The vault: after a burst of file events settles.
        val NOTE_DELAY = 200.milliseconds
        val VAULT_DELAY = 300.milliseconds
    }
}
