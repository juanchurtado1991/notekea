package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.DayNeighbors
import app.notekea.notes.domain.NoteMetadata

/**
 * What the note's header shows besides the editor's own state: the [properties] read from its frontmatter (tags and
 * aliases), and, for a daily note, its [dayNeighbors] to step to the days before and after (`ROADMAP.md` 1.10).
 */
internal data class EditorHeaderData(
    val properties: NoteMetadata?,
    val dayNeighbors: DayNeighbors?
)
