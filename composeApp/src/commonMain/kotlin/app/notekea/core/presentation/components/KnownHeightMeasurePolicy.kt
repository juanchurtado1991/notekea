package app.notekea.core.presentation.components

import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.unit.Dp

/** [ScrollAreaMeasurePolicy] whose content's height is known ([height]), so its lazy list is never asked for it. */
internal class KnownHeightMeasurePolicy(private val height: Dp) : MeasurePolicy by ScrollAreaMeasurePolicy {
    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int
    ): Int = height.roundToPx()

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int
    ): Int = height.roundToPx()
}
