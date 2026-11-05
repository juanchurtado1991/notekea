package app.notekea.graph.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import app.notekea.core.notes.domain.RelativePath

private const val DIMMED = 0.18f
private const val DEPTH_FADE = 0.2f
private const val HALO_GROWTH = 8f
private const val HALO_ALPHA = 0.22f
private const val LABEL_GAP = 6f
private const val LABEL_WIDTH = 150
private const val LINK_WIDTH = 1.4f
private const val RELATED_WIDTH = 1.2f
private val RELATED_DASH = floatArrayOf(4f, 6f)

internal fun DrawScope.drawLines(
    lines: List<GraphLine>,
    paint: GraphPaint
) {
    lines.forEach { line ->
        val start = paint.places[line.first]
        val end = paint.places[line.second]
        if (start != null && end != null) {
            val isDimmed = paint.isDimmed(path = line.first) || paint.isDimmed(path = line.second)
            drawLine(
                color = (if (line.isRelated) paint.accent else paint.lineColor).copy(
                    alpha = if (isDimmed) DIMMED else 1f
                ),
                start = start,
                end = end,
                strokeWidth = if (line.isRelated) RELATED_WIDTH else LINK_WIDTH,
                pathEffect = if (line.isRelated) PathEffect.dashPathEffect(intervals = RELATED_DASH) else null
            )
        }
    }
}

internal fun DrawScope.drawDots(
    dots: List<GraphDot>,
    paint: GraphPaint,
    measurer: TextMeasurer
) {
    dots.forEach { dot ->
        val center = paint.places[dot.path] ?: return@forEach
        val radius = dot.radius * paint.scale
        val alpha = if (paint.isDimmed(path = dot.path)) DIMMED else 1f - dot.depth * DEPTH_FADE / 2
        if (dot.path == paint.selected) {
            drawCircle(color = paint.accent.copy(alpha = HALO_ALPHA), radius = radius + HALO_GROWTH, center = center)
        }
        drawCircle(color = paint.colorOf(dot).copy(alpha = alpha), radius = radius, center = center)
        val isNamed = paint.showsAllLabels || dot.path == paint.selected || dot.path in paint.hovered ||
            paint.isNamedMatch(path = dot.path)
        if (isNamed) drawName(dot, center, radius, alpha, paint, measurer)
    }
}

private fun DrawScope.drawName(
    dot: GraphDot,
    center: Offset,
    radius: Float,
    alpha: Float,
    paint: GraphPaint,
    measurer: TextMeasurer
) {
    val text = measurer.measure(
        text = dot.name,
        style = paint.labelStyle,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        constraints = Constraints(maxWidth = LABEL_WIDTH)
    )
    val topLeft = Offset(x = center.x - text.size.width / 2f, y = center.y + radius + LABEL_GAP)
    drawText(textLayoutResult = text, topLeft = topLeft, alpha = alpha)
}
