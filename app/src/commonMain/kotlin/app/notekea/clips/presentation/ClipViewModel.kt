package app.notekea.clips.presentation

import app.notekea.clips.domain.ClipStep
import app.notekea.clips.domain.ClipWebPageUseCase
import app.notekea.clips.domain.WebClip
import app.notekea.clips.domain.WebClipNote
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlin.time.Clock

/**
 * "Clip web page…" (4.4) for the vault at [vaultPath]: the page the user typed is clipped ([ClipWebPageUseCase]) and
 * written to `Web sources/` under a name no other note has, then opened; a failure writes nothing.
 */
class ClipViewModel(
    private val vaultPath: String,
    private val clipWebPage: ClipWebPageUseCase,
    private val noteContentStore: NoteContentStore,
    private val noteFileOperations: NoteFileOperations,
    private val localCalendar: LocalCalendar,
    private val clock: Clock
) : BaseViewModel<ClipState, ClipIntent, ClipEffect>(initialState = ClipState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var clipping: Job? = null

    override fun handleIntent(intent: ClipIntent) {
        when (intent) {
            ClipIntent.OnOpen -> reduce { ClipState(isOpen = true) }

            is ClipIntent.OnUrlChange -> reduce {
                it.copy(
                    url = intent.url,
                    error = null
                )
            }

            ClipIntent.OnClip -> clip()

            ClipIntent.OnDismiss -> {
                clipping?.cancel()
                reduce { ClipState() }
            }
        }
    }

    private fun clip() {
        val url = state
            .value
            .url
            .trim()

        if (!state.value.canClip) return

        clipping = launchGuarded(onError = ::logError) {
            clipWebPage(url = url).collect { step ->
                reduce {
                    it.copy(
                        step = step,
                        error = (step as? ClipStep.Failed)?.error
                    )
                }

                if (step is ClipStep.Done) written(clip = step.clip)
            }
        }
    }

    private suspend fun written(clip: WebClip) {
        noteFileOperations.createFolder(
            vaultPath = vaultPath,
            path = WebClipNote.FOLDER
        )

        val path = freePath(wanted = WebClipNote.pathOf(title = clip.title))

        val created = noteFileOperations.createFile(
            vaultPath = vaultPath,
            path = path
        )

        if (created is Outcome.Failure) return reduce { it.copy(step = null) }

        noteContentStore.write(
            vaultPath = vaultPath,
            path = path,
            content = WebClipNote.textOf(clip, today())
        )

        reduce { ClipState() }
        emit(effect = ClipEffect.Opened(path = path))
    }

    // "Title.md", or "Title 2.md" and on when a note of that name is there already.
    private suspend fun freePath(wanted: RelativePath): RelativePath {
        var number = 1
        var path = wanted

        while (noteFileOperations.exists(vaultPath = vaultPath, path = path)) {
            number++
            path = WebClipNote.FOLDER.child(name = "${wanted.nameWithoutExtension} $number${RelativePath.MARKDOWN_EXTENSION}")
        }

        return path
    }

    private fun today(): String {
        val date = localCalendar.momentOf(instant = clock.now()).date

        return "${date.year}-${date.month.toString().padStart(2, '0')}-${date.day.toString().padStart(2, '0')}"
    }

    private fun logError(throwable: Throwable) {
        logger.w(throwable = throwable) { FAILED_MESSAGE }
        reduce { it.copy(step = null) }
    }

    private companion object {
        const val LOG_TAG = "ClipViewModel"
        const val FAILED_MESSAGE = "The page could not be clipped"
    }
}
