package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import app.notekea.core.domain.Outcome
import app.notekea.core.presentation.BaseViewModel
import app.notekea.notes.domain.EditorPreferencesStore
import app.notekea.notes.domain.FillTemplateUseCase
import app.notekea.notes.domain.ObserveNoteOnDiskUseCase
import app.notekea.notes.domain.OpenNoteUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.RenameAndUpdateLinksUseCase
import app.notekea.notes.domain.SaveNoteUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.seconds

/**
 * The note editor (`ROADMAP.md` 1.5). [buffer] holds the body being edited; the text field edits it directly and this
 * ViewModel watches it: an edit marks the note unsaved and saves it [AUTOSAVE_DELAY] after the last keystroke, Save
 * does it at once, and opening another note or closing the window saves first, so typing never loses text. The header
 * (frontmatter) is kept apart and written back untouched ([EditorSaving]); the file is watched for outside changes
 * ([EditorDisk]). Leaving the text saves at once; the title renames the note and updates the links to it. Notes open in
 * the mode the user last chose (Edit or Read), remembered between runs. The rest has parts of its own, which this
 * ViewModel routes the intents to: the keyboard and the cursor ([EditorFocus]), Find ([EditorFind]), the edits a button or key makes ([EditorTextEdits]), the files
 * ([EditorFiles]), the outline and Note info ([EditorNoteInfo]), the properties ([EditorProperties]) and templates
 * ([EditorTemplates]).
 */
