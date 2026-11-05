package app.notekea.graph.presentation

import app.notekea.core.notes.domain.RelativePath
import app.notekea.graph.domain.GraphNode
import app.notekea.graph.domain.GraphPoint
import app.notekea.graph.domain.NoteGraph
import kotlin.math.abs
import kotlin.math.sqrt

// What the graph tab draws, unit tested directly (`ARCHITECTURE.md` §4).

private const val BASE_RADIUS = 9f
private const val RADIUS_PER_LINK = 3f
private const val MAX_RADIUS = 26f
private const val CENTER_RADIUS = 18f
private const val SIDE_WEIGHT = 2f

/** [graph] placed at [points]: more links make a bigger dot, and the center is never small. */
internal fun dotsOf(
    graph: NoteGraph,
    points: Map<RelativePath, GraphPoint>,
    depths: Map<RelativePath, Int>
): List<GraphDot> = graph.nodes.mapNotNull { node ->
    points[node.path]?.let { point ->
        val depth = depths[node.path] ?: 0
        val isCenter = depths.isNotEmpty() && depth == 0
        GraphDot(
            path = node.path,
            name = node.name,
            folder = node.folder,
            x = point.x,
            y = point.y,
            radius = if (isCenter) maxOf(radiusOf(node = node), CENTER_RADIUS) else radiusOf(node = node),
            depth = depth
        )
    }
}

private fun radiusOf(
    node: GraphNode
): Float = (BASE_RADIUS + sqrt(node.links.toFloat()) * RADIUS_PER_LINK).coerceAtMost(maximumValue = MAX_RADIUS)

/** The note an arrow key leads to: of those joined to [selected], the nearest that way; null when none is. */
internal fun noteTowards(
    direction: GraphDirection,
    selected: RelativePath,
    dots: List<GraphDot>,
    lines: List<GraphLine>
): RelativePath? {
    val from = dots.firstOrNull { it.path == selected } ?: return null
    val joined = lines.mapNotNull {
        when (selected) {
            it.first -> it.second
            it.second -> it.first
            else -> null
        }
    }.toSet()
    return dots.filter { it.path in joined }
        .map { dot -> dot to scoreOf(direction = direction, dx = dot.x - from.x, dy = dot.y - from.y) }
        .filter { (_, score) -> score != null }
        .minByOrNull { (_, score) -> score ?: Float.MAX_VALUE }
        ?.first
        ?.path
}

// Lower is better; null when the note lies the other way. Being off to the side counts more than being far.
private fun scoreOf(
    direction: GraphDirection,
    dx: Float,
    dy: Float
): Float? {
    val ahead = dx * direction.dx + dy * direction.dy
    if (ahead <= 0f) return null
    val side = abs(dx * direction.dy) + abs(dy * direction.dx)
    return ahead + side * SIDE_WEIGHT
}
