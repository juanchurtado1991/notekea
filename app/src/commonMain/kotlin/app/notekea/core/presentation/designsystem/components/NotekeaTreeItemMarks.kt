package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val BAR_INSET_DP = 5
private const val BAR_WIDTH_DP = 3
private const val PILL_ALPHA = 0.1f
private const val PILL_PADDING_DP = 6
private const val PREVIEW_COUNT = "12"

/**
 * What a tree row draws under its content: a thin [guide] line under each of the [depth] folders it is in, where their
 * chevrons are, and the selected row's [bar] at its start (null when it is not selected).
 */
internal fun Modifier.treeRowMarks(
    depth: Int,
    guide: Color,
    bar: Color?
): Modifier = drawBehind {
    val indent = NotekeaDimensions.TreeIndent.toPx()
    val first = (NotekeaDimensions.SpacingXs + NotekeaDimensions.IconSize / 2).toPx()

    repeat(times = depth) { level ->
        val x = first + indent * level

        drawLine(
            color = guide,
            start = Offset(
                x = x,
                y = 0f
            ),
            end = Offset(
                x = x,
                y = size.height
            ),
            strokeWidth = NotekeaDimensions.BorderWidth.toPx()
        )
    }

    if (bar != null) {
        val inset = BAR_INSET_DP.dp.toPx()
        val width = BAR_WIDTH_DP.dp.toPx()

        drawRoundRect(
            color = bar,
            topLeft = Offset(
                x = 0f,
                y = inset
            ),
            size = Size(
                width = width,
                height = size.height - inset * 2
            ),
            cornerRadius = CornerRadius(x = width / 2)
        )
    }
}

/** How many a folder holds, at the end of its row: [text] in a soft pill. */
@Composable
internal fun TreeCountPill(text: String) {
    val colors = MaterialTheme.colorScheme

    Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingS))

    Text(
        text = text,
        modifier = Modifier
            .background(
                color = colors.onSurfaceVariant.copy(alpha = PILL_ALPHA),
                shape = CircleShape
            )
            .padding(horizontal = PILL_PADDING_DP.dp),
        style = MaterialTheme.typography.labelSmall,
        color = colors.onSurfaceVariant
    )
}

@Preview
@Composable
private fun TreeCountPillPreview() {
    NotekeaPreviewThemes { TreeCountPill(text = PREVIEW_COUNT) }
}
