package app.notekea.graph.domain

import app.notekea.core.notes.domain.RelativePath

/** A note on the graph (`ROADMAP.md` 2.7): its [path], and how many [links] it has, which size its dot. */
data class GraphNode(
    val path: RelativePath,
    val links: Int
) {
    val name: String
        get() = path.nameWithoutExtension

    /** The top-level folder that colors it; empty at the vault's root. */
    val folder: String
        get() = path.value.substringBefore(delimiter = RelativePath.SEPARATOR, missingDelimiterValue = "")
}
