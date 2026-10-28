package app.notekea.notes.presentation.editor

import androidx.compose.ui.text.TextRange
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.FillTemplateUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TemplateInsertion

/**
 * Insert template (`ROADMAP.md` 1.12): the template picker opens for the open note, Read mode turning to Edit first
 * ([onEditMode]) so the template is seen going in, and the chosen template is filled in and inserted where the
 * selection was, its properties merged into the note's frontmatter.
 */
internal class EditorTemplates(
    private val host: EditorHost,
    private val vaultPath: String,
    private val fillTemplate: FillTemplateUseCase,
    private val saving: EditorSaving,
    private val textEdits: EditorTextEdits,
    private val onEditMode: () -> Unit
) {
    private val templateInsertion = TemplateInsertion()

    fun request() {
        val current = host.state.value
        if (current.notePath == null || current.isLoading) return
        if (current.isReading) onEditMode()
        textEdits.requestPicker(effect = EditorEffect.RequestTemplate)
    }

    /**
     * Fills [template] in for this note and inserts it where the picker found the selection, as one edit (undone in one
     * step); its properties merge into the note's frontmatter, which is saved at once. A template that cannot be read
     * says so in a banner.
     */
    suspend fun insert(
        template: RelativePath
    ) {
        val state = host.state.value
        val path = state.notePath ?: return
        val document = saving.opened ?: return
        if (!state.isEditable || state.hasConflict || state.isDeleted) return
        val filled = when (val outcome = fillTemplate(vaultPath, template, title = path.nameWithoutExtension)) {
            is Outcome.Failure -> {
                host.reduce { it.copy(actionError = outcome.error) }
                return
            }

            is Outcome.Success -> outcome.value
        }
        val selection = textEdits.takePickerSelection()
        val result = templateInsertion.insert(
            note = document.copy(body = host.buffer.text.toString()),
            selectionStart = selection.start,
            selectionEnd = selection.end,
            template = filled
        )
        host.buffer.edit {
            replace(start = result.edit.start, end = result.edit.end, text = result.edit.replacement)
            this.selection = TextRange(start = result.edit.selectionStart, end = result.edit.selectionEnd)
        }
        if (result.header != document.header) {
            val written = document.copy(header = result.header, body = host.buffer.text.toString())
            saving.write(path = path, document = written, expectedOnDisk = saving.onDisk)
        }
        host.emit(effect = EditorEffect.FocusText)
    }
}
