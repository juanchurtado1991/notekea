package app.notekea.core.presentation.platform

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import java.net.URI
import kotlin.io.path.toPath

/**
 * [LocalFileDrop] on the desktop: the element takes a drag that carries files, and a drop gives their paths, read from
 * the `file:` addresses the system hands over.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
fun awtFileDrop(
    onDrop: (List<String>) -> Unit
): Modifier = Modifier.dragAndDropTarget(
    shouldStartDragAndDrop = { it.dragData() is DragData.FilesList },
    target = object : DragAndDropTarget {
        override fun onDrop(
            event: DragAndDropEvent
        ): Boolean {
            val files = (event.dragData() as? DragData.FilesList)?.readFiles().orEmpty()
            val paths = files.mapNotNull { runCatching { URI(it).toPath().toString() }.getOrNull() }
            if (paths.isNotEmpty()) onDrop(paths)
            return paths.isNotEmpty()
        }
    }
)
