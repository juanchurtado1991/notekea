package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes

private const val RING_DP = 12
private const val STROKE_DP = 2
private const val PREVIEW_FRACTION = 0.28f

/**
 * A small ring that fills as work of a known length goes on ([fraction], from 0 to 1), the size of a status bar icon:
 * the [NotekeaStatusLabel]'s leading element for "Indexing · 28 %".
 */
@Composable
fun NotekeaStatusRing(
    fraction: Float,
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        progress = {
            fraction.coerceIn(
                minimumValue = 0f,
                maximumValue = 1f
            )
        },
        modifier = modifier.size(size = RING_DP.dp),
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = STROKE_DP.dp,
        trackColor = MaterialTheme.colorScheme.outlineVariant,
        gapSize = 0.dp
    )
}

@Preview
@Composable
private fun NotekeaStatusRingPreview() {
    NotekeaPreviewThemes { NotekeaStatusRing(fraction = PREVIEW_FRACTION) }
}
