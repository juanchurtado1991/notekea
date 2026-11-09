package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val HEIGHT_DP = 6
private const val PREVIEW_FRACTION = 0.61f
private const val PREVIEW_LABEL = "Downloading"
private const val PREVIEW_WIDTH_DP = 240

/**
 * A thin bar of how much of a long job is done (a download): [fraction] from 0 to 1, or a moving bar when it is null
 * (the size is not known yet, or the job checks what it did). [label] is what a screen reader announces.
 */
@Composable
fun NotekeaProgressBar(
    label: String,
    fraction: Float?,
    modifier: Modifier = Modifier
) {
    val barModifier = modifier
        .height(height = HEIGHT_DP.dp)
        .semantics { contentDescription = label }

    val color = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant

    if (fraction == null) {
        LinearProgressIndicator(
            modifier = barModifier,
            color = color,
            trackColor = track,
            strokeCap = StrokeCap.Round
        )
    } else {
        LinearProgressIndicator(
            progress = {
                fraction.coerceIn(
                    minimumValue = 0f,
                    maximumValue = 1f
                )
            },
            modifier = barModifier,
            color = color,
            trackColor = track,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
    }
}

@Preview
@Composable
private fun NotekeaProgressBarPreview() {
    NotekeaPreviewThemes {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)) {
            NotekeaProgressBar(
                fraction = PREVIEW_FRACTION,
                label = PREVIEW_LABEL,
                modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)
            )

            NotekeaProgressBar(
                fraction = null,
                label = PREVIEW_LABEL,
                modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)
            )
        }
    }
}
