package app.notekea.graph.presentation.model

/** An arrow key on the graph, as a step on the map. */
enum class GraphDirection(val dx: Int, val dy: Int) {
    LEFT(
        dx = -1,
        dy = 0
    ),
    RIGHT(
        dx = 1,
        dy = 0
    ),
    UP(
        dx = 0,
        dy = -1
    ),
    DOWN(
        dx = 0,
        dy = 1
    )
}
