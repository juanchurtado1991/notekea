package app.notekea.graph.presentation.model

import app.notekea.core.notes.domain.RelativePath

/** An edge as the graph draws it: solid for a link, dashed when [isRelated]. */
data class GraphLine(
    val first: RelativePath,
    val second: RelativePath,
    val isRelated: Boolean
)
