package app.notekea.notes.presentation.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import app.notekea.core.domain.Outcome
import app.notekea.core.presentation.BaseViewModel
import app.notekea.notes.domain.FormatAction
import app.notekea.notes.domain.GetEditorModeUseCase
import app.notekea.notes.domain.ListAction
import app.notekea.notes.domain.ListEditing
import app.notekea.notes.domain.MarkdownFormatter
import app.notekea.notes.domain.NoteDiskChange
import app.notekea.notes.domain.NoteDocument
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.ObserveNoteOnDiskUseCase
import app.notekea.notes.domain.OpenNoteUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.RenameAndUpdateLinksUseCase
import app.notekea.notes.domain.SaveEditorModeUseCase
import app.notekea.notes.domain.SaveNoteUseCase
import app.notekea.notes.domain.TaskFinder
import co.touchlab.kermit.Logger
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The note editor (`ROADMAP.md` 1.5). [buffer] holds the body being edited; the text field edits it directly and this
 * ViewModel watches it: an edit marks the note unsaved and saves it [AUTOSAVE_DELAY] after the last keystroke, Save
 * does it at once, and opening another note or closing the window saves first, so typing never loses text. The header
 * (frontmatter) is kept apart and written back untouched. The open file is watched: the editor's own saves are
 * recognized, an outside change reloads a note without unsaved edits and otherwise asks (Keep mine / Load theirs), and
 * a deleted file offers Restore or Close. Saves never overwrite an outside change nor bring back a file that is gone
 * ([SaveNoteUseCase]). Leaving the text saves at once; the title renames the note and updates the links to it.
 * Notes open in the mode the user last chose (Edit or Read), remembered between runs. A focus request gives the
 * keyboard back to the text once its note is open (after the quick switcher closes).
 */
