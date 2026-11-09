package app.notekea.graph.domain

import app.notekea.core.notes.domain.RelativePath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Where the graph's notes sit. Around a note they sit on [rings], one per link away. The whole vault
 * is [spread] by forces: edges pull, near notes push apart, within a grid, so each round stays linear in the notes.
 */
class GraphLayout {
    /** [depths] as rings around (0, 0), folders side by side, the same way every time. */
    fun rings(
        graph: NoteGraph,
        depths: Map<RelativePath, Int>
    ): Map<RelativePath, GraphPoint> {
        val points = HashMap<RelativePath, GraphPoint>()

        graph
            .nodes
            .groupBy {
                depths[it.path] ?: 0
            }
            .forEach { (depth, nodes) ->
                val ring = nodes.sortedWith(compareBy({ it.folder }, { it.name }))

                ring.forEachIndexed { index, node ->
                    val angle = START_ANGLE + TURN * index / ring.size + depth * RING_TWIST
                    val radius = max(depth * RING_GAP, ring.size * MIN_ARC / TURN)

                    points[node.path] = if (depth == 0) {
                        ORIGIN
                    } else {
                        pointAt(
                            angle = angle,
                            radius = radius
                        )
                    }
                }
            }

        return points
    }

    /** The whole [graph] after [rounds] of forces; [onRound] may stop it, a cancelled caller for one. */
    suspend fun spread(
        graph: NoteGraph,
        rounds: Int = ROUNDS,
        onRound: suspend () -> Unit = {}
    ): Map<RelativePath, GraphPoint> {
        val index = graph
            .nodes
            .withIndex()
            .associate { (i, node) ->
                node.path to i
            }

        val x = FloatArray(size = graph.nodes.size) { spiral(index = it).x }

        val y = FloatArray(size = graph.nodes.size) { spiral(index = it).y }

        val pairs = graph.edges.mapNotNull { edge ->
            val first = index[edge.first]
            val second = index[edge.second]
            if (first == null || second == null) null else first to second
        }

        repeat(times = rounds) { round ->
            onRound()
            val pushX = FloatArray(size = x.size)
            val pushY = FloatArray(size = y.size)

            repel(
                x = x,
                y = y,
                pushX = pushX,
                pushY = pushY
            )

            attract(
                pairs = pairs,
                x = x,
                y = y,
                pushX = pushX,
                pushY = pushY
            )

            val heat = 1f - round.toFloat() / rounds

            for (i in x.indices) {
                x[i] += limited(
                    value = pushX[i] - x[i] * GRAVITY,
                    heat = heat
                )

                y[i] += limited(
                    value = pushY[i] - y[i] * GRAVITY,
                    heat = heat
                )
            }
        }

        return graph
            .nodes
            .indices
            .associate {
                graph.nodes[it].path to GraphPoint(
                    x = x[it],
                    y = y[it]
                )
            }
    }

    private fun repel(
        x: FloatArray,
        y: FloatArray,
        pushX: FloatArray,
        pushY: FloatArray
    ) {
        val cells = HashMap<Long, MutableList<Int>>()

        for (i in x.indices) {
            cells
                .getOrPut(
                    key = cellOf(
                        x = x[i],
                        y = y[i]
                    )
                ) { mutableListOf() } += i
        }

        for (i in x.indices) {
            val column = (x[i] / CELL).toInt()
            val row = (y[i] / CELL).toInt()

            for (c in column - 1..column + 1) {
                for (r in row - 1..row + 1) {
                    cells[
                        keyOf(
                            column = c,
                            row = r
                        )
                    ]?.forEach { other ->
                        if (other != i) {
                            push(
                                i = i,
                                other = other,
                                x = x,
                                y = y,
                                pushX = pushX,
                                pushY = pushY
                            )
                        }
                    }
                }
            }
        }
    }

    private fun push(
        i: Int,
        other: Int,
        x: FloatArray,
        y: FloatArray,
        pushX: FloatArray,
        pushY: FloatArray
    ) {
        val dx = x[i] - x[other]
        val dy = y[i] - y[other]
        val distance = max(sqrt(dx * dx + dy * dy), MIN_DISTANCE)
        val force = REPULSION / (distance * distance)
        pushX[i] += dx * force
        pushY[i] += dy * force
    }

    private fun attract(
        pairs: List<Pair<Int, Int>>,
        x: FloatArray,
        y: FloatArray,
        pushX: FloatArray,
        pushY: FloatArray
    ) {
        pairs.forEach { (first, second) ->
            val dx = x[second] - x[first]
            val dy = y[second] - y[first]
            pushX[first] += dx * SPRING
            pushY[first] += dy * SPRING
            pushX[second] -= dx * SPRING
            pushY[second] -= dy * SPRING
        }
    }

    private fun limited(
        value: Float,
        heat: Float
    ): Float = value.coerceIn(
        minimumValue = -MAX_STEP * heat,
        maximumValue = MAX_STEP * heat
    )

    private fun cellOf(
        x: Float,
        y: Float
    ): Long = keyOf(
        column = (x / CELL).toInt(),
        row = (y / CELL).toInt()
    )

    private fun keyOf(
        column: Int,
        row: Int
    ): Long = (column.toLong() shl Int.SIZE_BITS) xor (row.toLong() and INT_MASK)

    // A sunflower spiral: an even start, and the same one every time.
    private fun spiral(index: Int): GraphPoint = pointAt(
        angle = index * GOLDEN_ANGLE,
        radius = SPIRAL_STEP * sqrt(index.toFloat())
    )

    private fun pointAt(
        angle: Float,
        radius: Float
    ): GraphPoint = GraphPoint(
        x = cos(angle) * radius,
        y = sin(angle) * radius
    )

    private companion object {
        val ORIGIN = GraphPoint(
            x = 0f,
            y = 0f
        )
        const val TURN = (2 * PI).toFloat()
        const val START_ANGLE = (-PI / 2).toFloat()
        const val RING_TWIST = 0.4f
        const val RING_GAP = 170f
        const val MIN_ARC = 110f
        const val ROUNDS = 80
        const val CELL = 150f
        const val MIN_DISTANCE = 1f
        const val REPULSION = 1_200f
        const val SPRING = 0.04f
        const val GRAVITY = 0.01f
        const val MAX_STEP = 30f
        const val SPIRAL_STEP = 30f
        const val GOLDEN_ANGLE = 2.3999631f
        const val INT_MASK = 0xFFFFFFFFL
    }
}
