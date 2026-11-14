package app.notekea.tree.presentation.view

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import app.notekea.core.notes.domain.RelativePath
import app.notekea.tree.domain.TreeDrop
import app.notekea.tree.domain.TreeRow

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
    fun start(offset: Offset) {
        val item = list
            .layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                it.key == row.path.value
            }

        drag.start(
            path = row.path,
            at = (item?.offset ?: 0) + offset.y
        )
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
        change.consume()
        drag.y += amount.y

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

    detectDragGestures(
        onDragStart = { start(offset = it) },
        onDragEnd = { drop() },
        onDragCancel = { drag.end() },
        onDrag = { change, amount -> move(change, amount) }
    )
}
