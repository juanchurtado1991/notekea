package app.notekea.graph.presentation

import app.notekea.core.notes.domain.RelativePath

/** A note as the graph draws it: [x] and [y] on the map, its [radius], [depth] links from the center and its folder. */
data class GraphDot(
    val path: RelativePath,
    val name: String,
    val folder: String,
    val x: Float,
    val y: Float,
    val radius: Float,
    val depth: Int
)
