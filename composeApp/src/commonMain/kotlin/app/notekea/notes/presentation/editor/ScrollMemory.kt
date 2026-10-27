package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.RelativePath

/**
 * Where the view was in each note shown in one pane (`ROADMAP.md` 1.11), so going back to a note's tab shows the same
 * part of it. In memory only: a note opened in a new run starts at the top.
 */
class ScrollMemory {
    private val offsets = mutableMapOf<RelativePath, Int>()

    fun of(
        path: RelativePath
    ): Int = offsets[path] ?: 0

    fun keep(
        path: RelativePath,
        offset: Int
    ) {
        offsets[path] = offset
    }
}
