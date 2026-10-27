package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.PropertyValue
import app.notekea.notes.domain.PropertyKeys
import app.notekea.notes.domain.FrontmatterContent
import app.notekea.core.domain.time.LocalMoment
import kotlin.time.Instant
import app.notekea.core.domain.StringPatterns
import app.notekea.notes.domain.FrontmatterReader
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.withContext
import app.notekea.core.domain.StringPatterns.NEW_LINE
import app.notekea.notes.domain.Embed
import app.notekea.notes.domain.AttachmentSource
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import app.notekea.core.domain.Outcome
import app.notekea.core.presentation.BaseViewModel
import app.notekea.notes.domain.FormatAction
import app.notekea.notes.domain.TemplateInsertion
import app.notekea.notes.domain.FillTemplateUseCase
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
import app.notekea.notes.domain.OutlineFinder
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
 * keyboard back to the text once its note is open (after the quick switcher closes). The note's headings are kept as
 * its outline (`ROADMAP.md` 1.9), shown in the panel on the right, whose width and visibility are remembered too.
 */
class EditorViewModel(
    private val vaultPath: String,
    private val openNote: OpenNoteUseCase,
    private val saveNote: SaveNoteUseCase,
    private val observeNoteOnDisk: ObserveNoteOnDiskUseCase,
    private val renameAndUpdateLinks: RenameAndUpdateLinksUseCase,
    private val getEditorMode: GetEditorModeUseCase,
    private val saveEditorMode: SaveEditorModeUseCase,
    private val attachmentTools: AttachmentTools,
    private val fillTemplate: FillTemplateUseCase,
    private val propertyTools: PropertyTools,
    private val noteInfoTools: NoteInfoTools
) : BaseViewModel<EditorState, EditorIntent, EditorEffect>(initialState = EditorState()) {
    /** The body of the open note, edited in place by the text field. */
    val buffer = TextFieldState()

    private val logger = Logger.withTag(tag = LOG_TAG)
    private val noteSearch = NoteSearch()
    private val taskFinder = TaskFinder()
    private val markdownFormatter = MarkdownFormatter()
    private val listEditing = ListEditing()
    private val outlineFinder = OutlineFinder()
    private val templateInsertion = TemplateInsertion()
    private val frontmatterReader = FrontmatterReader()
    private val propertyEditing = PropertyEditing()

    // Whether the properties above notes are open: the fold the user last chose, here and in earlier runs.
    private var isPropertiesExpandedChosen = true
    private var outlineJob: Job? = null
    private val saveLock = Mutex()
    private var autosaveJob: Job? = null
    private var sourceJob: Job? = null
    private var statsJob: Job? = null
    private var pendingSource: PropertyIntent.OnSourceEdit? = null
    private var watchJob: Job? = null

    // The note as last read from or written to disk: the header kept apart, and what the file holds.
    private var opened: NoteDocument? = null
    private var onDisk: String? = null

    // The mode every note opens in: the one the user last chose, here and in earlier runs.
    private var isReadingChosen = false

    // Where the cursor was in each note left in this pane, so going back to its tab puts it there again.
    private val selections = mutableMapOf<RelativePath, TextRange>()

    // A note whose text takes the keyboard as soon as it finishes opening; dropped if another note opens instead.
    private var focusPending: RelativePath? = null

    private val attachments = EditorAttachments(
        vaultPath = vaultPath,
        attachmentStore = attachmentTools.attachmentStore,
        vaultSettingsReader = attachmentTools.vaultSettingsReader,
        attachFile = attachmentTools.attachFile,
        dispatchers = attachmentTools.dispatchers
    )
    private var attachmentsJob: Job? = null

    // The selection when a picker (the note to link, the tag) took the keyboard, which clears it in the text field: what is
    // chosen formats it, and closing the picker puts it back.
    private var pickerSelection: TextRange? = null

    init {
        launchGuarded(onError = ::showUnexpectedError) {
            isReadingChosen = getEditorMode()
            isPropertiesExpandedChosen = propertyTools.getPropertiesFold()
            reduce {
                val opened = it.copy(isPropertiesExpanded = isPropertiesExpandedChosen)
                if (it.find == null) opened.copy(isReading = isReadingChosen) else opened
            }
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
            is EditorIntent.OnLinkClick -> openLink(link = intent.link, inBackground = intent.inBackground)
            is EditorIntent.OnTagClick -> emit(effect = EditorEffect.SearchTag(tag = intent.tag))
            is EditorIntent.OnFocusRequest -> requestFocus(path = intent.path)
            is EditorIntent.OnTaskToggle -> toggleTask(stateIndex = intent.stateIndex)
            EditorIntent.OnTaskToggleShortcut -> toggleTaskAtCursor()
            is EditorIntent.OnFormat -> format(action = intent.action)
            is EditorIntent.OnListKey -> editList(action = intent.action)
            EditorIntent.OnNoteLinkRequest -> requestPicker(effect = EditorEffect.RequestNoteLink)
            EditorIntent.OnTagRequest -> requestPicker(effect = EditorEffect.RequestTag(query = selectedForTag()))
            EditorIntent.OnTemplateRequest -> requestTemplate()
            EditorIntent.OnBookmarkToggle -> toggleBookmark()
            EditorIntent.OnFolderReveal -> revealFolder()
            is PropertyIntent -> onPropertyIntent(intent = intent)
            is EditorIntent.OnTemplateInsert -> runSave { insertTemplate(template = intent.template) }
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
            is EditorIntent.OnHeadingClick -> revealHeading(index = intent.index)
            is EditorIntent.OnDayStep -> emit(effect = EditorEffect.DayStep(step = intent.step))
            else -> onAttachmentIntent(intent = intent)
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
        link: ReadLink.Note,
        inBackground: Boolean
    ) {
        val source = state.value.notePath ?: return
        emit(
            effect = EditorEffect.OpenLink(
                source = source,
                target = link.target,
                kind = link.kind,
                inBackground = inBackground
            )
        )
    }

    private fun onBufferChanged(
        body: String
    ) {
        state.value.find?.let { find -> updateMatches(query = find.query, body = body, keepCurrent = true) }
        updateAttachments(body = body)
        updateOutline(body = body)
        updateStats(body = body)
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
        attachments.forgetFiles()
        saveNow()
        watchJob?.cancel()
        opened = null
        onDisk = null
        state.value.notePath?.let { selections[it] = buffer.selection }
        reduce {
            EditorState(
                notePath = path,
                isLoading = path != null,
                isReading = isReadingChosen,
                isPropertiesExpanded = isPropertiesExpandedChosen
            )
        }
        if (path == null) {
            replaceBuffer(body = "", isNewNote = true)
            return
        }
        when (val outcome = openNote(vaultPath = vaultPath, path = path)) {
            is Outcome.Failure -> reduce { it.copy(isLoading = false, loadError = outcome.error) }

            is Outcome.Success -> {
                show(document = outcome.value, isNewNote = true)
                restoreSelection(path = path)
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
        val toggled = if (state == MarkdownSyntax.TASK_OPEN_STATE) DONE_STATE else OPEN_STATE
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
        val selection = takePickerSelection()
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

    /** A picker is opened ([effect]) and will take the keyboard: the selection is kept for what it chooses. */
    private fun requestPicker(
        effect: EditorEffect
    ) {
        if (!isEditable()) return
        pickerSelection = buffer.selection
        emit(effect = effect)
    }

    private fun onPropertyIntent(
        intent: PropertyIntent
    ) {
        when (intent) {
            PropertyIntent.OnFoldToggle -> toggleProperties()

            PropertyIntent.OnSourceToggle -> reduce { it.copy(isSourceShown = !it.isSourceShown) }

            is PropertyIntent.OnSourceEdit -> editSourceLater(intent = intent)

            else -> {
                cancelSourceEdit()
                runSave { editProperties(intent = intent) }
            }
        }
    }

    private fun toggleProperties() {
        isPropertiesExpandedChosen = !isPropertiesExpandedChosen
        reduce { it.copy(isPropertiesExpanded = isPropertiesExpandedChosen) }
        launchGuarded(onError = ::showUnexpectedError) {
            propertyTools.savePropertiesFold(isExpanded = isPropertiesExpandedChosen)
        }
    }

    /**
     * Typed YAML in the source view is written once typing pauses, like the note's text; the next keystroke waits again.
     * Any save before that (leaving the note, Save, the text losing focus) writes it first.
     */
    private fun editSourceLater(
        intent: PropertyIntent.OnSourceEdit
    ) {
        sourceJob?.cancel()
        pendingSource = intent
        sourceJob = launchGuarded(onError = ::showUnexpectedError) {
            delay(duration = AUTOSAVE_DELAY)
            sourceJob = null
            saveLock.withLock { writePendingSource() }
        }
    }

    private suspend fun writePendingSource() {
        val intent = pendingSource ?: return
        pendingSource = null
        editProperties(intent = intent)
    }

    private fun cancelSourceEdit() {
        sourceJob?.cancel()
        sourceJob = null
        pendingSource = null
    }

    /**
     * Writes the frontmatter after a property edit at once, with the body as it is, so it is saved like any save (never
     * over an outside change); nothing when the edit changes nothing or is not one to make.
     */
    private suspend fun editProperties(
        intent: PropertyIntent
    ) {
        val path = state.value.notePath ?: return
        val document = opened ?: return
        if (state.value.isLoading || state.value.hasConflict || state.value.isDeleted) return
        if (sourcePathOf(intent = intent).let { it != null && it != path }) return
        val today = today()
        val lineBreak = if (document.usesCrLf) StringPatterns.WINDOWS_NEW_LINE else StringPatterns.NEW_LINE
        val header = propertyEditing.headerAfter(intent, document.header, state.value.properties, today, lineBreak)
        if (header == null || header == document.header) return
        write(
            path = path,
            document = document.copy(header = header, body = buffer.text.toString()),
            expectedOnDisk = onDisk
        )
    }

    /** The note a source view edit was typed in; null for the form's edits, which always concern the open note. */
    private fun sourcePathOf(
        intent: PropertyIntent
    ): RelativePath? = when (intent) {
        is PropertyIntent.OnSourceEdit -> intent.path
        is PropertyIntent.OnSourceCommit -> intent.path
        else -> null
    }

    /** How many lines of the file come before the note's text: its frontmatter's. */
    private fun linesBeforeBody(
        document: NoteDocument
    ): Int = document.header.count { it == NEW_LINE_CHAR }

    private fun today() = propertyTools.localCalendar.momentOf(instant = propertyTools.clock.now()).date

    /** The star beside the title: the window bookmarks this note, or removes its bookmark. */
    private fun toggleBookmark() {
        state.value.notePath?.let { emit(effect = EditorEffect.ToggleBookmark(path = it)) }
    }

    /** The note's folder above the title: the window shows it in the tree. */
    private fun revealFolder() {
        val folder = state.value.notePath?.parent?.takeUnless { it.isRoot } ?: return
        emit(effect = EditorEffect.RevealFolder(path = folder))
    }

    /** The template picker is opened for this note; Read mode turns to Edit first, so the template is seen going in. */
    private fun requestTemplate() {
        val current = state.value
        if (current.notePath == null || current.isLoading) return
        if (current.isReading) chooseMode(isReading = false)
        requestPicker(effect = EditorEffect.RequestTemplate)
    }

    /**
     * Fills [template] in for this note and inserts it where the picker found the selection, as one edit (undone in one
     * step); its properties merge into the note's frontmatter, which is saved at once. A template that cannot be read
     * says so in a banner.
     */
    private suspend fun insertTemplate(
        template: RelativePath
    ) {
        val path = state.value.notePath ?: return
        val document = opened ?: return
        if (!isEditable() || state.value.hasConflict || state.value.isDeleted) return
        val filled = when (val outcome = fillTemplate(vaultPath, template, title = path.nameWithoutExtension)) {
            is Outcome.Failure -> {
                reduce { it.copy(actionError = outcome.error) }
                return
            }

            is Outcome.Success -> outcome.value
        }
        val selection = takePickerSelection()
        val result = templateInsertion.insert(
            note = document.copy(body = buffer.text.toString()),
            selectionStart = selection.start,
            selectionEnd = selection.end,
            template = filled
        )
        buffer.edit {
            replace(start = result.edit.start, end = result.edit.end, text = result.edit.replacement)
            this.selection = TextRange(start = result.edit.selectionStart, end = result.edit.selectionEnd)
        }
        if (result.header != document.header) {
            write(
                path = path,
                document = document.copy(header = result.header, body = buffer.text.toString()),
                expectedOnDisk = onDisk
            )
        }
        emit(effect = EditorEffect.FocusText)
    }

    /** The selection a picker found, within the text as it is now, or the current one; the picker's is used once. */
    private fun takePickerSelection(): TextRange {
        val length = buffer.text.length
        val selection = pickerSelection?.let { TextRange(it.start.coerceAtMost(length), it.end.coerceAtMost(length)) }
            ?: buffer.selection
        pickerSelection = null
        return selection
    }

    /** The selected text as the tag picker's first search, when it is a short phrase on one line. */
    private fun selectedForTag(): String {
        val selection = buffer.selection
        val selected = buffer.text.substring(startIndex = selection.min, endIndex = selection.max).trim()
        return selected.takeIf { NEW_LINE_CHAR !in it && it.length <= MAX_TAG_QUERY }.orEmpty()
    }

    private fun onAttachmentIntent(
        intent: EditorIntent
    ) {
        when (intent) {
            EditorIntent.OnPaste -> paste()
            is EditorIntent.OnFilesDrop -> drop(paths = intent.paths)
            EditorIntent.OnAttachmentPick -> pick()
            is EditorIntent.OnAttachmentOpen -> openAttachment(path = intent.path)
            is EditorIntent.OnAttachmentLocate -> locate(embed = intent.embed)
            EditorIntent.OnActionErrorDismiss -> reduce { it.copy(actionError = null) }
            else -> Unit
        }
    }

    /** The files [body] embeds, resolved and read a moment after the last change, so typing is never held up. */
    private fun updateAttachments(
        body: String
    ) {
        val note = state.value.notePath ?: return
        attachmentsJob?.cancel()
        attachmentsJob = launchGuarded(onError = ::showUnexpectedError) {
            delay(duration = ATTACHMENTS_DELAY)
            val views = attachments.viewsOf(note = note, text = body)
            reduce { if (it.notePath == note) it.copy(attachments = views) else it }
        }
    }

    /** The headings of [body], found a moment after the last change, so typing is never held up. */
    private fun updateOutline(
        body: String
    ) {
        val note = state.value.notePath ?: return
        outlineJob?.cancel()
        outlineJob = launchGuarded(onError = ::showUnexpectedError) {
            delay(duration = OUTLINE_DELAY)
            val outline = outlineFinder.headings(text = body)
            reduce { if (it.notePath == note) it.copy(outline = outline) else it }
        }
    }

    /** Counts what the note holds for Note info once typing pauses, away from the UI thread (links are parsed). */
    private fun updateStats(
        body: String
    ) {
        val note = state.value.notePath ?: return
        statsJob?.cancel()
        statsJob = launchGuarded(onError = ::showUnexpectedError) {
            delay(duration = OUTLINE_DELAY)
            val tags = propertyTagsOf(content = state.value.properties)
            val stats = withContext(context = attachmentTools.dispatchers.default) {
                noteInfoTools.getNoteStats(body = body, propertyTags = tags)
            }
            reduce { if (it.notePath == note) it.copy(info = it.info.copy(stats = stats)) else it }
        }
    }

    /** The tags listed in the note's properties, which Note info counts with the text's. */
    private fun propertyTagsOf(
        content: FrontmatterContent
    ): List<String> = (content as? FrontmatterContent.Properties)?.properties.orEmpty()
        .filter { PropertyKeys.isTags(key = it.key) }
        .flatMap { (it.value as? PropertyValue.Items)?.items.orEmpty() }

    /** Reads when the note's file was created and modified, for Note info. */
    private fun readTimes() {
        val note = state.value.notePath ?: return
        launchGuarded(onError = ::showUnexpectedError) {
            val times = noteInfoTools.getNoteTimes(vaultPath = vaultPath, path = note)
            reduce {
                if (it.notePath != note) return@reduce it
                it.copy(
                    info = it.info.copy(createdAt = momentOf(times.createdAt), modifiedAt = momentOf(times.modifiedAt))
                )
            }
        }
    }

    private fun momentOf(
        instant: Instant?
    ): LocalMoment? = instant?.let { propertyTools.localCalendar.momentOf(instant = it) }

    /**
     * An outline heading was chosen: the editor scrolls to it and, in Edit mode, puts the cursor at the end of its line
     * and takes the keyboard, so typing goes on there.
     */
    private fun revealHeading(
        index: Int
    ) {
        val heading = state.value.outline.getOrNull(index = index) ?: return
        val text = buffer.text
        if (heading.offset > text.length) return
        if (isEditable()) {
            val lineEnd = text.indexOf(char = NEW_LINE_CHAR, startIndex = heading.offset).takeIf { it >= 0 }
                ?: text.length
            buffer.edit { selection = TextRange(index = lineEnd) }
        }
        emit(effect = EditorEffect.RevealHeading(heading = heading))
        if (isEditable()) focusText()
    }

    /** Puts the cursor (or selection) back where it was when [path] was last left, kept within its text. */
    private fun restoreSelection(
        path: RelativePath
    ) {
        val kept = selections[path] ?: return
        val length = buffer.text.length
        buffer.edit {
            selection = TextRange(start = kept.start.coerceAtMost(length), end = kept.end.coerceAtMost(length))
        }
    }

    /** Mod+V: a copied image is attached and embedded; copied text replaces the selection, as a paste does. */
    private fun paste() {
        if (!isEditable()) return
        launchGuarded(onError = ::showUnexpectedError) {
            val image =
                withContext(context = attachmentTools.dispatchers.io) { attachmentTools.clipboardReader.image() }
            when {
                // An image the clipboard cannot give is explained, instead of pasting nothing or other text.
                image is Outcome.Failure -> {
                    reduce { it.copy(actionError = image.error) }
                    return@launchGuarded
                }

                image is Outcome.Success && image.value != null -> {
                    attachAndEmbed(sources = listOf(attachments.pastedImage(bytes = image.value)))
                    return@launchGuarded
                }
            }
            val text = withContext(context = attachmentTools.dispatchers.io) { attachmentTools.clipboardReader.text() }
            text?.let { pasted ->
                buffer.edit {
                    val start = selection.min
                    replace(start = start, end = selection.max, text = pasted)
                    selection = TextRange(index = start + pasted.length)
                }
            }
        }
    }

    private fun drop(
        paths: List<String>
    ) {
        if (!isEditable() || paths.isEmpty()) return
        launchGuarded(onError = ::showUnexpectedError) {
            attachAndEmbed(sources = paths.map { AttachmentSource.File(absolutePath = it) })
        }
    }

    /** Image or file: the system's picker takes the keyboard, so the selection is kept for the embed. */
    private fun pick() {
        if (!isEditable()) return
        pickerSelection = buffer.selection
        launchGuarded(onError = ::showUnexpectedError) {
            val picked = attachmentTools.attachmentPicker.pickFile()
            if (picked == null) {
                requestFocus(path = null)
            } else {
                attachAndEmbed(sources = listOf(AttachmentSource.File(absolutePath = picked)))
            }
        }
    }

    /** Attaches every source and writes their embeds, one per line, in place of the selection; images get a width. */
    private suspend fun attachAndEmbed(
        sources: List<AttachmentSource>
    ) {
        val note = state.value.notePath ?: return
        val embeds = mutableListOf<String>()
        for (source in sources) {
            when (val attached = attachments.attach(note = note, source = source, withDefaultWidth = true)) {
                is Outcome.Success -> embeds += attached.value
                is Outcome.Failure -> reduce { it.copy(actionError = attached.error) }
            }
        }
        if (embeds.isNotEmpty()) {
            format(
                action = FormatAction.Embed(markdown = embeds.joinToString(separator = NEW_LINE))
            )
        }
    }

    private fun openAttachment(
        path: RelativePath
    ) {
        launchGuarded(onError = ::showUnexpectedError) {
            val opened = attachmentTools.fileOpener.open(vaultPath = vaultPath, path = path)
            if (opened is Outcome.Failure) reduce { it.copy(actionError = opened.error) }
        }
    }

    /** Locate…: the chosen file, copied into the vault when it is outside it, takes the missing embed's place. */
    private fun locate(
        embed: Embed
    ) {
        val note = state.value.notePath ?: return
        val written = buffer.text.toString().substring(range = embed.range)
        launchGuarded(onError = ::showUnexpectedError) {
            val picked = attachmentTools.attachmentPicker.pickFile() ?: return@launchGuarded
            val source = AttachmentSource.File(absolutePath = picked)
            when (val attached = attachments.attach(note = note, source = source, width = embed.width)) {
                is Outcome.Failure -> reduce { it.copy(actionError = attached.error) }
                is Outcome.Success -> replaceEmbed(embed = embed, written = written, replacement = attached.value)
            }
        }
    }

    /** Writes [replacement] where [embed] was, unless the text there changed meanwhile. */
    private fun replaceEmbed(
        embed: Embed,
        written: String,
        replacement: String
    ) {
        val text = buffer.text.toString()
        if (embed.range.last >= text.length || text.substring(range = embed.range) != written) return
        buffer.edit { replace(start = embed.range.first, end = embed.range.last + 1, text = replacement) }
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
        sourceJob?.cancel()
        sourceJob = null
        writePendingSource()
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
                        properties = frontmatterReader.read(header = document.header),
                        propertiesSource = propertyEditing.sourceOf(header = document.header),
                        info = it.info.copy(modifiedAt = momentOf(instant = propertyTools.clock.now())),
                        usesCrLf = document.usesCrLf,
                        bodyStartLine = linesBeforeBody(document = document),
                        today = today(),
                        saveStatus = if (isCurrent) SaveStatus.SAVED else SaveStatus.UNSAVED,
                        saveError = null,
                        hasConflict = false,
                        isDeleted = false
                    )
                }
                // The properties' tags count in Note info.
                updateStats(body = buffer.text.toString())
            }

            is Outcome.Failure -> reduceSaveFailure(error = outcome.error)
        }
    }

    private fun reduceSaveFailure(
        error: NotesError
    ) {
        logger.w { SAVE_FAILED_MESSAGE + error::class.simpleName }
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
        reduce {
            it.copy(
                properties = frontmatterReader.read(header = document.header),
                propertiesSource = propertyEditing.sourceOf(header = document.header),
                usesCrLf = document.usesCrLf,
                bodyStartLine = linesBeforeBody(document = document),
                today = today()
            )
        }
        readTimes()
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
        private val DONE_STATE = MarkdownSyntax.TASK_DONE_STATE.toString()
        private val OPEN_STATE = MarkdownSyntax.TASK_OPEN_STATE.toString()
        private const val SAVE_FAILED_MESSAGE = "Note save failed: "
        private val TASK_STATES = MarkdownSyntax.TASK_OPEN_STATE.toString() + MarkdownSyntax.TASK_DONE_STATES
        val AUTOSAVE_DELAY = 1.seconds
        private const val LOG_TAG = "EditorViewModel"
        private val ATTACHMENTS_DELAY = 150.milliseconds
        private val OUTLINE_DELAY = 150.milliseconds
        private const val MAX_TAG_QUERY = 40
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the note editor"
    }
}
