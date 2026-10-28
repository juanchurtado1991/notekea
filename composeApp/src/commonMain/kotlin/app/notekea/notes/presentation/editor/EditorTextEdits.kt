package app.notekea.notes.presentation.editor

import androidx.compose.ui.text.TextRange
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.notes.domain.FormatAction
import app.notekea.notes.domain.ListAction
import app.notekea.notes.domain.ListEditing
import app.notekea.notes.domain.MarkdownFormatter
import app.notekea.notes.domain.TaskFinder

/**
 * The edits the editor makes to the text itself, each one edit undone in one step and saved like typing: a task
 * checked or unchecked (`ROADMAP.md` 1.7), a toolbar format (`ROADMAP.md` 1.17), a key in a list, and the selection kept
 * while a picker (a note to link, a tag, a template, a file) has the keyboard, so what it chooses goes there.
 */
internal class EditorTextEdits(
    private val host: EditorHost
) {
    private val taskFinder = TaskFinder()
    private val markdownFormatter = MarkdownFormatter()
    private val listEditing = ListEditing()

    // The selection when a picker took the keyboard, which clears it in the text field: what is chosen formats it,
    // and closing the picker puts it back.
    private var pickerSelection: TextRange? = null

    /**
     * Checks or unchecks the task whose state is at [stateIndex], by editing the text like a keystroke would, so it is
     * saved as any edit and can be undone. Nothing happens when that character is no longer a task's state.
     */
    fun toggleTask(
        stateIndex: Int
    ) {
        val state = host.buffer.text.getOrNull(index = stateIndex) ?: return
        if (state !in TASK_STATES) return
        val toggled = if (state == MarkdownSyntax.TASK_OPEN_STATE) DONE_STATE else OPEN_STATE
        host.buffer.edit { replace(start = stateIndex, end = stateIndex + 1, text = toggled) }
    }

    fun toggleTaskAtCursor() {
        if (host.state.value.isReading || host.state.value.notePath == null) return
        val text = host.buffer.text.toString()
        taskFinder.taskAt(text = text, offset = host.buffer.selection.start)?.let {
            toggleTask(stateIndex = it.stateIndex)
        }
    }

    /**
     * Applies a toolbar [action] to the selection as one edit (undone in one step, saved like typing), selects what it
     * says, and gives the keyboard back to the text, which the toolbar button took.
     */
    fun format(
        action: FormatAction
    ) {
        if (!host.state.value.isEditable) return
        val selection = takePickerSelection()
        val edit = markdownFormatter.format(
            text = host.buffer.text.toString(),
            selectionStart = selection.start,
            selectionEnd = selection.end,
            action = action
        )
        host.buffer.edit {
            replace(start = edit.start, end = edit.end, text = edit.replacement)
            this.selection = TextRange(start = edit.selectionStart, end = edit.selectionEnd)
        }
        host.emit(effect = EditorEffect.FocusText)
    }

    /** A key in a list item, as in other editors: the edit replaces what the key would have typed. */
    fun editList(
        action: ListAction
    ) {
        if (!host.state.value.isEditable) return
        val selection = host.buffer.selection
        val edit = listEditing.edit(
            text = host.buffer.text.toString(),
            selectionStart = selection.start,
            selectionEnd = selection.end,
            action = action
        ) ?: return
        host.buffer.edit {
            replace(start = edit.start, end = edit.end, text = edit.replacement)
            this.selection = TextRange(start = edit.selectionStart, end = edit.selectionEnd)
        }
    }

    /** A picker is opened ([effect]) and will take the keyboard: the selection is kept for what it chooses. */
    fun requestPicker(
        effect: EditorEffect
    ) {
        if (!host.state.value.isEditable) return
        keepSelectionForPicker()
        host.emit(effect = effect)
    }

    /** Keeps the selection for a picker that takes the keyboard (the system's file picker). */
    fun keepSelectionForPicker() {
        pickerSelection = host.buffer.selection
    }

    /** The selection a picker found, within the text as it is now, or the current one; the picker's is used once. */
    fun takePickerSelection(): TextRange {
        val length = host.buffer.text.length
        val selection = pickerSelection?.let { TextRange(it.start.coerceAtMost(length), it.end.coerceAtMost(length)) }
            ?: host.buffer.selection
        pickerSelection = null
        return selection
    }

    /** A picker closed without a choice ([isCancelled]): the text gets back the selection it had; else it is dropped. */
    fun releasePickerSelection(
        isCancelled: Boolean
    ) {
        pickerSelection?.takeIf { isCancelled && it.max <= host.buffer.text.length }?.let { kept ->
            host.buffer.edit { selection = kept }
        }
        pickerSelection = null
    }

    /** The selected text as the tag picker's first search, when it is a short phrase on one line. */
    fun selectedForTag(): String {
        val selection = host.buffer.selection
        val selected = host.buffer.text.substring(startIndex = selection.min, endIndex = selection.max).trim()
        return selected.takeIf { NEW_LINE_CHAR !in it && it.length <= MAX_TAG_QUERY }.orEmpty()
    }

    companion object {
        private val DONE_STATE = MarkdownSyntax.TASK_DONE_STATE.toString()
        private const val MAX_TAG_QUERY = 40
        private val OPEN_STATE = MarkdownSyntax.TASK_OPEN_STATE.toString()
        private val TASK_STATES = MarkdownSyntax.TASK_OPEN_STATE.toString() + MarkdownSyntax.TASK_DONE_STATES
    }
}
