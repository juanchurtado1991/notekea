package app.notekea.graph.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.ui.geometry.Offset
import app.notekea.core.notes.domain.RelativePath

/**
 * One move of the map from where its notes were to [dots]: notes that stay slide, new ones come out of the center, or
 * of the note that is going to it, and [leaving] ones fade. A move that starts mid-way starts from where notes are then.
 */
internal class GraphMotion private constructor(
    private val dots: List<GraphDot>,
    private val starts: Map<RelativePath, Offset>,
    val leaving: List<GraphDot>,
    private val origin: Offset?
) {
    val progress = Animatable(initialValue = if (origin == null) 1f else 0f)

    /** How much of the [leaving] notes still shows. */
    val fade: Float
        get() = 1f - progress.value

    fun placeOf(
        dot: GraphDot
    ): Offset {
        val target = Offset(x = dot.x, y = dot.y)
        val start = starts[dot.path] ?: origin ?: target
        return start + (target - start) * progress.value
    }

    companion object {
        /** The move after [previous], the first of all arriving at once. */
        fun after(
            dots: List<GraphDot>,
            previous: GraphMotion?,
            center: RelativePath?
        ): GraphMotion {
            val paths = dots.mapTo(destination = HashSet()) { it.path }
            val starts = previous?.run { this.dots.associate { it.path to placeOf(dot = it) } }.orEmpty()
            return GraphMotion(
                dots = dots,
                starts = starts,
                leaving = previous?.dots.orEmpty().filter { it.path !in paths },
                origin = previous?.let { starts[center] ?: Offset.Zero }
            )
        }
    }
}