class EditorViewModel(
    private val vaultPath: String,
    private val openNote: OpenNoteUseCase,
    saveNote: SaveNoteUseCase,
    observeNoteOnDisk: ObserveNoteOnDiskUseCase,
    private val renameAndUpdateLinks: RenameAndUpdateLinksUseCase,
    private val editorPreferences: EditorPreferencesStore,
    attachmentTools: AttachmentTools,
    fillTemplate: FillTemplateUseCase,
    propertyTools: PropertyTools,
    noteInfoTools: NoteInfoTools
) : BaseViewModel<EditorState, EditorIntent, EditorEffect>(initialState = EditorState()) {
    /** The body of the open note, edited in place by the text field. */
    val buffer = TextFieldState()

    private val logger = Logger.withTag(tag = LOG_TAG)
    private val host = object : EditorHost {
        override val state: StateFlow<EditorState> get() = this@EditorViewModel.state
        override val buffer: TextFieldState get() = this@EditorViewModel.buffer

        override fun reduce(
            transform: (EditorState) -> EditorState
        ) = this@EditorViewModel.reduce(transform = transform)

        override fun emit(
            effect: EditorEffect
        ) = this@EditorViewModel.emit(effect = effect)

        override fun launch(
            block: suspend () -> Unit
        ): Job = launchGuarded(onError = ::showUnexpectedError, block = block)
    }
    private val find = EditorFind(host = host)
    private val textEdits = EditorTextEdits(host = host)
    private val noteInfo = EditorNoteInfo(host, vaultPath, noteInfoTools, propertyTools, attachmentTools.dispatchers)
    private val files = EditorFiles(host, vaultPath, attachmentTools, textEdits) { focus.request(path = null) }
    private val saving = EditorSaving(host = host, vaultPath = vaultPath, saveNote = saveNote, noteInfo = noteInfo)
    private val properties = EditorProperties(host, saving, noteInfo, editorPreferences)
    private val disk = EditorDisk(host, vaultPath, openNote, observeNoteOnDisk, saving) { autosaveJob?.cancel() }
    private val templates = EditorTemplates(host, vaultPath, fillTemplate, saving, textEdits) { chooseMode(false) }
    private val focus = EditorFocus(host = host, textEdits = textEdits)

    private var autosaveJob: Job? = null

    // The mode every note opens in: the one the user last chose, here and in earlier runs.
    private var isReadingChosen = false

    init {
        launchGuarded(onError = ::showUnexpectedError) {
            isReadingChosen = editorPreferences.isReadMode()
            properties.loadFold()
            reduce {
                val opened = it
                    .copy(isPropertiesExpanded = properties.isExpandedChosen)

                if (it.find == null) {
                    opened.copy(isReading = isReadingChosen)
                } else {
                    opened
                }
            }
        }
        launchGuarded(onError = ::showUnexpectedError) {
            snapshotFlow { buffer.text.toString() }
                .collect { onBufferChanged(body = it) }
        }
    }

    override fun onIntent(
        intent: EditorIntent
    ) {
        when (intent) {
            is EditorIntent.OnTaskToggle -> textEdits.toggleTask(stateIndex = intent.stateIndex)

            EditorIntent.OnTaskToggleShortcut -> textEdits.toggleTaskAtCursor()

            is EditorIntent.OnFormat -> textEdits.format(action = intent.action)

            is EditorIntent.OnListKey -> textEdits.editList(action = intent.action)

            EditorIntent.OnNoteLinkRequest -> textEdits.requestPicker(effect = EditorEffect.RequestNoteLink)

            EditorIntent.OnTagRequest ->
                textEdits.requestPicker(effect = EditorEffect.RequestTag(query = textEdits.selectedForTag()))

            EditorIntent.OnTemplateRequest -> templates.request()

            is EditorIntent.OnTemplateInsert -> saving.runSave { templates.insert(template = intent.template) }

            is PropertyIntent -> properties.onIntent(intent = intent)

            EditorIntent.OnFindClose -> find.close()

            EditorIntent.OnFindNext -> find.moveToMatch(step = 1)

            EditorIntent.OnFindOpen -> openFind()

            EditorIntent.OnFindPrevious -> find.moveToMatch(step = -1)

            is EditorIntent.OnFindQueryChange -> find.search(query = intent.query)

            else -> onNoteIntent(intent = intent)
        }
    }

    /** The intents about the note itself: opening, saving, its file, its title, links and focus. */
    private fun onNoteIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.OnOpen -> saving.runSave { open(path = intent.path) }
            EditorIntent.OnRetryOpen -> saving.runSave { open(path = state.value.notePath, isRetry = true) }
            EditorIntent.OnFlushRequest -> saving.runSave { flush() }
            EditorIntent.OnFocusLost, EditorIntent.OnSaveShortcut -> saving.runSave { saveNow() }
            EditorIntent.OnKeepMine -> saving.runSave { disk.keepMine() }
            EditorIntent.OnLoadTheirs -> saving.runSave { disk.loadTheirs() }
            EditorIntent.OnRestore -> saving.runSave { disk.restore() }
            EditorIntent.OnCloseDeleted -> saving.runSave { closeDeleted() }
            is EditorIntent.OnNoteMoved -> disk.moveTo(path = intent.path)
            is EditorIntent.OnTitleCommit -> saving.runSave { rename(name = intent.name) }
            EditorIntent.OnTitleErrorDismiss -> reduce { it.copy(titleError = null) }
            EditorIntent.OnReadModeToggle -> chooseMode(isReading = !state.value.isReading)
            else -> onNavigationIntent(intent = intent)
        }
    }

    /** The intents that lead elsewhere: links, tags, the star, the folder, headings, days, focus, and files. */
    private fun onNavigationIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.OnLinkClick -> openLink(link = intent.link, inBackground = intent.inBackground)

            is EditorIntent.OnTagClick -> emit(effect = EditorEffect.SearchTag(tag = intent.tag))

            is EditorIntent.OnFocusRequest -> focus.request(path = intent.path)

            EditorIntent.OnBookmarkToggle -> state.value.notePath?.let {
                emit(effect = EditorEffect.ToggleBookmark(it))
            }

            EditorIntent.OnFolderReveal -> revealFolder()

            is EditorIntent.OnHeadingClick -> focus.revealHeading(index = intent.index)

            is EditorIntent.OnDayStep -> emit(effect = EditorEffect.DayStep(step = intent.step))

            else -> files.onIntent(intent = intent)
        }
    }

    /** Find works on the Markdown, so it switches to Edit mode, which is then the mode remembered. */
    private fun openFind() {
        reduce { it.copy(find = it.find ?: FindState()) }
        chooseMode(isReading = false)
    }

    /** Shows [isReading]'s mode and remembers it for the next notes and runs. */
    private fun chooseMode(
        isReading: Boolean
    ) {
        reduce { it.copy(isReading = isReading, find = if (isReading) null else it.find) }
        if (isReading == isReadingChosen) return
        isReadingChosen = isReading
        launchGuarded(onError = ::showUnexpectedError) { editorPreferences.setReadMode(isReading = isReading) }
    }

    /** Asks the panes around the editor to open the note a Read mode link leads to, from the open note. */
    private fun openLink(link: ReadLink.Note, inBackground: Boolean) {
        val source = state.value.notePath ?: return
        emit(
            effect = EditorEffect.OpenLink(
                source,
                target = link.target,
                kind = link.kind,
                inBackground = inBackground
            )
        )
    }

    /** The note's folder above the title: the window shows it in the tree. */
    private fun revealFolder() {
        val folder = state.value.notePath?.parent?.takeUnless { it.isRoot } ?: return
        emit(effect = EditorEffect.RevealFolder(path = folder))
    }

    private fun onBufferChanged(body: String) {
        state.value.find?.let { find.updateMatches(query = it.query, body = body, keepCurrent = true) }
        files.update(body = body)
        noteInfo.updateOutline(body = body)
        noteInfo.updateStats(body = body)
        val document = saving.opened ?: return
        autosaveJob?.cancel()
        if (body == document.body) {
            reduce { if (it.saveStatus == SaveStatus.UNSAVED) it.copy(saveStatus = SaveStatus.NONE) else it }
            return
        }
        reduce { it.copy(saveStatus = SaveStatus.UNSAVED) }
        autosaveJob = launchGuarded(onError = ::showUnexpectedError) {
            delay(duration = AUTOSAVE_DELAY)
            // From here the save runs to its end: a keystroke during the write schedules the next autosave instead of
            // cancelling this one, which would stop it at the disk with nothing written.
            autosaveJob = null
            saving.alone { saveNow() }
        }
    }

    private suspend fun open(path: RelativePath?, isRetry: Boolean = false) {
        if (path == state.value.notePath && !isRetry) return
        files.forgetFiles()
        saveNow()
        disk.stop()
        saving.forget()
        focus.leave(path = path)
        reduce {
            EditorState(
                notePath = path,
                isLoading = path != null,
                isReading = isReadingChosen,
                isPropertiesExpanded = properties.isExpandedChosen
            )
        }
        if (path == null) {
            saving.replaceBuffer(body = "", isNewNote = true)
            return
        }
        when (val outcome = openNote(vaultPath = vaultPath, path = path)) {
            is Outcome.Failure -> reduce {
                it.copy(
                    isLoading = false,
                    loadError = outcome.error
                )
            }

            is Outcome.Success -> {
                saving.show(document = outcome.value, isNewNote = true)
                focus.arrive(path = path)
                reduce { it.copy(isLoading = false) }
                focus.onShown(path = path)
                disk.watch(path = path)
            }
        }
    }

    /** Saves the edits, unless there is nothing new or the user has a question to answer first. */
    private suspend fun saveNow() {
        properties.writePendingSource()
        val path = state.value.notePath ?: return
        val document = saving.opened ?: return
        val body = buffer.text.toString()
        val blocked = state.value.hasConflict || state.value.isDeleted
        if (body == document.body || blocked) return
        saving.write(
            path = path,
            document = document.copy(body = body),
            expectedOnDisk = saving.onDisk
        )
    }

    /** Saves first, so the file that moves holds every edit; the links to the note follow the new name. */
    private suspend fun rename(name: String) {
        val from = state.value.notePath ?: return
        if (name.trim() == from.nameWithoutExtension) return
        saveNow()
        when (val outcome = renameAndUpdateLinks(vaultPath = vaultPath, path = from, newName = name)) {
            is Outcome.Failure -> reduce { it.copy(titleError = outcome.error) }

            is Outcome.Success -> {
                val renamed = outcome.value
                disk.stop()
                reduce { it.copy(notePath = renamed.path, titleError = null) }
                disk.watch(path = renamed.path)
                emit(effect = EditorEffect.Renamed(from = from, to = renamed.path, links = renamed.links))
            }
        }
    }

    private suspend fun flush() {
        saveNow()
        emit(effect = EditorEffect.Flushed)
    }

    private suspend fun closeDeleted() {
        open(path = null)
        emit(effect = EditorEffect.Closed)
    }

    private fun showUnexpectedError(throwable: Throwable) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
        reduce { it.copy(isLoading = false) }
    }

    companion object {
        val AUTOSAVE_DELAY = 1.seconds
        private const val LOG_TAG = "EditorViewModel"
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the note editor"
    }
}
