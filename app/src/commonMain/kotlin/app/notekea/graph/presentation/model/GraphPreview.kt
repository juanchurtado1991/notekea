package app.notekea.graph.presentation.model

import app.notekea.core.notes.domain.RelativePath

/**
 * What the inspector shows of the selected note: its [snippet] as plain text, the notes it is [connected] to and those
 * close in meaning that are not, [suggested] to link.
 */
data class GraphPreview(
    val path: RelativePath,
    val name: String,
    val folder: String,
    val snippet: String,
    val connected: List<RelativePath>,
    val suggested: List<RelativePath>
)
