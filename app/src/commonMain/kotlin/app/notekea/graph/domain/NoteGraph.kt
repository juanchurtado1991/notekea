package app.notekea.graph.domain

import app.notekea.core.notes.domain.RelativePath

/** The notes of a vault and how they connect. Tags and attachments are never part of it. */
data class NoteGraph(
    val nodes: List<GraphNode>,
    val edges: List<GraphEdge>
) {
    /** How many links away from [center] each note is, up to [depth]; [center] itself at 0. */
    fun depthsFrom(
        center: RelativePath,
        depth: Int
    ): Map<RelativePath, Int> {
        val neighbors = neighbors()
        val depths = mutableMapOf(center to 0)
        var ring = setOf(center)

        for (step in 1..depth) {
            ring = ring
                .flatMapTo(destination = HashSet()) { neighbors[it].orEmpty() } - depths.keys

            ring.forEach { depths[it] = step }
        }

        return depths
    }

    /** Only the notes in [paths], and the edges between them. */
    fun only(paths: Set<RelativePath>): NoteGraph = NoteGraph(
        nodes = nodes.filter {
            it.path in paths
        },
        edges = edges.filter {
            it.first in paths && it.second in paths
        }
    )

    /** Without the notes of [folders], top-level names as [GraphNode.folder] gives them. */
    fun withoutFolders(folders: Set<String>): NoteGraph = if (folders.isEmpty()) {
        this
    } else {
        only(
            paths = nodes
                .filter {
                    it.folder !in folders
                }
                .mapTo(destination = HashSet()) { it.path }
        )
    }

    /** Without the notes that no edge reaches. */
    fun withoutOrphans(): NoteGraph {
        val joined = edges.flatMapTo(destination = HashSet()) { listOf(it.first, it.second) }

        return only(
            paths = nodes
                .mapTo(destination = HashSet()) { it.path } intersect joined
        )
    }

    /** This graph with [related] added, each joined to [center]; a pair already linked stays a link. */
    fun withRelated(
        center: RelativePath,
        related: List<RelativePath>
    ): NoteGraph {
        val known = edges
            .mapTo(destination = HashSet()) {
                it.first to it.second
            }

        val added = related.map {
            GraphEdge.between(
                one = center,
                other = it,
                isRelated = true
            )
        }
            .filter {
                (it.first to it.second) !in known
            }

        return copy(edges = edges + added)
    }

    /** This graph with [one] and [other] linked, as it will be once the vault is read again. */
    fun withLink(
        one: RelativePath,
        other: RelativePath
    ): NoteGraph = copy(
        nodes = shifted(
            one = one,
            other = other,
            by = 1
        ),
        edges = edges + GraphEdge.between(
            one = one,
            other = other
        )
    )

    /** This graph without the link between [one] and [other]. */
    fun withoutLink(
        one: RelativePath,
        other: RelativePath
    ): NoteGraph = copy(
        nodes = shifted(
            one = one,
            other = other,
            by = -1
        ),
        edges = edges - GraphEdge.between(
            one = one,
            other = other
        )
    )

    private fun shifted(
        one: RelativePath,
        other: RelativePath,
        by: Int
    ): List<GraphNode> = nodes.map {
        if (it.path == one || it.path == other) it.copy(links = it.links + by) else it
    }

    /** Each note and the notes an edge joins it to. */
    fun neighbors(): Map<RelativePath, List<RelativePath>> {
        val neighbors = HashMap<RelativePath, MutableList<RelativePath>>()

        edges.forEach { edge ->
            neighbors
                .getOrPut(key = edge.first) { mutableListOf() } += edge.second

            neighbors
                .getOrPut(key = edge.second) { mutableListOf() } += edge.first
        }

        return neighbors
    }
}
