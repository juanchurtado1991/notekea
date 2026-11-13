package app.notekea.editor.presentation.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.FillTemplateUseCase
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.BaseViewModel
import app.notekea.editor.domain.EditorPreferencesStore
import app.notekea.editor.domain.KeepUnsavedTextUseCase
import app.notekea.editor.domain.ObserveNoteOnDiskUseCase
import app.notekea.editor.domain.OpenNoteUseCase
import app.notekea.editor.domain.RenameAndUpdateLinksUseCase
import app.notekea.editor.domain.SaveNoteUseCase
import app.notekea.editor.presentation.attachments.AttachmentTools
import app.notekea.editor.presentation.find.EditorFind
import app.notekea.editor.presentation.find.FindState
import app.notekea.editor.presentation.model.EditorEffect
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.EditorState
import app.notekea.editor.presentation.model.PropertyIntent
import app.notekea.editor.presentation.model.SaveStatus
import app.notekea.editor.presentation.noteinfo.EditorNoteInfo
import app.notekea.editor.presentation.noteinfo.NoteInfoTools
import app.notekea.editor.presentation.properties.EditorProperties
import app.notekea.editor.presentation.properties.PropertyTools
import app.notekea.editor.presentation.reading.ReadLink
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.seconds

/**
 * The note editor. The text field edits [buffer] directly and this ViewModel watches it: an edit saves [AUTOSAVE_DELAY]
 * after the last keystroke, and opening another note or closing the window saves first. The header is written back
 * untouched ([EditorSaving]); outside changes are watched ([EditorDisk]). Its other parts each take their intents.
 */
