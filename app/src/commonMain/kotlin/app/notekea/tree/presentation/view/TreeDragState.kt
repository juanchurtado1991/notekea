package app.notekea.tree.presentation.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.notekea.core.notes.domain.RelativePath

/** An entry of the tree being dragged: which one ([dragged]) and the folder it would drop into ([target]). */
internal class TreeDragState {
    var dragged by mutableStateOf<RelativePath?>(value = null)
        private set

    var target by mutableStateOf<RelativePath?>(value = null)

    // Where the pointer is, down the list.
    var y = 0f

    fun start(
        path: RelativePath,
        at: Float
    ) {
        dragged = path
        y = at
    }

    fun end() {
        dragged = null
        target = null
    }
}
