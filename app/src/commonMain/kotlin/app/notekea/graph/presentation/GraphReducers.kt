package app.notekea.graph.presentation

import app.notekea.core.notes.domain.FuzzyMatcher
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
private const val DEPTH_SHRINK = 0.12f
private const val SIDE_WEIGHT = 2f

/** [graph] placed at [points]: more links make a bigger dot, farther rings smaller ones; the center is never small. */
internal fun dotsOf(
    graph: NoteGraph,
    points: Map<RelativePath, GraphPoint>,
    depths: Map<RelativePath, Int>
): List<GraphDot> = graph.nodes.mapNotNull { node ->
    points[node.path]?.let { point ->
        val depth = depths[node.path] ?: 0
        val isCenter = depths.isNotEmpty() && depth == 0
        val radius = radiusOf(node = node) * (1f - depth * DEPTH_SHRINK)

        val shownRadius = if (isCenter) maxOf(radius, CENTER_RADIUS) else radius

        GraphDot(
            path = node.path,
            name = node.name,
            folder = node.folder,
            x = point.x,
            y = point.y,
            radius = shownRadius,
            depth = depth
        )
    }
}

private fun radiusOf(node: GraphNode): Float = (
    BASE_RADIUS + sqrt(node.links.toFloat()) * RADIUS_PER_LINK
    ).coerceAtMost(maximumValue = MAX_RADIUS)

/** The notes of [paths] named like [query], best first; the same score goes by path, so the order holds. */
internal fun matchesOf(
    query: String,
    paths: Collection<RelativePath>,
    matcher: FuzzyMatcher
): List<RelativePath> = if (query.isBlank()) {
    emptyList()
} else {
    paths
        .mapNotNull { path ->
            matcher.match(query, path.nameWithoutExtension)?.let {
                path to it.score
            }
        }
        .sortedWith(
            compareByDescending<Pair<RelativePath, Int>> { it.second }.thenBy { it.first.value }
        )
        .map { it.first }
}

/** The note an arrow key leads to: of those joined to [selected], the nearest that way; null when none is. */
internal fun noteTowards(
    direction: GraphDirection,
    selected: RelativePath,
    dots: List<GraphDot>,
    lines: List<GraphLine>
): RelativePath? {
    val from = dots.firstOrNull {
        it.path == selected
    } ?: return null

    val joined = lines.mapNotNull {
        when (selected) {
            it.first -> it.second
            it.second -> it.first
            else -> null
        }
    }.toSet()

    return dots
        .filter {
            it.path in joined
        }
        .map { dot ->
            dot to scoreOf(
                direction = direction,
                dx = dot.x - from.x,
                dy = dot.y - from.y
            )
        }
        .filter { (_, score) ->
            score != null
        }
        .minByOrNull { (_, score) ->
            score ?: Float.MAX_VALUE
        }
        ?.first
        ?.path
}

/** The notes a link joins to [path]; related notes are not. */
internal fun connectedTo(
    path: RelativePath,
    lines: List<GraphLine>
): List<RelativePath> = lines.filter {
    !it.isRelated
}.mapNotNull {
    when (path) {
        it.first -> it.second
        it.second -> it.first
        else -> null
    }
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
