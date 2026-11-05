package app.notekea.graph.presentation

import app.notekea.core.notes.domain.RelativePath

/** One-shot events of the graph. */
sealed interface GraphEffect {
    /** The note at [path] opens in a tab. */
    data class OpenNote(val path: RelativePath) : GraphEffect
}
