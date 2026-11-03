package app.notekea.core.presentation.components

import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints

/**
 * [NotekeaScrollArea]'s layout: the content at its size, the scrollbar as tall as it on its right edge. The area's intrinsic size is the content's,
 * so a parent can ask how tall it would be without measuring the scrollbar against an endless height.
 */
internal object ScrollAreaMeasurePolicy : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        val content = measurables.first().measure(constraints = constraints.copy(minWidth = 0, minHeight = 0))
        val width = maxOf(content.width, constraints.minWidth)
        val height = maxOf(content.height, constraints.minHeight)
        val bar = measurables.last().measure(constraints = Constraints.fixedHeight(height = height))
        return layout(width = width, height = height) {
            content.placeRelative(x = 0, y = 0)
            bar.placeRelative(x = width - bar.width, y = 0)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int
    ): Int = measurables.first().minIntrinsicHeight(width = width)

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int
    ): Int = measurables.first().maxIntrinsicHeight(width = width)

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int
    ): Int = measurables.first().minIntrinsicWidth(height = height)

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int
    ): Int = measurables.first().maxIntrinsicWidth(height = height)
}
