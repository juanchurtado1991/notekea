package app.notekea.editor.presentation.view

import androidx.compose.foundation.text.input.delete
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.ShortcutModifier
import app.notekea.core.presentation.platform.matches
import app.notekea.editor.domain.ListAction
import app.notekea.editor.domain.ListEditing
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.toolbar.FormatActions

/**
 * The keys pressed in the text that the editor handles itself: the formatting shortcuts and the list keys, sent as
 * intents, and the keys that leave the blocks the text field shows: at the window's edges an arrow stops and
 * Backspace deletes nothing, so those keys move the cursor, or delete, across the edge here.
 */
internal class EditorKeys(
    private val page: EditorPageState,
    private val platformConventions: PlatformConventions,
    private val formats: FormatActions,
    private val onIntent: (EditorIntent) -> Unit
) {
    private val listEditing = ListEditing()
    private val isMac = platformConventions.primaryModifier == ShortcutModifier.COMMAND

    /** False for the text field to handle the key. */
    fun onKey(event: KeyEvent): Boolean {
        val intent = formatShortcut(event = event) ?: listKey(event = event)

        if (intent != null) {
            onIntent(intent)

            return true
        }

        if (event.type != KeyEventType.KeyDown) return false

        return moveAcross(event = event) || deleteAcross(event = event)
    }

    /** The toolbar action of a formatting shortcut (bold, italic, links), or null for any other key. */
    private fun formatShortcut(event: KeyEvent): EditorIntent? = when {
        platformConventions.boldShortcut.matches(event = event) ->
            EditorIntent.OnFormat(action = formats.bold)

        platformConventions.italicShortcut.matches(event = event) ->
            EditorIntent.OnFormat(action = formats.italic)

        platformConventions.noteLinkShortcut.matches(event = event) ->
            EditorIntent.OnNoteLinkRequest

        platformConventions.webLinkShortcut.matches(event = event) ->
            EditorIntent.OnFormat(action = formats.webLink)

        // A copied image is attached; copied text is pasted by the editor too, so both behave the same.
        platformConventions.pasteShortcut.matches(event = event) -> EditorIntent.OnPaste

        else -> null
    }

    /**
     * Enter, Tab, Shift+Tab or Backspace where they edit a list (`ListEditing`); null where the key types as usual, so
     * the text field handles it.
     */
    private fun listKey(event: KeyEvent): EditorIntent? {
        val isIgnored =
            event.type != KeyEventType.KeyDown ||
                event.isCtrlPressed ||
                event.isMetaPressed ||
                event.isAltPressed

        if (isIgnored) return null

        val action = when (event.key) {
            Key.Enter, Key.NumPadEnter -> ListAction.NEW_ITEM.takeUnless { event.isShiftPressed }
            Key.Tab -> if (event.isShiftPressed) ListAction.OUTDENT else ListAction.INDENT
            Key.Backspace -> ListAction.REMOVE_MARKER.takeUnless { event.isShiftPressed }
            else -> null
        } ?: return null

        val selection = page.buffer.selection

        val edit = listEditing.edit(
            text = page
                .buffer
                .text
                .toString(),
            selectionStart = selection.start,
            selectionEnd = selection.end,
            action = action
        )

        return edit?.let { EditorIntent.OnListKey(action = action) }
    }

    /** Select all, and the keys that move the cursor out of the shown blocks; Shift selects up to there. */
    private fun moveAcross(event: KeyEvent): Boolean {
        val buffer = page.buffer
        val isMod = if (isMac) event.isMetaPressed else event.isCtrlPressed

        if (isMod && event.key == Key.A) {
            buffer.edit {
                selection = TextRange(
                    start = 0,
                    end = length
                )
            }

            return true
        }

        val target = target(
            event = event,
            isMod = isMod
        ) ?: return false

        buffer.edit {
            selection = TextRange(
                start = if (event.isShiftPressed) selection.start else target,
                end = target
            )
        }

        return true
    }

    /** Where the cursor goes when the key takes it out of the shown blocks; null when it stays in them. */
    private fun target(
        event: KeyEvent,
        isMod: Boolean
    ): Int? {
        val selection = page.buffer.selection
        val length = page.buffer.text.length
        val shown = page.items.shown

        val before = (shown.min - 1).takeIf {
            it >= 0
        }

        val after = (shown.max + 1).takeIf {
            it <= length
        }

        val isPlain =
            !event.isCtrlPressed &&
                !event.isAltPressed &&
                !event.isMetaPressed

        val key = event.key

        return when {
            isMod && (key == Key.MoveHome || (isMac && key == Key.DirectionUp)) -> 0

            isMod && (key == Key.MoveEnd || (isMac && key == Key.DirectionDown)) -> length

            // Without Shift, an arrow first puts the cursor at the selection's end: the field does that.
            !selection.collapsed && !event.isShiftPressed -> null

            key == Key.DirectionLeft &&
                !event.isMetaPressed &&
                selection.end == shown.min -> before

            key == Key.DirectionRight &&
                !event.isMetaPressed &&
                selection.end == shown.max -> after

            isPlain &&
                key == Key.DirectionUp &&
                isOnLine(isLast = false) -> before

            isPlain &&
                key == Key.DirectionDown &&
                isOnLine(isLast = true) -> after

            else -> null
        }
    }

    /** Whether the cursor is on the first line the field shows, or on the last one. */
    private fun isOnLine(isLast: Boolean): Boolean {
        val layout = page.currentLayout() ?: return false

        val line =
            layout.getLineForOffset(offset = page.shown.toShown(offset = page.buffer.selection.end))

        return line == if (isLast) layout.lineCount - 1 else 0
    }

    /** Backspace at the start of the shown blocks and Delete at their end remove the line break across the edge. */
    private fun deleteAcross(event: KeyEvent): Boolean {
        val selection = page.buffer.selection
        val shown = page.items.shown

        val isPlain =
            !event.isCtrlPressed &&
                !event.isAltPressed &&
                !event.isMetaPressed

        val start = when {
            !selection.collapsed || !isPlain -> null
            event.key == Key.Backspace && selection.end == shown.min -> shown.min - 1
            event.key == Key.Delete && selection.end == shown.max -> shown.max
            else -> null
        }

        if (start == null ||
            start < 0 ||
            start >= page.buffer.text.length
        ) {
            return false
        }

        page.buffer.edit {
            delete(
                start = start,
                end = start + 1
            )

            this.selection = TextRange(index = start)
        }

        return true
    }
}
