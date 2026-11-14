package app.notekea.tree.presentation.view

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.RelativePath
import app.notekea.tree.domain.TreeDrop
import app.notekea.tree.domain.TreeRow

private const val DRAG_START_DP = 6

/**
 * A row of the tree dragged with the pointer onto a folder, a note or the empty part, and dropped there
 * ([onDrop]: what, and into which folder): how a note goes into a folder without leaving the tree (the developer's
 * request of 2026-11-14). [drag] says, while it lasts, which row is dragged and where it would go.
 */
internal fun Modifier.treeDrag(
    row: TreeRow,
    rows: () -> List<TreeRow>,
    list: LazyListState,
    drag: TreeDragState,
    onDrop: (path: RelativePath, folder: RelativePath) -> Unit
): Modifier = pointerInput(row.path) {
    // A click with the mouse always moves a little, and the mouse's own drag slop is a fraction of a pixel: until the
    // pointer has gone [DRAG_START_DP] from where it was pressed nothing is dragged and nothing is consumed, so the
    // row's click still happens (code review, 2026-11-14).
    val startDistance = DRAG_START_DP.dp.toPx()
    var pressedAt = 0f
    var moved = Offset.Zero

    fun start(offset: Offset) {
        val item = list
            .layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                it.key == row.path.value
            }

        pressedAt = (item?.offset ?: 0) + offset.y
        moved = Offset.Zero
    }

    fun drop() {
        val target = drag.target
        drag.end()
        if (target != null) onDrop(row.path, target)
    }

    fun move(
        change: PointerInputChange,
        amount: Offset
    ) {
        moved += amount
        val isDragging = drag.dragged == row.path
        if (!isDragging && moved.getDistance() < startDistance) return

        if (!isDragging) {
            drag.start(
                path = row.path,
                at = pressedAt + moved.y
            )
        } else {
            drag.y += amount.y
        }

        change.consume()

        val over = list
            .layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                drag.y >= it.offset && drag.y < it.offset + it.size
            }

        drag.target = TreeDrop.folderFor(
            dragged = row.path,
            over = over?.let { item ->
                rows().firstOrNull {
                    it.path.value == item.key
                }
            }
        )
    }

    // Written by hand: detectDragGestures consumes the move that passes the slop, which is what cancels the click.
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        if (!currentEvent.buttons.isPrimaryPressed) return@awaitEachGesture
        start(offset = down.position)

        while (true) {
            val change = awaitPointerEvent().changes.firstOrNull {
                it.id == down.id
            }

            // The pointer gone (the window lost it) drops nothing; the button let go drops where it is.
            if (change == null) {
                drag.end()
                break
            }

            if (!change.pressed) {
                drop()
                break
            }

            move(
                change = change,
                amount = change.positionChange()
            )
        }
    }
}
