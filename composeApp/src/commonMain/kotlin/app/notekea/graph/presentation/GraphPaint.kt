package app.notekea.graph.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import app.notekea.core.notes.domain.RelativePath

/** What the graph is drawn with: where each note is now and which notes stand out. */
internal class GraphPaint(
    val places: Map<RelativePath, Offset>,
    val scale: Float,
    val hovered: Set<RelativePath>,
    val showsAllLabels: Boolean,
    val lineColor: Color,
    val accent: Color,
    val labelStyle: TextStyle,
    val colorOf: (GraphDot) -> Color,
    val selected: RelativePath?
) {
    fun isDimmed(
        path: RelativePath
    ): Boolean = hovered.isNotEmpty() && path !in hovered
}
