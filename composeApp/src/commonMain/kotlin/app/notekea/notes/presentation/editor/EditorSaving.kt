package app.notekea.notes.presentation.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import app.notekea.notes.domain.FrontmatterReader
import app.notekea.notes.domain.NoteDocument
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SaveNoteUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The open note as it is on disk, and the one way to write it (`ROADMAP.md` 1.5): [opened] is the note as last read or
 * written (its header kept apart), [onDisk] what its file holds, so a save never replaces an outside change; saves,
 * loads and answers to a conflict run one at a time ([runSave]). A note shown or saved updates what the state says
 * about its file: its properties, line endings and where its text starts.
 */
internal class EditorSaving(
    private val host: EditorHost,
    private val vaultPath: String,
    private val saveNote: SaveNoteUseCase,
    private val noteInfo: EditorNoteInfo
) {
    var opened: NoteDocument? = null
    var onDisk: String? = null

    val propertyEditing = PropertyEditing()
    private val frontmatterReader = FrontmatterReader()
    private val lock = Mutex()
    private val logger = Logger.withTag(tag = LOG_TAG)

    /** Runs [action] alone: saves, loads and answers to a conflict never interleave. */
    fun runSave(
        action: suspend () -> Unit
    ) = host.launch { lock.withLock { action() } }

    /** Runs [action] alone from a coroutine already started (an autosave, a change on disk). */
    suspend fun alone(
        action: suspend () -> Unit
    ) = lock.withLock { action() }

    /** Whether the text holds edits the file does not. */
    fun hasEdits(): Boolean = host.buffer.text.toString() != opened?.body

    suspend fun write(
        path: RelativePath,
        document: NoteDocument,
        expectedOnDisk: String?
    ) {
        when (val outcome = saveNote(vaultPath, path, document, expectedOnDisk)) {
            is Outcome.Success -> {
                // Note info counts the properties' tags: a save that changed them counts again; the text's own
                // changes are counted as they are typed.
                val isHeaderChanged = opened?.header != document.header
                opened = document
                onDisk = outcome.value
                val isCurrent = host.buffer.text.toString() == document.body
                host.reduce {
                    describeFile(state = it, document = document).copy(
                        info = it.info.copy(modifiedAt = noteInfo.now()),
                        saveStatus = if (isCurrent) SaveStatus.SAVED else SaveStatus.UNSAVED,
                        saveError = null,
                        hasConflict = false,
                        isDeleted = false
                    )
                }
                if (isHeaderChanged) noteInfo.updateStats(body = host.buffer.text.toString())
            }

            is Outcome.Failure -> reduceFailure(error = outcome.error)
        }
    }

    /** Shows [document] as the saved state of the note; a new note starts at the top with a fresh undo history. */
    fun show(
        document: NoteDocument,
        isNewNote: Boolean
    ) {
        opened = document
        onDisk = document.toText()
        host.reduce { describeFile(state = it, document = document) }
        noteInfo.readTimes()
        replaceBuffer(body = document.body, isNewNote = isNewNote)
    }

    /** Forgets the open note's file before another opens. */
    fun forget() {
        opened = null
        onDisk = null
    }

    // Clearing the undo history is still experimental; without it, Undo would bring back the previous note's text.
    @OptIn(ExperimentalFoundationApi::class)
    fun replaceBuffer(
        body: String,
        isNewNote: Boolean
    ) {
        val buffer = host.buffer
        val cursor = buffer.selection.start.coerceAtMost(maximumValue = body.length)
        buffer.edit {
            replace(start = 0, end = length, text = body)
            if (isNewNote) placeCursorBeforeCharAt(index = 0) else placeCursorBeforeCharAt(index = cursor)
        }
        if (isNewNote) buffer.undoState.clearHistory()
    }

    /** What [state] says about the file of [document]: its properties, line endings, where its text starts, today. */
    private fun describeFile(
        state: EditorState,
        document: NoteDocument
    ): EditorState = state.copy(
        properties = frontmatterReader.read(header = document.header),
        propertiesSource = propertyEditing.sourceOf(header = document.header),
        usesCrLf = document.usesCrLf,
        bodyStartLine = document.header.count { it == NEW_LINE_CHAR },
        today = noteInfo.today()
    )

    private fun reduceFailure(
        error: NotesError
    ) {
        logger.w { SAVE_FAILED_MESSAGE + error::class.simpleName }
        host.reduce {
            when (error) {
                is NotesError.ChangedOnDisk -> it.copy(hasConflict = true)
                is NotesError.EntryMissing -> it.copy(isDeleted = true)
                else -> it.copy(saveError = error)
            }
        }
    }

    companion object {
        private const val LOG_TAG = "EditorSaving"
        private const val SAVE_FAILED_MESSAGE = "Note save failed: "
    }
}
