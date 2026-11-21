package app.notekea.editor.presentation.viewmodel

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.domain.KeepUnsavedTextUseCase
import app.notekea.editor.presentation.model.EditorEffect

/**
 * What the editor could not save on leaving a note or closing (an outside change, a deleted file, a failed write)
 * goes to a copy beside it ([KeepUnsavedTextUseCase]); when even that fails this pane keeps it ([takeDraft]) and shows
 * it when the note opens again, asking first when the file changed since.
 */
internal class EditorUnsaved(
    private val host: EditorHost,
    private val vaultPath: String,
    private val keepUnsavedText: KeepUnsavedTextUseCase
) {
    private val drafts = mutableMapOf<RelativePath, Draft>()
    private var lastKept: Pair<RelativePath, String>? = null

    /**
     * Keeps [document], the note at [path] with what could not be saved, written over the file's text [basedOn]; the
     * same text is kept only once.
     */
    suspend fun keep(
        path: RelativePath,
        document: NoteDocument,
        basedOn: String?
    ) {
        val text = document.toText()
        if (lastKept == path to text) return

        when (val outcome = keepUnsavedText(vaultPath = vaultPath, note = path, text = text)) {
            is Outcome.Success -> {
                lastKept = path to text

                host.emit(
                    effect = EditorEffect.UnsavedKept(
                        note = path,
                        copy = outcome.value
                    )
                )
            }

            is Outcome.Failure -> {
                drafts[path] = Draft(
                    body = document.body,
                    basedOn = basedOn
                )

                host.emit(
                    effect = EditorEffect.UnsavedKept(
                        note = path,
                        copy = null
                    )
                )
            }
        }
    }

    /**
     * The text kept in memory for [path], if any; it goes back to the editor once. When the file, now [onDisk], no
     * longer holds the text the draft was kept over, it changed outside: the conflict is raised so no save replaces it.
     */
    fun takeDraft(
        path: RelativePath,
        onDisk: String?
    ): String? {
        val draft = drafts.remove(key = path) ?: return null

        if (draft.basedOn != onDisk) {
            host.reduce { it.copy(hasConflict = true) }
        }

        return draft.body
    }

    /** A note's unsaved [body], and the file's text it was typed over. */
    private data class Draft(
        val body: String,
        val basedOn: String?
    )
}
