package app.notekea.editor.presentation.viewmodel

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.domain.NoteDiskChange
import app.notekea.editor.domain.ObserveNoteOnDiskUseCase
import app.notekea.editor.domain.OpenNoteUseCase
import app.notekea.editor.presentation.model.EditorState
import app.notekea.editor.presentation.model.SaveStatus
import kotlinx.coroutines.Job

/**
 * The open note's file, watched: the editor's own saves are recognized; an outside change reloads a note without
 * unsaved edits and otherwise asks; a deleted file offers Restore ([onDeleted] stops a pending autosave); a note moved
 * in Notekea is followed. Only a file that is really gone counts as deleted: one that cannot be read is a failed save.
 */
internal class EditorDisk(
    private val host: EditorHost,
    private val vaultPath: String,
    private val openNote: OpenNoteUseCase,
    private val observeNoteOnDisk: ObserveNoteOnDiskUseCase,
    private val saving: EditorSaving,
    private val onDeleted: () -> Unit
) {
    private var watchJob: Job? = null

    // The file's text when the pending conflict was first seen: a rewrite by the app tells its own conflict from
    // an outside change that came first by it.
    private var conflictText: String? = null

    fun watch(path: RelativePath) {
        watchJob?.cancel()

        watchJob = host.launch {
            observeNoteOnDisk(
                vaultPath = vaultPath,
                path = path
            ).collect { change ->
                saving.alone { onChange(change = change) }
            }
        }
    }

    fun stop() {
        watchJob?.cancel()
    }

    fun moveTo(path: RelativePath) {
        if (host.state.value.notePath == null) return

        host.reduce {
            it.copy(
                notePath = path,
                isDeleted = false
            )
        }

        watch(path = path)
    }

    /** Overwrites the outside change: the file's current text is what the save now expects to replace. */
    suspend fun keepMine() {
        val path = host.state.value.notePath ?: return
        val document = saving.opened ?: return

        val current = openNote(
            vaultPath = vaultPath,
            path = path
        )

        if (isUnreadable(outcome = current)) return
        val expected = (current as? Outcome.Success)?.value?.toText()

        host.reduce { it.copy(hasConflict = false) }
        conflictText = null

        saving.write(
            path = path,
            document = document.copy(
                body = host
                    .buffer
                    .text
                    .toString()
            ),
            expectedOnDisk = expected
        )
    }

    suspend fun loadTheirs() {
        val path = host.state.value.notePath ?: return

        val outcome = openNote(
            vaultPath = vaultPath,
            path = path
        )

        if (isUnreadable(outcome = outcome)) return

        when (outcome) {
            is Outcome.Failure -> host.reduce {
                it.copy(
                    hasConflict = false,
                    isDeleted = true
                )
            }

            is Outcome.Success -> {
                saving.show(
                    document = outcome.value,
                    isNewNote = false
                )

                conflictText = null

                host.reduce {
                    it.copy(
                        hasConflict = false,
                        saveStatus = SaveStatus.NONE,
                        saveError = null
                    )
                }
            }
        }
    }

    /**
     * Notekea itself rewrote the open note at [path] (a rename updated its links): the editor takes the new text, or
     * keeps its own edits on top of it, so the next save is no conflict; one the watcher raised on other text stays.
     */
    suspend fun followRewrite(path: RelativePath) {
        if (host.state.value.notePath != path) return

        val document = (
            openNote(
                vaultPath = vaultPath,
                path = path
            ) as? Outcome.Success
            )?.value ?: return

        val isOtherConflict = host.state.value.hasConflict && conflictText != document.toText()
        // An outside change is still waiting for an answer: taking the rewrite would drop it unseen, and the
        // next save would undo both, so the question stays.
        if (isOtherConflict) return

        if (saving.hasEdits()) {
            saving.rebase(document = document)
        } else {
            saving.show(
                document = document,
                isNewNote = false
            )
        }

        conflictText = null
        host.reduce { it.copy(hasConflict = false) }
    }

    /**
     * Brings back a file that is gone; one that came back meanwhile is replaced only if it holds what was last seen.
     */
    suspend fun restore() {
        val path = host.state.value.notePath ?: return
        val document = saving.opened ?: return

        val current = openNote(
            vaultPath = vaultPath,
            path = path
        )

        if (isUnreadable(outcome = current)) return
        val isBack = current is Outcome.Success

        if (isBack) {
            host.reduce { it.copy(isDeleted = false) }
        }

        saving.write(
            path = path,
            document = document.copy(
                body = host
                    .buffer
                    .text
                    .toString()
            ),
            expectedOnDisk = if (isBack) saving.onDisk else null
        )
    }

    /** Whether [outcome] failed with the file still there: that is shown as a failed save. */
    private fun isUnreadable(outcome: Outcome<NoteDocument, NotesError>): Boolean {
        val error = (outcome as? Outcome.Failure)?.error
        if (error == null || error is NotesError.EntryMissing) return false

        host.reduce { it.copy(saveError = error) }

        return true
    }

    /**
     * The editor's own saves are recognized and ignored; anything else reloads or asks. A change can also be the file
     * read before the editor's last save and reported after it (quick saves, a busy computer): the file is read again,
     * and one that holds the last save is no outside change.
     */
    private suspend fun onChange(change: NoteDiskChange) {
        when (change) {
            NoteDiskChange.Deleted -> {
                onDeleted()

                host.reduce { it.copy(isDeleted = true) }
            }

            is NoteDiskChange.Changed -> {
                val isSameText = change.text == saving.onDisk && !host.state.value.isDeleted
                if (isSameText) return
                if (holdsLastSave()) return

                val hasUnsavedWork = saving.hasEdits() || host.state.value.hasConflict

                if (hasUnsavedWork) {
                    // A conflict a failed save raised has no text yet: the watcher's first look gives it one.
                    val isFirstLook = !host.state.value.hasConflict || conflictText == null
                    if (isFirstLook) conflictText = change.text

                    host.reduce {
                        it.copy(
                            hasConflict = true,
                            isDeleted = false
                        )
                    }
                } else {
                    saving.show(
                        document = NoteDocument.fromText(text = change.text),
                        isNewNote = false
                    )

                    host.reduce { it.copy(isDeleted = false) }
                }
            }
        }
    }

    private suspend fun holdsLastSave(): Boolean {
        val path = host.state.value.notePath ?: return false
        if (host.state.value.isDeleted) return false

        val current = (
            openNote(
                vaultPath = vaultPath,
                path = path
            ) as? Outcome.Success
            )?.value?.toText()

        return current != null && current == saving.onDisk
    }
}
