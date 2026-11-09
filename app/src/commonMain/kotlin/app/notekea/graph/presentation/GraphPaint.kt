package app.notekea.graph.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import app.notekea.core.notes.domain.RelativePath

/** What the graph is drawn with: where each note is now and which notes stand out; [matches] while searching. */
internal class GraphPaint(
    val places: Map<RelativePath, Offset>,
    val scale: Float,
    val hovered: Set<RelativePath>,
    val showsAllLabels: Boolean,
    val lineColor: Color,
    val accent: Color,
    val labelStyle: TextStyle,
    val labelBackground: Color,
    val colorOf: (GraphDot) -> Color,
    val selected: RelativePath?,
    val matches: Set<RelativePath>?
) {
    fun isDimmed(path: RelativePath): Boolean = (hovered.isNotEmpty() && path !in hovered) ||
        (
            matches != null &&
                path !in matches &&
                path != selected
            )

    /** A few matches say their names; many would cover the map. */
    fun isNamedMatch(path: RelativePath): Boolean =
        matches != null &&
            matches.size <= MAX_NAMED_MATCHES &&
            path in matches

    private companion object {
        const val MAX_NAMED_MATCHES = 30
    }
}
