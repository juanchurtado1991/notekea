package app.notekea.graph.presentation

import app.notekea.core.notes.domain.RelativePath

/** What the inspector shows of the selected note: its [snippet] as plain text and the notes it is [connected] to. */
data class GraphPreview(
    val path: RelativePath,
    val name: String,
    val folder: String,
    val snippet: String,
    val connected: List<RelativePath>
)
