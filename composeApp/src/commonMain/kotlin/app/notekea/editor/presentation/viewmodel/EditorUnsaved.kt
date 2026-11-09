package app.notekea.editor.presentation.viewmodel

import app.notekea.core.domain.Outcome
import app.notekea.editor.presentation.model.EditorEffect
import app.notekea.editor.domain.KeepUnsavedTextUseCase
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.RelativePath

/**
 * What the editor could not save when it leaves a note or the window closes: an outside change waiting for an answer,
 * a deleted file or a failed write. It goes to a copy beside the note ([KeepUnsavedTextUseCase]), and the tree says so;
 * when even that cannot be written, this pane keeps the text ([takeDraft]) and shows it when the note opens again.
 */
internal class EditorUnsaved(
    private val host: EditorHost,
    private val vaultPath: String,
    private val keepUnsavedText: KeepUnsavedTextUseCase
) {
    private val drafts = mutableMapOf<RelativePath, String>()
    private var lastKept: Pair<RelativePath, String>? = null

    /** Keeps [document], the note at [path] with what could not be saved; the same text is kept only once. */
    suspend fun keep(
        path: RelativePath,
        document: NoteDocument
    ) {
        val text = document.toText()
        if (lastKept == path to text) return
        when (val outcome = keepUnsavedText(vaultPath = vaultPath, note = path, text = text)) {
            is Outcome.Success -> {
                lastKept = path to text
                host.emit(effect = EditorEffect.UnsavedKept(note = path, copy = outcome.value))
            }

            is Outcome.Failure -> {
                drafts[path] = document.body
                host.emit(effect = EditorEffect.UnsavedKept(note = path, copy = null))
            }
        }
    }

    /** The text kept in memory for [path], if any; it goes back to the editor once. */
    fun takeDraft(
        path: RelativePath
    ): String? = drafts.remove(key = path)
}
