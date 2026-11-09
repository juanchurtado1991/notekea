package app.notekea.graph.domain

import app.notekea.core.notes.domain.RelativePath

/** Two notes joined by a link, or close in meaning when [isRelated]; no direction: [first] sorts before [second]. */
data class GraphEdge(
    val first: RelativePath,
    val second: RelativePath,
    val isRelated: Boolean = false
) {
    companion object {
        fun between(
            one: RelativePath,
            other: RelativePath,
            isRelated: Boolean = false
        ): GraphEdge = if (one.value <= other.value) {
            GraphEdge(first = one, second = other, isRelated = isRelated)
        } else {
            GraphEdge(first = other, second = one, isRelated = isRelated)
        }
    }
}
