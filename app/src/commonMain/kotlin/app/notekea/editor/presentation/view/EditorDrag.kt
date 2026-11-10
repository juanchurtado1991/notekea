package app.notekea.editor.presentation.view

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import app.notekea.editor.presentation.view.EditorPageState.Companion.WINDOW_KEY

/**
 * On the editor's list: a selection dragged out of the text field goes on over the blocks drawn around it. The field
 * selects inside itself; once the pointer leaves it, the list takes the drag (before the field sees it, which ends the
 * field's own) and selects from where the drag started to the block under the pointer, which the field then shows.
 */
internal fun Modifier.selectAcrossBlocks(page: EditorPageState): Modifier = pointerInput(page) {
    awaitEachGesture {
        val down = awaitFirstDown(
            requireUnconsumed = false,
            pass = PointerEventPass.Initial
        )

        val isOnField = page.fieldY(position = down.position)?.let {
            page.isInField(y = it)
        } == true

        val isOnToolbar = page.isOnToolbar(y = down.position.y)

        if (!isOnField || isOnToolbar) return@awaitEachGesture

        var anchor: Int? = null

        while (true) {
            val change = awaitPointerEvent(pass = PointerEventPass.Initial)
                .changes
                .firstOrNull {
                    it.id == down.id
                }
                ?.takeIf { it.pressed } ?: break

            val isInside = page.fieldY(position = change.position)?.let {
                page.isInField(y = it)
            } == true

            if (anchor == null && isInside) continue

            val start = anchor ?: page
                .buffer
                .selection
                .start
                .also { anchor = it }

            page.dragTarget(position = change.position)?.let { target ->
                page.buffer.edit {
                    selection = TextRange(
                        start = start,
                        end = target
                    )
                }
            }

            change.consume()
        }
    }
}

/** Where [position], in the list, is from the top of the text field; null while the field is not on the page. */
private fun EditorPageState.fieldPosition(position: Offset): Offset? {
    val field = fieldPlace?.takeIf { it.isAttached } ?: return null
    val list = listPlace?.takeIf { it.isAttached } ?: return null

    return field.localPositionOf(
        sourceCoordinates = list,
        relativeToSource = position
    )
}

private fun EditorPageState.fieldY(position: Offset): Float? = fieldPosition(position = position)?.y

private fun EditorPageState.isInField(y: Float): Boolean =
    y >= 0f && y < (fieldPlace?.size?.height ?: 0)

/** Whether [y], in the list, is on the toolbar, which stays over the text field scrolled under it. */
private fun EditorPageState.isOnToolbar(y: Float): Boolean {
    val info = listState.layoutInfo
    val inContent = y + info.viewportStartOffset

    val toolbar = info.visibleItemsInfo.firstOrNull {
        it.key == TOOLBAR_KEY
    } ?: return false

    return inContent >= toolbar.offset && inContent < toolbar.offset + toolbar.size
}

/**
 * The character a drag at [position], in the list, selects up to: the one under the pointer inside the text field;
 * above the field, the start of the block under the pointer; below it, the end of that block. Null while the
 * field's layout is not the one of its text.
 */
private fun EditorPageState.dragTarget(position: Offset): Int? {
    val window = items.window ?: return null
    val shown = items.shown
    val inField = fieldPosition(position = position) ?: return null

    if (isInField(y = inField.y)) {
        return currentLayout()?.let {
            shown.min + it.getOffsetForPosition(position = inField)
        }
    }

    val info = listState.layoutInfo
    val y = position.y + info.viewportStartOffset

    val drawn = info.visibleItemsInfo.filter {
        it.key != WINDOW_KEY && it.key is Int
    }

    return if (inField.y < 0f) {
        drawn
            .map {
                it to it.key as Int
            }
            .firstOrNull { (item, block) ->
                block < window.first && y < item.offset + item.size
            }
            ?.let { (_, block) -> items.blocks.start(index = block) }
            ?: shown.min
    } else {
        drawn
            .map {
                it to it.key as Int
            }
            .lastOrNull { (item, block) ->
                block > window.last && y >= item.offset
            }
            ?.let { (_, block) -> items.blocks.textEnd(window = block..block) }
            ?: shown.max
    }
}
