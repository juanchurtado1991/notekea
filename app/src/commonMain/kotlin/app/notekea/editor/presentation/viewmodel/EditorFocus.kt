package app.notekea.editor.presentation.viewmodel

import androidx.compose.ui.text.TextRange
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.presentation.model.EditorEffect
import app.notekea.editor.presentation.model.EditorState

/**
 * Where the keyboard and the cursor go in the editor: a focus request gives the keyboard to the text once its note is
 * open, each note left in this pane gets its cursor back when it opens again, an outline heading chosen is scrolled to,
 * and a mention or passage chosen elsewhere is selected once its note opens.
 */
internal class EditorFocus(
    private val host: EditorHost,
    private val textEdits: EditorTextEdits
) {
    // Where the cursor was in each note left in this pane, so going back to its tab puts it there again.
    private val selections = mutableMapOf<RelativePath, TextRange>()

    // A note whose text takes the keyboard as soon as it finishes opening; dropped if another note opens instead.
    private var pending: RelativePath? = null

    // A place of a note to select once it opens: a mention chosen in Connections, dropped if another note opens
    // instead.
    private var revealed: Pair<RelativePath, IntRange>? = null

    /** Another note ([path]) is about to open: the open one's cursor is kept, and a request for a third one dropped. */
    fun leave(path: RelativePath?) {
        if (path != pending) pending = null
        if (path != revealed?.first) revealed = null
        textEdits.releasePickerSelection(isCancelled = false)

        host
            .state
            .value
            .notePath
            ?.let { selections[it] = host.buffer.selection }
    }

    /** [path] is open: its cursor comes back, and its text takes the keyboard when that was asked for. */
    fun arrive(path: RelativePath) {
        selections[path]?.let { kept ->
            val length = host.buffer.text.length

            host.buffer.edit {
                selection = TextRange(
                    start = kept.start.coerceAtMost(length),
                    end = kept.end.coerceAtMost(length)
                )
            }
        }
    }

    /** Called once the open note is shown: the text takes the keyboard when a request waited for it. */
    fun onShown(path: RelativePath) {
        revealed?.takeIf {
            it.first == path
        }?.let { (_, range) -> select(range = range) }

        if (pending == path) focusText()
    }

    /** [range] of [path]'s body is selected, so the text scrolls to it, now if the note is open or once it opens. */
    fun reveal(
        path: RelativePath,
        range: IntRange
    ) {
        textEdits.releasePickerSelection(isCancelled = false)

        val isOpen = host
            .state
            .value
            .let {
                it.notePath == path &&
                    !it.isLoading &&
                    it.loadError == null
            }

        if (isOpen) {
            select(range = range)
            focusText()
        } else {
            revealed = path to range
            pending = path
        }
    }

    private fun select(range: IntRange) {
        revealed = null

        host.reduce { it.copy(isRevealing = true) }

        val length = host.buffer.text.length

        host.buffer.edit {
            selection = TextRange(
                start = range.first.coerceAtMost(length),
                end = (range.last + 1).coerceAtMost(length)
            )
        }
    }

    /** The text of [path] (the open note when null) takes the keyboard now if it is open, or once it opens. */
    fun request(path: RelativePath?) {
        textEdits.releasePickerSelection(isCancelled = path == null)
        val target = path ?: host.state.value.notePath ?: return

        val isOpen = host
            .state
            .value
            .let {
                it.notePath == target &&
                    !it.isLoading &&
                    it.loadError == null
            }

        if (isOpen) focusText() else pending = target
    }

    /**
     * An outline heading was chosen: the editor scrolls to it and puts the cursor at the end of its line
     * and takes the keyboard, so typing goes on there.
     */
    fun revealHeading(index: Int) {
        val heading = host
            .state
            .value
            .outline
            .getOrNull(index = index) ?: return

        val text = host.buffer.text
        if (heading.offset > text.length) return
        val isEditable = host.state.value.isEditable

        if (isEditable) {
            val lineEnd = text.indexOf(
                char = NEW_LINE_CHAR,
                startIndex = heading.offset
            ).takeIf {
                it >= 0
            }
                ?: text.length

            host.buffer.edit { selection = TextRange(index = lineEnd) }
        }

        host.emit(effect = EditorEffect.RevealHeading(heading = heading))
        if (isEditable) focusText()
    }

    private fun focusText() {
        pending = null
        host.emit(effect = EditorEffect.FocusText)
    }
}
