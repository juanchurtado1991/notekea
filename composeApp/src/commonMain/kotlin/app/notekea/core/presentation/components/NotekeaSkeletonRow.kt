package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val BAR_HEIGHT_DP = 10
private const val PREVIEW_WIDTH_DP = 260
private val PREVIEW_ROWS = listOf(0 to 0.55f, 1 to 0.7f, 1 to 0.45f, 0 to 0.6f)

/**
 * Placeholder for a row that is still loading, the height of a tree row so nothing moves when the content arrives.
 * [widthFraction] varies the bar so a column of skeletons reads as text; [depth] indents it like a tree row. It is
 * static on purpose: no shimmer to flash on screen, nothing to switch off for reduced motion.
 */
@Composable
fun NotekeaSkeletonRow(
    widthFraction: Float,
    modifier: Modifier = Modifier,
    depth: Int = 0
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height = NotekeaDimensions.TreeRowHeight)
            .padding(
                start = NotekeaDimensions.SpacingM + NotekeaDimensions.TreeIndent * depth,
                end = NotekeaDimensions.SpacingS
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = widthFraction)
                .height(height = BAR_HEIGHT_DP.dp)
                .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.extraSmall)
        )
    }
}

@Preview
@Composable
private fun NotekeaSkeletonRowPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            PREVIEW_ROWS.forEach { (depth, fraction) -> NotekeaSkeletonRow(widthFraction = fraction, depth = depth) }
        }
    }
}
