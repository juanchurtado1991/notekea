package app.notekea.notes.presentation.editor

import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NoteDiskChange
import app.notekea.notes.domain.NoteDocument
import app.notekea.notes.domain.ObserveNoteOnDiskUseCase
import app.notekea.notes.domain.OpenNoteUseCase
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.Job

/**
 * The open note's file, watched (`ROADMAP.md` 1.5): the editor's own saves are recognized; an outside change reloads a
 * note without unsaved edits and otherwise asks (Keep mine overwrites it, Load theirs drops the edits); a deleted file
 * offers Restore ([onDeleted] stops a pending autosave); a note moved in Notekea is followed.
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
        when (val outcome = openNote(vaultPath = vaultPath, path = path)) {
            is Outcome.Failure -> host.reduce { it.copy(hasConflict = false, isDeleted = true) }

            is Outcome.Success -> {
                saving.show(document = outcome.value, isNewNote = false)
                host.reduce { it.copy(hasConflict = false, saveStatus = SaveStatus.NONE, saveError = null) }
            }
        }
    }

    suspend fun restore() {
        val path = host.state.value.notePath ?: return
        val document = saving.opened ?: return
        saving.write(path = path, document = document.copy(body = host.buffer.text.toString()), expectedOnDisk = null)
    }

    /** The editor's own saves are recognized and ignored; anything else reloads or asks. */
    private fun onChange(
        change: NoteDiskChange
    ) {
        when (change) {
            NoteDiskChange.Deleted -> {
                onDeleted()
                host.reduce { it.copy(isDeleted = true) }
            }

            is NoteDiskChange.Changed -> {
                if (change.text == saving.onDisk && !host.state.value.isDeleted) return
                if (saving.hasEdits() || host.state.value.hasConflict) {
                    host.reduce { it.copy(hasConflict = true, isDeleted = false) }
                } else {
                    saving.show(document = NoteDocument.fromText(text = change.text), isNewNote = false)
                    host.reduce { it.copy(isDeleted = false) }
                }
            }
        }
    }
}