class EditorViewModel(
    private val vaultPath: String,
    private val openNote: OpenNoteUseCase,
    private val saveNote: SaveNoteUseCase,
    private val observeNoteOnDisk: ObserveNoteOnDiskUseCase,
    private val renameAndUpdateLinks: RenameAndUpdateLinksUseCase,
    private val getEditorMode: GetEditorModeUseCase,
    private val saveEditorMode: SaveEditorModeUseCase
) : BaseViewModel<EditorState, EditorIntent, EditorEffect>(initialState = EditorState()) {
    /** The body of the open note, edited in place by the text field. */
    val buffer = TextFieldState()

    private val logger = Logger.withTag(tag = LOG_TAG)
    private val noteSearch = NoteSearch()
    private val taskFinder = TaskFinder()
    private val markdownFormatter = MarkdownFormatter()
    private val listEditing = ListEditing()
    private val saveLock = Mutex()
    private var autosaveJob: Job? = null
    private var watchJob: Job? = null

    // The note as last read from or written to disk: the header kept apart, and what the file holds.
    private var opened: NoteDocument? = null
    private var onDisk: String? = null

    // The mode every note opens in: the one the user last chose, here and in earlier runs.
    private var isReadingChosen = false

    // A note whose text takes the keyboard as soon as it finishes opening; dropped if another note opens instead.
    private var focusPending: RelativePath? = null

    // The selection when a picker (the note to link) took the keyboard, which clears it in the text field: what is
    // chosen formats it, and closing the picker puts it back.
    private var pickerSelection: TextRange? = null

    init {
        launchGuarded(onError = ::showUnexpectedError) {
            isReadingChosen = getEditorMode()
            reduce { if (it.find == null) it.copy(isReading = isReadingChosen) else it }
        }
        launchGuarded(onError = ::showUnexpectedError) {
            snapshotFlow { buffer.text.toString() }.collect { onBufferChanged(body = it) }
        }
    }

    override fun onIntent(
        intent: EditorIntent
    ) {
        when (intent) {
            EditorIntent.OnCloseDeleted -> closeDeleted()
            is EditorIntent.OnLinkClick -> openLink(link = intent.link)
            is EditorIntent.OnTagClick -> emit(effect = EditorEffect.SearchTag(tag = intent.tag))
            is EditorIntent.OnFocusRequest -> requestFocus(path = intent.path)
            is EditorIntent.OnTaskToggle -> toggleTask(stateIndex = intent.stateIndex)
            EditorIntent.OnTaskToggleShortcut -> toggleTaskAtCursor()
            is EditorIntent.OnFormat -> format(action = intent.action)
            is EditorIntent.OnListKey -> editList(action = intent.action)
            EditorIntent.OnNoteLinkRequest -> requestNoteLink()
            EditorIntent.OnFindClose -> closeFind()
            EditorIntent.OnFindNext -> moveToMatch(step = 1)
            EditorIntent.OnFindOpen -> openFind()
            EditorIntent.OnFindPrevious -> moveToMatch(step = -1)
            is EditorIntent.OnFindQueryChange -> search(query = intent.query)
            EditorIntent.OnFlushRequest -> runSave { flush() }
            EditorIntent.OnFocusLost -> runSave { saveNow() }
            EditorIntent.OnKeepMine -> runSave { keepMine() }
            EditorIntent.OnLoadTheirs -> runSave { loadTheirs() }
            is EditorIntent.OnNoteMoved -> moveTo(path = intent.path)
            is EditorIntent.OnOpen -> runSave { open(path = intent.path) }
            EditorIntent.OnReadModeToggle -> chooseMode(isReading = !state.value.isReading)
            EditorIntent.OnRestore -> runSave { restore() }
            EditorIntent.OnRetryOpen -> runSave { open(path = state.value.notePath, isRetry = true) }
            EditorIntent.OnSaveShortcut -> runSave { saveNow() }
            is EditorIntent.OnTitleCommit -> runSave { rename(name = intent.name) }
            EditorIntent.OnTitleErrorDismiss -> reduce { it.copy(titleError = null) }
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
        launchGuarded(onError = ::showUnexpectedError) { saveEditorMode(isReading = isReading) }
    }

    /** Asks the panes around the editor to open the note a Read mode link leads to, from the open note. */
    private fun openLink(
        link: ReadLink.Note
    ) {
        val source = state.value.notePath ?: return
        emit(effect = EditorEffect.OpenLink(source = source, target = link.target, kind = link.kind))
    }

    private fun onBufferChanged(
        body: String
    ) {
        state.value.find?.let { find -> updateMatches(query = find.query, body = body, keepCurrent = true) }
        val document = opened ?: return
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
            saveLock.withLock { saveNow() }
        }
    }

    /** Runs [action] alone: saves, loads and answers to a conflict never interleave. */
    private fun runSave(
        action: suspend () -> Unit
    ) {
        launchGuarded(onError = ::showUnexpectedError) { saveLock.withLock { action() } }
    }

    private suspend fun open(
        path: RelativePath?,
        isRetry: Boolean = false
    ) {
        if (path == state.value.notePath && !isRetry) return
        if (path != focusPending) focusPending = null
        pickerSelection = null
        saveNow()
        watchJob?.cancel()
        opened = null
        onDisk = null
        reduce { EditorState(notePath = path, isLoading = path != null, isReading = isReadingChosen) }
        if (path == null) {
            replaceBuffer(body = "", isNewNote = true)
            return
        }
        when (val outcome = openNote(vaultPath = vaultPath, path = path)) {
            is Outcome.Failure -> reduce { it.copy(isLoading = false, loadError = outcome.error) }

            is Outcome.Success -> {
                show(document = outcome.value, isNewNote = true)
                reduce { it.copy(isLoading = false) }
                if (focusPending == path) focusText()
                watch(path = path)
            }
        }
    }

    /**
     * Checks or unchecks the task whose state is at [stateIndex], by editing the text like a keystroke would, so it is
     * saved as any edit and can be undone. Nothing happens when that character is no longer a task's state.
     */
    private fun toggleTask(
        stateIndex: Int
    ) {
        val state = buffer.text.getOrNull(index = stateIndex) ?: return
        if (state !in TASK_STATES) return
        val toggled = if (state == ' ') DONE_STATE else OPEN_STATE
        buffer.edit { replace(start = stateIndex, end = stateIndex + 1, text = toggled) }
    }

    /**
     * Applies a toolbar [action] to the selection as one edit (undone in one step, saved like typing), selects what it
     * says, and gives the keyboard back to the text, which the toolbar button took.
     */
    private fun format(
        action: FormatAction
    ) {
        if (!isEditable()) return
        val length = buffer.text.length
        val selection = pickerSelection?.let { TextRange(it.start.coerceAtMost(length), it.end.coerceAtMost(length)) }
            ?: buffer.selection
        pickerSelection = null
        val edit = markdownFormatter.format(
            text = buffer.text.toString(),
            selectionStart = selection.start,
            selectionEnd = selection.end,
            action = action
        )
        buffer.edit {
            replace(start = edit.start, end = edit.end, text = edit.replacement)
            this.selection = TextRange(start = edit.selectionStart, end = edit.selectionEnd)
        }
        emit(effect = EditorEffect.FocusText)
    }

    /** A key in a list item, as in other editors: the edit replaces what the key would have typed. */
    private fun editList(
        action: ListAction
    ) {
        if (!isEditable()) return
        val selection = buffer.selection
        val edit = listEditing.edit(
            text = buffer.text.toString(),
            selectionStart = selection.start,
            selectionEnd = selection.end,
            action = action
        ) ?: return
        buffer.edit {
            replace(start = edit.start, end = edit.end, text = edit.replacement)
            this.selection = TextRange(start = edit.selectionStart, end = edit.selectionEnd)
        }
    }

    private fun requestNoteLink() {
        if (!isEditable()) return
        pickerSelection = buffer.selection
        emit(effect = EditorEffect.RequestNoteLink)
    }

    /** Whether the text can be changed now: a note is open and loaded, in Edit mode. */
    private fun isEditable(): Boolean = state.value.let { it.notePath != null && !it.isLoading && !it.isReading }

    private fun toggleTaskAtCursor() {
        if (state.value.isReading || state.value.notePath == null) return
        val text = buffer.text.toString()
        taskFinder.taskAt(text = text, offset = buffer.selection.start)?.let { toggleTask(stateIndex = it.stateIndex) }
    }

    /** The text of [path] (the open note when null) takes the keyboard now if it is open, or once it opens. */
    private fun requestFocus(
        path: RelativePath?
    ) {
        // A picker closed without a choice: the text gets back the selection it had.
        pickerSelection?.takeIf { path == null && it.max <= buffer.text.length }?.let { kept ->
            buffer.edit { selection = kept }
        }
        pickerSelection = null
        val target = path ?: state.value.notePath ?: return
        val isOpen = state.value.let { it.notePath == target && !it.isLoading && it.loadError == null }
        if (isOpen) focusText() else focusPending = target
    }

    private fun focusText() {
        focusPending = null
        emit(effect = EditorEffect.FocusText)
    }

    /** Saves the edits, unless there is nothing new or the user has a question to answer first. */
    private suspend fun saveNow() {
        val path = state.value.notePath ?: return
        val document = opened ?: return
        val body = buffer.text.toString()
        val blocked = state.value.hasConflict || state.value.isDeleted
        if (body == document.body || blocked) return
        write(path = path, document = document.copy(body = body), expectedOnDisk = onDisk)
    }

    private suspend fun write(
        path: RelativePath,
        document: NoteDocument,
        expectedOnDisk: String?
    ) {
        when (val outcome = saveNote(vaultPath, path, document, expectedOnDisk)) {
            is Outcome.Success -> {
                opened = document
                onDisk = outcome.value
                val isCurrent = buffer.text.toString() == document.body
                reduce {
                    it.copy(
                        saveStatus = if (isCurrent) SaveStatus.SAVED else SaveStatus.UNSAVED,
                        saveError = null,
                        hasConflict = false,
                        isDeleted = false
                    )
                }
            }

            is Outcome.Failure -> reduceSaveFailure(error = outcome.error)
        }
    }

    private fun reduceSaveFailure(
        error: NotesError
    ) {
        logger.w { "Note save failed: ${error::class.simpleName}" }
        reduce {
            when (error) {
                is NotesError.ChangedOnDisk -> it.copy(hasConflict = true)
                is NotesError.EntryMissing -> it.copy(isDeleted = true)
                else -> it.copy(saveError = error)
            }
        }
    }

    /** Saves first, so the file that moves holds every edit; the links to the note follow the new name. */
    private suspend fun rename(
        name: String
    ) {
        val from = state.value.notePath ?: return
        if (name.trim() == from.nameWithoutExtension) return
        saveNow()
        when (val outcome = renameAndUpdateLinks(vaultPath = vaultPath, path = from, newName = name)) {
            is Outcome.Failure -> reduce { it.copy(titleError = outcome.error) }

            is Outcome.Success -> {
                val renamed = outcome.value
                watchJob?.cancel()
                reduce { it.copy(notePath = renamed.path, titleError = null) }
                watch(path = renamed.path)
                emit(effect = EditorEffect.Renamed(from = from, to = renamed.path, links = renamed.links))
            }
        }
    }

    private suspend fun flush() {
        saveNow()
        emit(effect = EditorEffect.Flushed)
    }

    /** Overwrites the outside change: the file's current text is what the save now expects to replace. */
    private suspend fun keepMine() {
        val path = state.value.notePath ?: return
        val document = opened ?: return
        val current = openNote(vaultPath = vaultPath, path = path)
        val expected = (current as? Outcome.Success)?.value?.toText()
        reduce { it.copy(hasConflict = false) }
        write(path = path, document = document.copy(body = buffer.text.toString()), expectedOnDisk = expected)
    }

    private suspend fun loadTheirs() {
        val path = state.value.notePath ?: return
        when (val outcome = openNote(vaultPath = vaultPath, path = path)) {
            is Outcome.Failure -> reduce { it.copy(hasConflict = false, isDeleted = true) }

            is Outcome.Success -> {
                show(document = outcome.value, isNewNote = false)
                reduce { it.copy(hasConflict = false, saveStatus = SaveStatus.NONE, saveError = null) }
            }
        }
    }

    private suspend fun restore() {
        val path = state.value.notePath ?: return
        val document = opened ?: return
        write(path = path, document = document.copy(body = buffer.text.toString()), expectedOnDisk = null)
    }

    private fun closeDeleted() {
        runSave {
            open(path = null)
            emit(effect = EditorEffect.Closed)
        }
    }

    private fun moveTo(
        path: RelativePath
    ) {
        if (state.value.notePath == null) return
        watchJob?.cancel()
        reduce { it.copy(notePath = path, isDeleted = false) }
        watch(path = path)
    }

    private fun watch(
        path: RelativePath
    ) {
        watchJob = launchGuarded(onError = ::showUnexpectedError) {
            observeNoteOnDisk(vaultPath = vaultPath, path = path).collect { change ->
                saveLock.withLock { onDiskChange(change = change) }
            }
        }
    }

    /** The editor's own saves are recognized and ignored; anything else reloads or asks. */
    private fun onDiskChange(
        change: NoteDiskChange
    ) {
        when (change) {
            NoteDiskChange.Deleted -> {
                autosaveJob?.cancel()
                reduce { it.copy(isDeleted = true) }
            }

            is NoteDiskChange.Changed -> {
                if (change.text == onDisk && !state.value.isDeleted) return
                val hasEdits = buffer.text.toString() != opened?.body
                if (hasEdits || state.value.hasConflict) {
                    reduce { it.copy(hasConflict = true, isDeleted = false) }
                } else {
                    show(document = NoteDocument.fromText(text = change.text), isNewNote = false)
                    reduce { it.copy(isDeleted = false) }
                }
            }
        }
    }

    /** Shows [document] as the saved state of the note; a new note starts at the top with a fresh undo history. */
    private fun show(
        document: NoteDocument,
        isNewNote: Boolean
    ) {
        opened = document
        onDisk = document.toText()
        replaceBuffer(body = document.body, isNewNote = isNewNote)
    }

    // Clearing the undo history is still experimental; without it, Undo would bring back the previous note's text.
    @OptIn(ExperimentalFoundationApi::class)
    private fun replaceBuffer(
        body: String,
        isNewNote: Boolean
    ) {
        val cursor = buffer.selection.start.coerceAtMost(maximumValue = body.length)
        buffer.edit {
            replace(start = 0, end = length, text = body)
            if (isNewNote) placeCursorBeforeCharAt(index = 0) else placeCursorBeforeCharAt(index = cursor)
        }
        if (isNewNote) buffer.undoState.clearHistory()
    }

    private fun search(
        query: String
    ) {
        updateMatches(query = query, body = buffer.text.toString(), keepCurrent = false)
        selectCurrentMatch()
    }

    /** Finds [query] again; after an edit the current match stays the same one when it still exists. */
    private fun updateMatches(
        query: String,
        body: String,
        keepCurrent: Boolean
    ) {
        val matches = noteSearch.matches(text = body, query = query)
        reduce { state ->
            val previous = state.find?.current?.takeIf { keepCurrent }
            val current = if (matches.isEmpty()) {
                null
            } else {
                (previous ?: 0).coerceAtMost(
                    maximumValue = matches.lastIndex
                )
            }
            state.copy(find = FindState(query = query, matches = matches, current = current))
        }
    }

    private fun moveToMatch(
        step: Int
    ) {
        reduce { state ->
            val find = state.find ?: return@reduce state
            val count = find.matches.size
            if (count == 0) return@reduce state
            val next = ((find.current ?: 0) + step + count) % count
            state.copy(find = find.copy(current = next))
        }
        selectCurrentMatch()
    }

    /** Selects the current match in the text, so closing the bar leaves the cursor on it. */
    private fun selectCurrentMatch() {
        val find = state.value.find ?: return
        val match = find.current?.let { find.matches.getOrNull(index = it) } ?: return
        buffer.edit { selection = TextRange(start = match.first, end = match.last + 1) }
    }

    private fun closeFind() {
        selectCurrentMatch()
        reduce { it.copy(find = null) }
    }

    private fun showUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
        reduce { it.copy(isLoading = false) }
    }

    companion object {
        private const val DONE_STATE = "x"
        private const val OPEN_STATE = " "
        private const val TASK_STATES = " xX"
        val AUTOSAVE_DELAY = 1.seconds
        private const val LOG_TAG = "EditorViewModel"
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the note editor"
    }
}
