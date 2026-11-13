package app.notekea.graph.presentation.viewmodel

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.ViewModelHost
import app.notekea.graph.domain.NoteGraph
import app.notekea.graph.presentation.model.GraphEffect
import app.notekea.graph.presentation.model.GraphState

/**
 * What the graph's parts (search, linking) share with [GraphViewModel]: besides the state, the vault's [notes], the
 * [graph] built from them, which a link changes, and a state change that draws the graph again ([change]).
 */
internal interface GraphHost : ViewModelHost<GraphState, GraphEffect> {
    val notes: Map<RelativePath, NoteEntry>
    var graph: NoteGraph?

    fun change(transform: (GraphState) -> GraphState)
}
