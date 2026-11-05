package app.notekea.graph.presentation

import app.notekea.core.notes.domain.RelativePath

/** One-shot events of the graph. */
sealed interface GraphEffect {
    /** The note at [path] opens in a tab. */
    data class OpenNote(val path: RelativePath) : GraphEffect

    /** `Space`: the side panel, where the inspector lives, shows or hides. */
    data object ToggleInspector : GraphEffect

    /** The note at [path] was written from the graph: an editor that has it open takes the new text. */
    data class NoteRewritten(val path: RelativePath) : GraphEffect
}
