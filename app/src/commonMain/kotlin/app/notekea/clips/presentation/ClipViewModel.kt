package app.notekea.clips.presentation

import app.notekea.clips.domain.ClipSectionUseCase
import app.notekea.clips.domain.ClipStep
import app.notekea.clips.domain.ClipWebPageUseCase
import app.notekea.clips.domain.DescribeSectionUseCase
import app.notekea.clips.domain.FindSectionUseCase
import app.notekea.clips.domain.SectionLinks
import app.notekea.clips.domain.SectionStep
import app.notekea.clips.domain.UpdateStep
import app.notekea.clips.domain.UpdateWebSourcesUseCase
import app.notekea.clips.domain.WebClip
import app.notekea.clips.domain.WebClipNote
import app.notekea.clips.domain.WebClipSection
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
 * "Clip web page…" (4.4) for the vault at [vaultPath]: the page the user typed is downloaded first; when it links pages
 * under its own path the dialog asks whether to clip only it or them too ([ClipSectionUseCase]). The notes are written
 * to `Web sources/` under names no other note has, then opened; a failure writes nothing.
 */
class ClipViewModel(
    private val vaultPath: String,
    private val clipWebPage: ClipWebPageUseCase,
    private val findSection: FindSectionUseCase,
    private val clipSection: ClipSectionUseCase,
    private val describeSection: DescribeSectionUseCase,
    private val updateWebSources: UpdateWebSourcesUseCase,
    private val noteContentStore: NoteContentStore,
    private val noteFileOperations: NoteFileOperations,
    private val localCalendar: LocalCalendar,
    private val clock: Clock
) : BaseViewModel<ClipState, ClipIntent, ClipEffect>(initialState = ClipState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var clipping: Job? = null
    private var updating: Job? = null

    // How deep the section being clipped follows its links, kept in its index for the updates to come.
    private var levels = 1

    override fun handleIntent(intent: ClipIntent) {
        when (intent) {
            ClipIntent.OnOpen -> reduce { ClipState(isOpen = true) }

            is ClipIntent.OnUrlChange -> reduce {
                it.copy(
                    url = intent.url,
                    error = null,
                    section = null
                )
            }

            ClipIntent.OnClip -> ask()

            ClipIntent.OnClipPage -> clipPage()

            ClipIntent.OnClipSection ->
                state
                    .value
                    .section
                    ?.let { clipWhole(links = it) }

            ClipIntent.OnClipAll ->
                state
                    .value
                    .section
                    ?.let { clipWhole(links = it.everyPage()) }

            is ClipIntent.OnClipDeep -> clipDeep(levels = intent.levels)

            is ClipIntent.OnUpdate -> update(
                path = intent.path,
                isShown = intent.isShown
            )

            ClipIntent.OnUpdateDismiss -> {
                updating?.cancel()

                reduce {
                    it.copy(
                        update = null,
                        isUpdateShown = false
                    )
                }
            }

            ClipIntent.OnDismiss -> {
                clipping?.cancel()
                reduce { ClipState() }
            }
        }
    }

    private val url: String get() = state
        .value
        .url
        .trim()

    // The page downloaded to see what it links under its path: asked about when it links some, else clipped alone.
    private fun ask() {
        if (!state.value.canClip) return

        clipping = launchGuarded(onError = ::logError) {
            reduce {
                it.copy(
                    step = ClipStep.Fetching,
                    error = null
                )
            }

            when (val found = findSection(url = url)) {
                is Outcome.Failure -> reduce {
                    it.copy(
                        step = null,
                        error = found.error
                    )
                }

                is Outcome.Success -> {
                    val links = found.value

                    if (links.urls.isEmpty()) {
                        clipPage()
                    } else {
                        val summary = describeSection(links = links)

                        reduce {
                            it.copy(
                                step = null,
                                section = links,
                                sectionSummary = summary
                            )
                        }
                    }
                }
            }
        }
    }

    private fun clipPage() {
        reduce { it.copy(section = null) }

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

    // The section again, the links of its pages followed too, then clipped whole.
    private fun clipDeep(levels: Int) {
        clipping = launchGuarded(onError = ::logError) {
            reduce {
                it.copy(
                    step = ClipStep.Fetching,
                    section = null
                )
            }

            val found = findSection(
                url = url,
                depth = levels
            )

            reduce { it.copy(step = null) }
            this.levels = levels

            when (found) {
                is Outcome.Success -> clipWhole(links = found.value)
                is Outcome.Failure -> reduce { it.copy(error = found.error) }
            }
        }
    }

    // One update at a time; the automatic one never takes the place of one the person asked for.
    private fun update(
        path: RelativePath,
        isShown: Boolean
    ) {
        if (updating?.isActive == true) return

        reduce {
            it.copy(
                update = null,
                isUpdateShown = isShown
            )
        }

        updating = launchGuarded(onError = ::logError) {
            updateWebSources(
                vaultPath = vaultPath,
                target = path,
                today = today()
            ).collect { step ->
                reduce { it.copy(update = step) }

                if (step is UpdateStep.Done && step.updated.isNotEmpty()) {
                    emit(effect = ClipEffect.Rewritten(paths = step.updated))
                }
            }

            // Nobody is shown the automatic update's end.
            if (!state.value.isUpdateShown) reduce { it.copy(update = null) }
        }
    }

    private fun clipWhole(links: SectionLinks) {
        clipping = launchGuarded(onError = ::logError) {
            clipSection(
                url = url,
                links = links
            ).collect { step ->
                when (step) {
                    is SectionStep.Clipping -> reduce {
                        it.copy(
                            progress = step,
                            section = null
                        )
                    }

                    is SectionStep.Failed -> reduce {
                        it.copy(
                            progress = null,
                            error = step.error
                        )
                    }

                    is SectionStep.Done -> {
                        val summary = state.value.sectionSummary
                        written(done = step.copy(summary = summary))
                    }
                }
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

    // The page as the index of its folder of pages, both under a name no note or folder has yet ("Overview 2").
    private suspend fun written(done: SectionStep.Done) {
        var title = done.main.title
        var number = 1

        while (isTaken(title = title)) {
            number++
            title = "${done.main.title} $number"
        }

        val notes = WebClipSection.notesOf(
            done = done.copy(main = done.main.copy(title = title)),
            today = today(),
            levels = levels
        )

        levels = 1

        val folder = WebClipNote.FOLDER.child(name = WebClipNote.nameOf(title = title))

        noteFileOperations.createFolder(
            vaultPath = vaultPath,
            path = WebClipNote.FOLDER
        )

        noteFileOperations.createFolder(
            vaultPath = vaultPath,
            path = folder
        )

        notes.forEach { (path, text) ->
            noteFileOperations.createFile(
                vaultPath = vaultPath,
                path = path
            )

            noteContentStore.write(
                vaultPath = vaultPath,
                path = path,
                content = text
            )
        }

        reduce { ClipState() }
        emit(effect = ClipEffect.Opened(path = notes.first().first))
    }

    private suspend fun isTaken(title: String): Boolean {
        val note = WebClipNote.pathOf(title = title)
        val folder = WebClipNote.FOLDER.child(name = WebClipNote.nameOf(title = title))

        val isNote = noteFileOperations.exists(vaultPath, note)

        return isNote || noteFileOperations.exists(vaultPath, folder)
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

        reduce {
            it.copy(
                step = null,
                progress = null
            )
        }
    }

    private companion object {
        const val LOG_TAG = "ClipViewModel"
        const val FAILED_MESSAGE = "The page could not be clipped"
    }
}
