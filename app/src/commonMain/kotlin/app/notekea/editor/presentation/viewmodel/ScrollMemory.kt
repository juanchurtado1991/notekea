package app.notekea.editor.presentation.viewmodel

import app.notekea.core.notes.domain.RelativePath

/**
 * Where the view was in each note shown in one pane, so going back to a note's tab shows the same
 * part of it. In memory only: a note opened in a new run starts at the top.
 */
class ScrollMemory {
    private val places = mutableMapOf<RelativePath, Place>()

    fun of(path: RelativePath): Place = places[path] ?: Place()

    fun keep(
        path: RelativePath,
        place: Place
    ) {
        places[path] = place
    }

    /** A place of the editor's list: its first [item] in view, scrolled [offset] pixels out of it. */
    data class Place(
        val item: Int = 0,
        val offset: Int = 0
    )
}