class EditorViewModel(
    private val vaultPath: String,
    private val openNote: OpenNoteUseCase,
    saveNote: SaveNoteUseCase,
    observeNoteOnDisk: ObserveNoteOnDiskUseCase,
    renameAndUpdateLinks: RenameAndUpdateLinksUseCase,
    private val editorPreferences: EditorPreferencesStore,
    attachmentTools: AttachmentTools,
    fillTemplate: FillTemplateUseCase,
    propertyTools: PropertyTools,
    noteInfoTools: NoteInfoTools,
    keepUnsavedText: KeepUnsavedTextUseCase
) : BaseViewModel<EditorState, EditorIntent, EditorEffect>(initialState = EditorState()) {
    /** The body of the open note, edited in place by the text field. */
    val buffer = TextFieldState()

    private val logger = Logger.withTag(tag = LOG_TAG)
    private val host = object : EditorHost {
        override val state: StateFlow<EditorState> get() = this@EditorViewModel.state
        override val buffer: TextFieldState get() = this@EditorViewModel.buffer

        override fun reduce(transform: (EditorState) -> EditorState) = this@EditorViewModel.reduce(
            transform = transform
        )

        override fun emit(effect: EditorEffect) = this@EditorViewModel.emit(effect = effect)

        override fun launch(block: suspend () -> Unit): Job = launchGuarded(
            onError = ::showUnexpectedError,
            block = block
        )
    }
    private val find = EditorFind(host = host)
    private val textEdits = EditorTextEdits(host = host)
    private val noteInfo =
        EditorNoteInfo(host, vaultPath, noteInfoTools, propertyTools, attachmentTools.dispatchers)
    private val files =
        EditorFiles(host, vaultPath, attachmentTools, textEdits) { focus.request(path = null) }
    private val saving = EditorSaving(
        host = host,
        vaultPath = vaultPath,
        saveNote = saveNote,
        noteInfo = noteInfo
    )
    private val unsaved = EditorUnsaved(
        host = host,
        vaultPath = vaultPath,
        keepUnsavedText = keepUnsavedText
    )
    private val properties = EditorProperties(host, saving, noteInfo, editorPreferences)
    private val disk =
        EditorDisk(host, vaultPath, openNote, observeNoteOnDisk, saving) { autosaveJob?.cancel() }
    private val renaming = EditorRenaming(host, vaultPath, renameAndUpdateLinks, disk, ::saveNow)
    private val templates = EditorTemplates(host, vaultPath, fillTemplate, saving, textEdits)
    private val focus = EditorFocus(
        host = host,
        textEdits = textEdits
    )

    private var autosaveJob: Job? = null

    init {
        launchGuarded(onError = ::showUnexpectedError) {
            properties.loadFold()

            reduce { it.copy(isPropertiesExpanded = properties.isExpandedChosen) }
        }

        launchGuarded(onError = ::showUnexpectedError) {
            snapshotFlow { buffer.text.toString() }
                .collect { onBufferChanged(body = it) }
        }
    }

    override fun handleIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.Edit -> onEditIntent(intent = intent)

            is EditorIntent.Find -> onFindIntent(intent = intent)

            is EditorIntent.Note -> onNoteIntent(intent = intent)

            is EditorIntent.Navigation -> onNavigationIntent(intent = intent)

            is EditorIntent.Attachment -> files.onIntent(intent = intent)

            is PropertyIntent -> properties.onIntent(intent = intent)

            // The side panel's own host acts on these, beside the editor.
            is EditorIntent.SidePanel -> Unit
        }
    }

    /** The intents that edit the note's text. */
    private fun onEditIntent(intent: EditorIntent.Edit) {
        when (intent) {
            is EditorIntent.OnTaskToggle -> textEdits.toggleTask(stateIndex = intent.stateIndex)

            EditorIntent.OnTaskToggleShortcut -> textEdits.toggleTaskAtCursor()

            is EditorIntent.OnFormat -> textEdits.format(action = intent.action)

            is EditorIntent.OnListKey -> textEdits.editList(action = intent.action)

            is EditorIntent.OnTableEdit -> textEdits.editTable(action = intent.action)

            EditorIntent.OnNoteLinkRequest -> textEdits.requestPicker(
                effect = EditorEffect.RequestNoteLink
            )

            EditorIntent.OnTagRequest ->
                textEdits.requestPicker(
                    effect = EditorEffect.RequestTag(query = textEdits.selectedForTag())
                )

            EditorIntent.OnTemplateRequest -> templates.request()

            is EditorIntent.OnTemplateInsert -> saving.runSave {
                templates.insert(template = intent.template)
            }
        }
    }

    /** Find in the note. */
    private fun onFindIntent(intent: EditorIntent.Find) {
        when (intent) {
            EditorIntent.OnFindClose -> find.close()

            EditorIntent.OnFindNext -> find.moveToMatch(step = 1)

            EditorIntent.OnFindOpen -> reduce {
                it.copy(
                    isRevealing = false,
                    find = it.find ?: FindState()
                )
            }

            EditorIntent.OnFindPrevious -> find.moveToMatch(step = -1)

            is EditorIntent.OnFindQueryChange -> find.search(query = intent.query)
        }
    }

    /** The intents about the note itself: opening, saving, its file, its title, links and focus. */
    private fun onNoteIntent(intent: EditorIntent.Note) {
        when (intent) {
            is EditorIntent.OnOpen -> saving.runSave { open(path = intent.path) }

            EditorIntent.OnRetryOpen -> saving.runSave {
                open(
                    path = state.value.notePath,
                    isRetry = true
                )
            }

            EditorIntent.OnFlushRequest -> saving.runSave { flush() }

            is EditorIntent.OnHandOver -> saving.runSave {
                saveNow()
            }.invokeOnCompletion { intent.done.complete(Unit) }

            EditorIntent.OnFocusLost, EditorIntent.OnSaveShortcut -> saving.runSave { saveNow() }

            EditorIntent.OnKeepMine -> saving.runSave {
                disk.keepMine().also { properties.writePendingSource() }
            }

            EditorIntent.OnLoadTheirs -> saving.runSave {
                disk.loadTheirs().also { properties.cancelSourceEdit() }
            }

            EditorIntent.OnRestore -> saving.runSave { disk.restore() }

            EditorIntent.OnCloseDeleted -> saving.runSave { closeDeleted() }

            is EditorIntent.OnNoteMoved -> disk.moveTo(path = intent.path)

            is EditorIntent.OnNoteRewritten -> saving.runSave {
                disk.followRewrite(path = intent.path)
            }

            is EditorIntent.OnTitleCommit -> saving.runSave { renaming.rename(name = intent.name) }

            EditorIntent.OnTitleErrorDismiss -> reduce { it.copy(titleError = null) }

            EditorIntent.OnSourceModeToggle -> reduce { it.copy(isSourceMode = !it.isSourceMode) }
        }
    }

    /** The intents that lead elsewhere: links, tags, the star, the folder, headings, days, focus, and files. */
    private fun onNavigationIntent(intent: EditorIntent.Navigation) {
        when (intent) {
            is EditorIntent.OnLinkClick -> openLink(
                link = intent.link,
                inBackground = intent.inBackground
            )

            is EditorIntent.OnTagClick -> emit(effect = EditorEffect.SearchTag(tag = intent.tag))

            is EditorIntent.OnFocusRequest -> focus.request(path = intent.path)

            EditorIntent.OnBookmarkToggle ->
                state
                    .value
                    .notePath
                    ?.let { emit(effect = EditorEffect.ToggleBookmark(it)) }

            EditorIntent.OnFolderReveal -> revealFolder()

            is EditorIntent.OnHeadingClick -> focus.revealHeading(index = intent.index)

            is EditorIntent.OnRevealRequest -> focus.reveal(
                path = intent.path,
                range = intent.range
            )

            is EditorIntent.OnDayStep -> emit(effect = EditorEffect.DayStep(step = intent.step))
        }
    }

    /** Asks the panes around the editor to open the note a link leads to, from the open note. */
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
        val folder = state
            .value
            .notePath
            ?.parent
            ?.takeUnless { it.isRoot } ?: return

        emit(effect = EditorEffect.RevealFolder(path = folder))
    }

    private fun onBufferChanged(body: String) {
        state
            .value
            .find
            ?.let {
                find.updateMatches(
                    query = it.query,
                    body = body,
                    keepCurrent = true
                )
            }

        files.update(body = body)
        noteInfo.updateOutline(body = body)
        noteInfo.updateStats(body = body)
        val document = saving.opened ?: return
        autosaveJob?.cancel()

        if (body == document.body) {
            reduce {
                if (it.saveStatus == SaveStatus.UNSAVED) {
                    it.copy(saveStatus = SaveStatus.NONE)
                } else {
                    it
                }
            }

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
        keepWhatWasNotSaved()
        disk.stop()
        saving.forget()
        focus.leave(path = path)

        reduce {
            EditorState(
                notePath = path,
                isLoading = path != null,
                isPropertiesExpanded = properties.isExpandedChosen
            )
        }

        if (path == null) {
            saving.replaceBuffer(
                body = "",
                isNewNote = true
            )

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
                saving.show(
                    document = outcome.value,
                    isNewNote = true
                )

                focus.arrive(path = path)

                reduce { it.copy(isLoading = false) }

                unsaved.takeDraft(path = path)?.let {
                    saving.replaceBuffer(
                        body = it,
                        isNewNote = false
                    )
                }

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

    private suspend fun flush() {
        saveNow()
        keepWhatWasNotSaved()
        emit(effect = EditorEffect.Flushed)
    }

    /** After [saveNow], whatever the file still does not hold (a save was blocked or failed) is kept, never dropped. */
    private suspend fun keepWhatWasNotSaved() {
        val path = state.value.notePath ?: return
        val document = saving.opened ?: return
        val header = properties.headerWithPendingSource(document = document)
        val isSaved = !saving.hasEdits() && header == document.header
        if (isSaved) return

        unsaved.keep(
            path = path,
            document = document.copy(
                header = header,
                body = buffer.text.toString()
            )
        )
    }

    private suspend fun closeDeleted() {
        open(path = null)
        emit(effect = EditorEffect.Closed)
    }

    private fun showUnexpectedError(throwable: Throwable) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }

        val error = NotesError.OperationFailed(
            path = state
                .value
                .notePath
                ?.value
                .orEmpty()
        )

        reduce {
            if (it.isLoading) {
                it.copy(
                    isLoading = false,
                    loadError = error
                )
            } else {
                it.copy(saveError = error)
            }
        }
    }

    companion object {
        val AUTOSAVE_DELAY = 1.seconds
        private const val LOG_TAG = "EditorViewModel"
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the note editor"
    }
}
