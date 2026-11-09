package app.notekea.editor.presentation.model

import app.notekea.core.notes.domain.DayNeighbors

/**
 * What the note's header shows besides the editor's own state: for a daily note, its [dayNeighbors] to step to the days
 * before and after (`ROADMAP.md` 1.10), and whether the note [isBookmarked] (`ROADMAP.md` 1.13).
 */
internal data class EditorHeaderData(
    val dayNeighbors: DayNeighbors?,
    val isBookmarked: Boolean = false
)
