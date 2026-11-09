package app.notekea.editor.presentation.viewmodel

import app.notekea.core.domain.Outcome
import app.notekea.editor.presentation.model.EditorState
import app.notekea.editor.presentation.model.SaveStatus
import app.notekea.editor.domain.NoteDiskChange
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.NotesError
import app.notekea.editor.domain.ObserveNoteOnDiskUseCase
import app.notekea.editor.domain.OpenNoteUseCase
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.Job

/**
 * The open note's file, watched (`ROADMAP.md` 1.5): the editor's own saves are recognized; an outside change reloads a
 * note without unsaved edits and otherwise asks (Keep mine overwrites it, Load theirs drops the edits); a deleted file
 * offers Restore ([onDeleted] stops a pending autosave); a note moved in Notekea is followed. Only a file that is
 * really gone counts as deleted: one that cannot be read is a failed save, and an answer never replaces it.
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

    fun watch(
        path: RelativePath
    ) {
        watchJob?.cancel()
        watchJob = host.launch {
            observeNoteOnDisk(vaultPath = vaultPath, path = path).collect { change ->
                saving.alone { onChange(change = change) }
            }
        }
    }

    fun stop() {
        watchJob?.cancel()
    }

    fun moveTo(
        path: RelativePath
    ) {
        if (host.state.value.notePath == null) return
        host.reduce { it.copy(notePath = path, isDeleted = false) }
        watch(path = path)
    }

    /** Overwrites the outside change: the file's current text is what the save now expects to replace. */
    suspend fun keepMine() {
        val path = host.state.value.notePath ?: return
        val document = saving.opened ?: return
        val current = openNote(vaultPath = vaultPath, path = path)
        if (isUnreadable(outcome = current)) return
        val expected = (current as? Outcome.Success)?.value?.toText()
        host.reduce { it.copy(hasConflict = false) }
        saving.write(
            path = path,
            document = document.copy(body = host.buffer.text.toString()),
            expectedOnDisk = expected
        )
    }

    suspend fun loadTheirs() {
        val path = host.state.value.notePath ?: return
        val outcome = openNote(vaultPath = vaultPath, path = path)
        if (isUnreadable(outcome = outcome)) return
        when (outcome) {
            is Outcome.Failure -> host.reduce { it.copy(hasConflict = false, isDeleted = true) }

            is Outcome.Success -> {
                saving.show(document = outcome.value, isNewNote = false)
                host.reduce { it.copy(hasConflict = false, saveStatus = SaveStatus.NONE, saveError = null) }
            }
        }
    }

    /**
     * Notekea itself rewrote the open note at [path] (a rename updated its links to itself): the editor takes the new
     * text, or with edits of its own keeps them on top of it, so the next save is no conflict, nor is a change the
     * watcher already took for an outside one.
     */
    suspend fun followRewrite(
        path: RelativePath
    ) {
        if (host.state.value.notePath != path) return
        val document = (openNote(vaultPath = vaultPath, path = path) as? Outcome.Success)?.value ?: return
        if (saving.hasEdits()) {
            saving.rebase(
                document = document
            )
        } else {
            saving.show(document = document, isNewNote = false)
        }
        host.reduce { it.copy(hasConflict = false) }
    }

    /** Brings back a file that is gone; one that came back meanwhile is replaced only if it holds what was last seen. */
    suspend fun restore() {
        val path = host.state.value.notePath ?: return
        val document = saving.opened ?: return
        val current = openNote(vaultPath = vaultPath, path = path)
        if (isUnreadable(outcome = current)) return
        val isBack = current is Outcome.Success
        if (isBack) host.reduce { it.copy(isDeleted = false) }
        saving.write(
            path = path,
            document = document.copy(body = host.buffer.text.toString()),
            expectedOnDisk = if (isBack) saving.onDisk else null
        )
    }

    /** Whether [outcome] failed with the file still there: that is shown as a failed save. */
    private fun isUnreadable(
        outcome: Outcome<NoteDocument, NotesError>
    ): Boolean {
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
    private suspend fun onChange(
        change: NoteDiskChange
    ) {
        when (change) {
            NoteDiskChange.Deleted -> {
                onDeleted()
                host.reduce { it.copy(isDeleted = true) }
            }

            is NoteDiskChange.Changed -> {
                if (change.text == saving.onDisk && !host.state.value.isDeleted) return
                if (holdsLastSave()) return
                if (saving.hasEdits() || host.state.value.hasConflict) {
                    host.reduce { it.copy(hasConflict = true, isDeleted = false) }
                } else {
                    saving.show(document = NoteDocument.fromText(text = change.text), isNewNote = false)
                    host.reduce { it.copy(isDeleted = false) }
                }
            }
        }
    }

    private suspend fun holdsLastSave(): Boolean {
        val path = host.state.value.notePath ?: return false
        if (host.state.value.isDeleted) return false
        val current = (openNote(vaultPath = vaultPath, path = path) as? Outcome.Success)?.value?.toText()
        return current != null && current == saving.onDisk
    }
}
